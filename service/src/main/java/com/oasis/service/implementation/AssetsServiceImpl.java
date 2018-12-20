package com.oasis.service.implementation;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.CollectionName;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.LastUniqueIdentifierModel;
import com.oasis.model.fieldname.AssetFieldName;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.LastUniqueIdentifierRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.service.DocumentHeader;
import com.oasis.service.ImageHelper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsServiceImpl
        implements AssetsServiceApi {

    private Logger logger = LoggerFactory.getLogger(AssetsServiceImpl.class);
    @Autowired
    private ImageHelper imageHelper;
    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private DocumentHeader documentHeader;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private LastUniqueIdentifierRepository lastUniqueIdentifierRepository;

    /*-------------Assets List Methods-------------*/
    @Override
    @Cacheable(value = "availableAssetsList", unless = "#result.size() == 0")
    public List< AssetModel > getAvailableAssetsList(
            final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = ((query != null) && query.isEmpty());

        if (emptyQueryGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        if (sort == null) {
            sort = "A-name";
        }

        if (!sort.matches("^[AD]-(SKU|name)$")) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        final Set< AssetModel > availableAssets;
        final long availableAssetsCount;
        final long availablePages;
        final boolean noAvailableAsset;
        final boolean pageIndexOutOfBounds;

        final boolean noQueryGiven = (query == null);

        if (noQueryGiven) {
            availableAssetsCount = assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
            availablePages = (long) Math.ceil((double) availableAssetsCount / ServiceConstant.ASSETS_LIST_PAGE_SIZE);

            noAvailableAsset = (availableAssetsCount == 0);
            pageIndexOutOfBounds = ((page < 1) || (page > availablePages));

            if (noAvailableAsset || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            availableAssets = new LinkedHashSet<>(getSortedAvailableAssets(page, sort));
        } else {
            availableAssetsCount = getAvailableAssetsCount(query, sort);
            availablePages = (long) Math.ceil((double) availableAssetsCount / ServiceConstant.ASSETS_LIST_PAGE_SIZE);

            noAvailableAsset = (availableAssetsCount == 0);
            pageIndexOutOfBounds = ((page < 1) || (page > availablePages));

            if (noAvailableAsset || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            availableAssets = new LinkedHashSet<>(getSortedAvailableAssetsFromQuery(page, query, sort));
        }

        return new ArrayList<>(availableAssets);
    }

    private Set< AssetModel > getSortedAvailableAssets(
            final int page, final String sort
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, ServiceConstant.ASSETS_LIST_PAGE_SIZE);

        if (sort.substring(2).equals("SKU")) {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuAsc(
                        ServiceConstant.ZERO, pageable).getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuDesc(
                        ServiceConstant.ZERO, pageable).getContent());
            }
        } else {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameAsc(
                        ServiceConstant.ZERO, pageable).getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameDesc(
                        ServiceConstant.ZERO, pageable).getContent());
            }
        }

        return sortedAvailableAssets;
    }

    private Set< AssetModel > getSortedAvailableAssetsFromQuery(
            final int page, final String query, final String sort
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, ServiceConstant.ASSETS_LIST_PAGE_SIZE);

        if (sort.substring(2).equals("SKU")) {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
                                                             ServiceConstant.ZERO, query, query, pageable)
                                                     .getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
                                                             ServiceConstant.ZERO, query, query, pageable)
                                                     .getContent());
            }
        } else {
            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                                             ServiceConstant.ZERO, query, query, pageable)
                                                     .getContent());
            } else {
                sortedAvailableAssets.addAll(assetRepository
                                                     .findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                                                             ServiceConstant.ZERO, query, query, pageable)
                                                     .getContent());
            }
        }

        return sortedAvailableAssets;
    }

    @Override
    public long getAvailableAssetsCount(
            final String query, final String sort
    ) {

        final boolean noQueryGiven = (query == null);

        if (noQueryGiven) {
            return assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        } else {
            return assetRepository
                    .countAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCase(
                            ServiceConstant.ZERO, query, query);
        }
    }

    @Override
    @Cacheable(value = "assetDetailData", key = "#sku")
    public AssetModel getAssetDetailData(
            final String sku
    )
            throws
            DataNotFoundException {

        final AssetModel assetDetailData = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        final boolean assetWithSkuDoesNotExist = (assetDetailData == null);

        if (assetWithSkuDoesNotExist) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        return assetDetailData;
    }

    @Override
    public List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    ) {

        List< String > imageURLs = new ArrayList<>();

        if (imageDirectory.isEmpty()) {
            return imageURLs;
        }

        final File directory = new File(imageDirectory);
        final File[] images = directory.listFiles();

        if (Files.exists(directory.toPath()) && images != null) {
            for (int i = 0; i < images.length; i++) {
                final String extension = imageHelper.getExtensionFromFileName(images[i].getName());

                imageURLs.add("http://localhost:8085/oasis/api/assets/" + sku + "/" +
                              sku.concat("-").concat(String.valueOf(i + 1)).concat("?extension=").concat(extension));
            }
        }

        return imageURLs;
    }

    @Override
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    public byte[] getAssetDetailInPdf(
            final String sku
    ) {

        final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        if (asset == null) {
            return new byte[0];
        }

        Document document = new Document(PageSize.A4, 36, 36, 90, 36);

        new File(sku.concat(ServiceConstant.EXTENSION_PDF));

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

            DocumentHeader event = documentHeader;
            writer.setPageEvent(event);

            document.open();

            document.add(Chunk.NEWLINE);

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, new BaseColor(32, 162, 223));
            Chunk chunk = new Chunk(asset.getName(), font);
            Paragraph paragraph = new Paragraph(chunk);
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);

            document.add(Chunk.NEWLINE);

            File possibleDirectory = new File(asset.getImageDirectory());

            final File[] images = possibleDirectory.listFiles();
            if (possibleDirectory.exists() && possibleDirectory.isDirectory() && images != null) {
                for (final File image : images) {
                    if (Files.exists(image.toPath())) {
                        Image detailImage = Image.getInstance(Files.readAllBytes(Paths.get(image.toURI())));
                        detailImage.scaleToFit(detailImage.getWidth(), document.getPageSize().getWidth() / 5);
                        detailImage.setAlignment(Element.ALIGN_CENTER);
                        document.add(detailImage);
                    }

                    break;  //Takes only first result, but code is prepared for multiple images
                }
            } else {
                Paragraph altDetailImageParagraph = new Paragraph("No Image Available!");
                altDetailImageParagraph.setAlignment(Element.ALIGN_CENTER);
                document.add(altDetailImageParagraph);
            }
            document.add(Chunk.NEWLINE);

            PdfPTable detailTable = new PdfPTable(2);
            detailTable.setWidths(new float[]{ 2, 7 });

            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.BLACK);

            addHeaderCellToTable(detailTable, "Description", infoFont);
            addHeaderCellToTable(detailTable, "Attribute", infoFont);

            addContentCellToTable(detailTable, "SKU", asset.getSku());
            addContentCellToTable(detailTable, "Name", asset.getName());
            addContentCellToTable(detailTable, "Brand", asset.getBrand());
            addContentCellToTable(detailTable, "Type", asset.getType());
            addContentCellToTable(detailTable, "Stock", String.valueOf(asset.getStock()));
            addContentCellToTable(detailTable, "Location", asset.getLocation());
            addContentCellToTable(detailTable, "Price", getFormattedPrice(asset.getPrice()));

            document.add(detailTable);

            document.close();

            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException | IOException exception) {
            logger.error("Failed to generate PDF document as DocumentException or IOException occurred with message: " +
                         exception.getMessage());
            return new byte[0];
        }
    }

    private void addHeaderCellToTable(
            PdfPTable table, final String value, final Font font
    ) {

        Paragraph description = new Paragraph(value, font);
        description.setAlignment(Element.ALIGN_CENTER);

        PdfPCell headerCell = new PdfPCell(description);
        headerCell.setPadding(5);
        headerCell.setPaddingBottom(10);

        table.addCell(headerCell);
    }

    private void addContentCellToTable(
            PdfPTable table, final String name, final String data
    ) {

        table.addCell(name);

        PdfPCell contentCell = new PdfPCell(new Phrase(data));
        contentCell.setPadding(2);
        contentCell.setPaddingLeft(10);
        contentCell.setPaddingBottom(5);

        table.addCell(contentCell);
    }

    private String getFormattedPrice(
            final double price
    ) {

        StringBuilder priceStr = new StringBuilder(String.format("%.2f", price));
        priceStr.reverse();

        String fraction = String.valueOf(priceStr).substring(0, 3);
        priceStr.replace(0, 3, "");

        StringBuilder formattedPriceStr = new StringBuilder();

        final int initialLength = priceStr.length();
        while (priceStr.length() > 0) {
            StringBuilder temp;

            if (priceStr.length() >= 3) {
                temp = new StringBuilder(String.valueOf(priceStr).substring(0, 3));
                priceStr.replace(0, 3, "");
            } else {
                temp = new StringBuilder(String.valueOf(priceStr));
                priceStr.replace(0, String.valueOf(priceStr).length(), "");
            }

            temp.reverse();
            if (initialLength - 3 != priceStr.length()) {
                temp.append(",");
            }
            formattedPriceStr.insert(0, temp);
        }
        formattedPriceStr.append(new StringBuilder(fraction).reverse());

        formattedPriceStr.insert(0, "Rp. ");

        return String.valueOf(formattedPriceStr);
    }

    @Override
    public byte[] getAssetImage(
            final String sku, final String imageName, final String extension
    ) {

        byte[] image;

        final boolean assetWithSkuExists = assetRepository.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

        if (!assetWithSkuExists) {
            logger.info("Failed to load asset image as SKU does not refer any asset in database");
            return new byte[0];
        }

        File file = new File(
                assetRepository.findByDeletedIsFalseAndSkuEquals(sku).getImageDirectory().concat(File.separator)
                               .concat(imageName).concat(".").concat(extension));

        if (!file.exists()) {
            file = new File(
                    ServiceConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator).concat("image_not_found.jpeg"));
        }

        try {
            image = Files.readAllBytes(file.toPath());
        } catch (IOException | NullPointerException exception) {
            logger.error("Failed to read image as IOException or NullPointerException occurred with message " +
                         exception.getMessage());
            return new byte[0];
        }

        return image;
    }

    /*-------------Save Asset Methods-------------*/
    @Override
    @Caching(evict = {
            @CacheEvict(value = "assetDetailData", key = "#asset.sku"),
            @CacheEvict(value = "availableAssetsList", allEntries = true)
    })
    public void saveAsset(
            final List< MultipartFile > imagesGiven, final String username, final AssetModel asset,
            final boolean addAssetOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException {

        if (!roleDeterminer.determineRole(username).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        }

        AssetModel savedAsset;

        if (addAssetOperation) {
            savedAsset = asset;

            if (assetRepository
                    .existsAssetModelByDeletedIsFalseAndNameAndBrandAndType(savedAsset.getName(), savedAsset.getBrand(),
                                                                            savedAsset.getType()
                    )) {
                throw new DuplicateDataException(DUPLICATE_DATA_FOUND);
            } else {
                savedAsset.setSku(generateSkuCode(username, asset.getBrand(), asset.getType()));
                savedAsset.setDeleted(false);
                savedAsset.setCreatedBy(username);
                savedAsset.setCreatedDate(new Date());
            }
        } else {
            savedAsset = assetRepository.findByDeletedIsFalseAndSkuEquals(asset.getSku());

            if (savedAsset == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            if (savedAsset.equals(asset)) {
                throw new UnauthorizedOperationException(SAME_DATA_ON_UPDATE);
            }

            final boolean nameChanged = !savedAsset.getName().equals(asset.getName());
            final boolean brandChanged = !savedAsset.getBrand().equals(asset.getBrand());
            final boolean typeChanged = !savedAsset.getType().equals(asset.getType());

            if (nameChanged || brandChanged || typeChanged) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            }

            // TODO Check concurrency
            Query query = new Query();
            query.addCriteria(Criteria.where(AssetFieldName.SKU).is(asset.getSku()));
            Update update;
            if (asset.getStock() < savedAsset.getStock()) {
                update = new Update().inc(AssetFieldName.STOCK, 0 - asset.getStock()).set("price", asset.getPrice())
                                     .set("expendable", asset.isExpendable()).set("location", asset.getLocation());
            } else {
                update = new Update().inc(AssetFieldName.STOCK, asset.getStock()).set("price", asset.getPrice())
                                     .set("expendable", asset.isExpendable()).set("location", asset.getLocation());
            }
            savedAsset = mongoOperations
                    .findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), AssetModel.class,
                                   CollectionName.ASSET_COLLECTION_NAME
                    );

            if (savedAsset == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }
        }

        validateAndSaveImages(imagesGiven, addAssetOperation, savedAsset);

        savedAsset.setUpdatedBy(username);
        savedAsset.setUpdatedDate(new Date());

        assetRepository.save(savedAsset);
    }

    /*-------------Delete Asset(s) Method-------------*/
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CacheEvict(value = { "availableAssetsList", "assetDetailData" }, allEntries = true)
    public void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException {

        if (!roleDeterminer.determineRole(username).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        }

        if (skus.isEmpty()) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        List< AssetModel > selectedAssets = new ArrayList<>();

        for (final String sku : skus) {
            final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

            if (asset == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            if (!requestRepository.findAllBySku(sku).isEmpty()) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            selectedAssets.add(asset);
        }

        for (AssetModel selectedAsset : selectedAssets) {
            selectedAsset.setDeleted(true);
        }

        assetRepository.saveAll(selectedAssets);
    }

    private String generateSkuCode(
            final String username, final String brand, final String type
    ) {

        StringBuilder sku;
        LastUniqueIdentifierModel lastUniqueIdentifier;

        if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrandAndType(brand, type)) {
            lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrandAndType(brand, type);

            int lastProductIdCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(16, 21));
            lastProductIdCode++;

            sku = new StringBuilder(lastUniqueIdentifier.getSku().substring(0, 15));
            sku.append(String.format("-%05d", lastProductIdCode));
        } else {
            if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrand(brand)) {
                lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrand(brand);

                int lastTypeCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(10, 15));
                lastTypeCode++;

                sku = new StringBuilder(lastUniqueIdentifier.getSku().substring(0, 9));
                sku.append(String.format("-%05d", lastTypeCode));
                sku.append(String.format("-%05d", 1));
            } else {
                sku = new StringBuilder(ServiceConstant.PREFIX_SKU);

                lastUniqueIdentifier = lastUniqueIdentifierRepository
                        .findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku));

                int lastBrandCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(4, 9));
                lastBrandCode++;

                sku.append(String.format("-%05d", lastBrandCode));
                sku.append(String.format("-%05d", 1));
                sku.append(String.format("-%05d", 1));
            }

            lastUniqueIdentifier = new LastUniqueIdentifierModel();
            lastUniqueIdentifier.setBrand(brand);
            lastUniqueIdentifier.setType(type);
            lastUniqueIdentifier.setCreatedBy(username);
            lastUniqueIdentifier.setCreatedDate(new Date());
        }

        lastUniqueIdentifier.setSku(String.valueOf(sku));
        lastUniqueIdentifier.setUpdatedBy(username);
        lastUniqueIdentifier.setUpdatedDate(new Date());

        lastUniqueIdentifierRepository.save(lastUniqueIdentifier);

        return String.valueOf(sku);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void validateAndSaveImages(
            final List< MultipartFile > imagesGiven, final boolean addAssetOperation, AssetModel savedAsset
    ) {

        boolean rootDirectoryCreated;

        if (!Files.exists(Paths.get(ServiceConstant.ASSET_IMAGE_DIRECTORY))) {
            rootDirectoryCreated = new File(ServiceConstant.ASSET_IMAGE_DIRECTORY).mkdir();
        } else {
            rootDirectoryCreated = true;
        }

        if (rootDirectoryCreated) {
            final Path saveDirectory = Paths
                    .get(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(savedAsset.getSku()));

            if (addAssetOperation) {
                if (!Files.exists(saveDirectory)) {
                    new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(savedAsset.getSku()))
                            .mkdir();
                }
            } else {
                if (Files.exists(saveDirectory)) {
                    final File assetImageFolder = new File(
                            ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(savedAsset.getSku()));
                    final File[] images = assetImageFolder.listFiles();

                    if (images != null) {
                        for (File image : images) {
                            image.delete();
                        }
                        assetImageFolder.delete();
                    }
                }
                new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(savedAsset.getSku()))
                        .mkdir();
            }

            String imageDirectory = ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                         .concat(savedAsset.getSku());
            savedAsset.setImageDirectory(imageDirectory);

            saveImages(imagesGiven, savedAsset.getSku());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveImages(
            final List< MultipartFile > imagesGiven, final String sku
    ) {

        if (imagesGiven.size() != 0) {
            try {
                for (int i = 0; i < imagesGiven.size(); i++) {
                    final Path saveDirectory = Paths
                            .get(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku));

                    if (!Files.exists(saveDirectory)) {
                        new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku)).mkdir();
                    }
                    File image = new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku)
                                                                               .concat(File.separator).concat(sku)
                                                                               .concat("-")
                                                                               .concat(String.valueOf(i + 1))
                                                                               .concat(".").concat(imageHelper
                                                                                                           .getExtensionFromFileName(
                                                                                                                   imagesGiven
                                                                                                                           .get(i)
                                                                                                                           .getOriginalFilename())));

                    imagesGiven.get(i).transferTo(image);
                }
            } catch (IOException exception) {
                logger.error("Failed to save image as IOException occurred with message " + exception.getMessage());
            }
        }
    }

}
