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
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.LastUniqueIdentifierModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.LastUniqueIdentifierRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.service.DocumentHeader;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;
import static java.util.Objects.requireNonNull;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsServiceImpl
        implements AssetsServiceApi {

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private DocumentHeader documentHeader;
    @Autowired
    private LastUniqueIdentifierRepository lastUniqueIdentifierRepository;

    /*-------------Assets List Methods-------------*/
    @Override
    public List< AssetModel > getAvailableAssetsList(
            final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (query != null && query.equals("defaultQuery")) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY);
        }

        if (query == null) {
            int foundDataSize = assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);

            if (page < 1 || foundDataSize == 0 || (int) Math.ceil(
                    (double) getAvailableAssetsCount(query, sort) / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) <
                                                  page) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            return new ArrayList<>(getSortedAvailableAssets(page, sort, ServiceConstant.ZERO));
        } else {
            //            Set<AssetModel> availableAssets = new LinkedHashSet<>();

            if (page < 1 || (int) Math.ceil(
                    (double) getAvailableAssetsCount(query, sort) / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) <
                            page) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            return new ArrayList<>(getSortedAvailableAssetsFromQuery(page, query, sort, ServiceConstant.ZERO));

            //            String[] queries = query.split(ServiceConstant.SPACE);
            //
            //            for (String word : queries) {
            //                int foundDataSize = assetRepository
            //                        .countAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCase(
            //                                word,
            //                                word
            //                        );
            //
            //                if (foundDataSize == 0) {
            //                    continue;
            //                }
            //
            //                availableAssets.addAll(getSortedAvailableAssetsFromQuery(page, word, sort));
            //            }
            //
            //            List<AssetModel> assetsList = new ArrayList<>(availableAssets);
            //
            //            if (sort.startsWith(ServiceConstant.ASCENDING)) {
            //                assetsList.sort(Comparator.comparing(AssetModel::getSku));
            //            } else if (sort.startsWith(ServiceConstant.DESCENDING)){
            //                assetsList.sort(Comparator.comparing(AssetModel::getSku).reversed());
            //            } else {
            //                return new ArrayList<>();
            //            }
            //
            //            return assetsList;
        }
    }

    @Override
    public Set< AssetModel > getSortedAvailableAssets(
            final int page, final String sort, final long stockLimit
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        if (sort.substring(2)
                .equals("SKU")) {
            if (sort.substring(0, 1)
                    .equals(ServiceConstant.ASCENDING)) {
                sortedAvailableAssets.addAll(
                        assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuAsc(stockLimit,
                                                                                                PageRequest.of(page - 1,
                                                                                                               ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE
                                                                                                )
                        )
                                       .getContent());
            } else {
                sortedAvailableAssets.addAll(
                        assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuDesc(stockLimit,
                                                                                                 PageRequest.of(
                                                                                                         page - 1,
                                                                                                         ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE
                                                                                                 )
                        )
                                       .getContent());
            }
        } else {
            if (sort.substring(2)
                    .equals("name")) {
                if (sort.substring(0, 1)
                        .equals(ServiceConstant.ASCENDING)) {
                    sortedAvailableAssets.addAll(
                            assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameAsc(stockLimit,
                                                                                                     PageRequest.of(
                                                                                                             page - 1,
                                                                                                             ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE
                                                                                                     )
                            )
                                           .getContent());
                } else {
                    sortedAvailableAssets.addAll(
                            assetRepository.findAllByDeletedIsFalseAndStockGreaterThanOrderByNameDesc(stockLimit,
                                                                                                      PageRequest.of(
                                                                                                              page - 1,
                                                                                                              ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE
                                                                                                      )
                            )
                                           .getContent());
                }
            }
        }

        return sortedAvailableAssets;
    }

    @Override
    public Set< AssetModel > getSortedAvailableAssetsFromQuery(
            final int page, final String query, final String sort, final long stockLimit
    ) {

        Set< AssetModel > sortedAvailableAssets = new LinkedHashSet<>();

        if (page == -1) {
            if (sort.substring(2)
                    .equals("SKU")) {
                if (sort.substring(0, 1)
                        .equals(ServiceConstant.ASCENDING)) {
                    sortedAvailableAssets.addAll(
                            assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
                                    stockLimit, query, query));
                } else {
                    if (sort.substring(0, 1)
                            .equals(ServiceConstant.DESCENDING)) {
                        sortedAvailableAssets.addAll(
                                assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
                                        stockLimit, query, query));
                    }
                }
            } else {
                if (sort.substring(2)
                        .equals("name")) {
                    if (sort.substring(0, 1)
                            .equals(ServiceConstant.ASCENDING)) {
                        sortedAvailableAssets.addAll(
                                assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                        stockLimit, query, query));
                    } else {
                        if (sort.substring(0, 1)
                                .equals(ServiceConstant.DESCENDING)) {
                            sortedAvailableAssets.addAll(
                                    assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                                            stockLimit, query, query));
                        }
                    }
                }
            }
        } else {
            if (sort.substring(2)
                    .equals("SKU")) {
                if (sort.substring(0, 1)
                        .equals(ServiceConstant.ASCENDING)) {
                    sortedAvailableAssets.addAll(
                            assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
                                    stockLimit, query, query,
                                    PageRequest.of(page - 1, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE)
                            )
                                           .getContent());
                } else {
                    if (sort.substring(0, 1)
                            .equals(ServiceConstant.DESCENDING)) {
                        sortedAvailableAssets.addAll(
                                assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
                                        stockLimit, query, query,
                                        PageRequest.of(page - 1, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE)
                                )
                                               .getContent());
                    }
                }
            } else {
                if (sort.substring(2)
                        .equals("name")) {
                    if (sort.substring(0, 1)
                            .equals(ServiceConstant.ASCENDING)) {
                        sortedAvailableAssets.addAll(
                                assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                        stockLimit, query, query,
                                        PageRequest.of(page - 1, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE)
                                )
                                               .getContent());
                    } else {
                        if (sort.substring(0, 1)
                                .equals(ServiceConstant.DESCENDING)) {
                            sortedAvailableAssets.addAll(
                                    assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                                            stockLimit, query, query,
                                            PageRequest.of(page - 1, ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE)
                                    )
                                                   .getContent());
                        }
                    }
                }
            }
        }

        return sortedAvailableAssets;
    }

    @Override
    public int getAvailableAssetsCount(
            final String query, final String sort
    ) {

        if (query == null) {
            return assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        } else {
            return getSortedAvailableAssetsFromQuery(-1, query, sort, ServiceConstant.ZERO).size();
        }
    }

    @Override
    public AssetModel getAssetDetailData(
            final String sku
    )
            throws
            DataNotFoundException {

        AssetModel assetDetailData = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        if (assetDetailData == null) {
            throw new DataNotFoundException(ASSET_NOT_FOUND);
        }

        return assetDetailData;
    }

    @Override
    public List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    ) {

        List< String > images = new ArrayList<>();
        if (imageDirectory == null || imageDirectory.isEmpty()) {
            return images;
        }

        File directory = new File(imageDirectory);
        if (Files.exists(directory.toPath())) {
            int i = 0;
            for (final File image : requireNonNull(directory.listFiles())) {
                StringBuilder extensionBuilder = new StringBuilder();
                extensionBuilder.append(image.getName());
                extensionBuilder.reverse();
                extensionBuilder.replace(0, extensionBuilder.length(),
                                         extensionBuilder.substring(0, String.valueOf(extensionBuilder)
                                                                             .indexOf("."))
                );
                extensionBuilder.reverse();
                images.add("http://localhost:8085/oasis/api/assets/" + sku + "/" + sku.concat("-")
                                                                                      .concat(String.valueOf(++i))
                                                                                      .concat("?extension=")
                                                                                      .concat(String.valueOf(
                                                                                              extensionBuilder)));
            }
        }

        return images;

    }

    @Override
    public byte[] getAssetDetailInPdf(
            final String sku, final ClassLoader classLoader
    )
            throws
            DataNotFoundException {

        AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(sku);

        //TODO remove throw, replace with return new byte[0]
        if (asset == null) {
            throw new DataNotFoundException(ASSET_NOT_FOUND);
        }

        Document document = new Document(PageSize.A4, 36, 36, 90, 36);

        if (!Files.exists(Paths.get(sku.concat(ServiceConstant.PDF_EXTENSION)))) {
            new File(sku.concat(ServiceConstant.PDF_EXTENSION));
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document,
                                                     new FileOutputStream(sku.concat(ServiceConstant.PDF_EXTENSION))
            );

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

            if (possibleDirectory.exists()) {
                if (possibleDirectory.isDirectory()) {
                    //noinspection LoopStatementThatDoesntLoop
                    for (final File image : requireNonNull(possibleDirectory.listFiles())) {
                        if (Files.exists(image.toPath())) {
                            Image detailImage = Image.getInstance(
                                    Files.readAllBytes(Paths.get(requireNonNull(image).toURI())));
                            detailImage.scaleToFit(detailImage.getWidth(), document.getPageSize()
                                                                                   .getWidth() / 5);
                            detailImage.setAlignment(Element.ALIGN_CENTER);
                            document.add(detailImage);
                        }

                        break;  //Takes only first result, but code is prepared for multiple images
                    }
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
            addContentCellToTable(detailTable, "Price", String.format("Rp. %.2f", asset.getPrice()));

            document.add(detailTable);

            document.close();
        } catch (DocumentException | IOException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        }

        byte[] assetPdf = new byte[100];
        try {
            assetPdf = Files.readAllBytes(Paths.get(sku.concat(ServiceConstant.PDF_EXTENSION)));
        } catch (IOException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        }

        return assetPdf;
    }

    @Override
    public void addHeaderCellToTable(
            PdfPTable table, final String value, final Font font
    ) {

        Paragraph description = new Paragraph(value, font);
        description.setAlignment(Element.ALIGN_CENTER);

        PdfPCell headerCell = new PdfPCell(description);
        headerCell.setPadding(5);
        headerCell.setPaddingBottom(10);

        table.addCell(headerCell);
    }

    @Override
    public void addContentCellToTable(
            PdfPTable table, final String name, final String data
    ) {

        table.addCell(name);

        PdfPCell contentCell = new PdfPCell(new Phrase(data));
        contentCell.setPadding(2);
        contentCell.setPaddingLeft(10);
        contentCell.setPaddingBottom(5);

        table.addCell(contentCell);
    }

    @Override
    public byte[] getAssetImage(
            final String sku, final String imageName, final String extension
    ) {

        byte[] image;

        //TODO add validation asset with SKU exists
        File file = new File(assetRepository.findByDeletedIsFalseAndSkuEquals(sku)
                                            .getImageDirectory()
                                            .concat(File.separator)
                                            .concat(imageName)
                                            .concat(".")
                                            .concat(extension));

        if (!file.exists()) {
            file = new File(ServiceConstant.RESOURCE_IMAGE_DIRECTORY.concat(File.separator)
                                                                    .concat("image_not_found.jpeg"));
        }

        try {
            image = Files.readAllBytes(file.toPath());
        } catch (IOException | NullPointerException exception) {
            image = new byte[0];
        }

        return image;
    }

    /*-------------Save Asset Methods-------------*/
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void saveAsset(
            final List< MultipartFile > photos, final String username, final AssetModel asset,
            final boolean isAddOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        if (!roleDeterminer.determineRole(username)
                           .equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);
        }

        AssetModel savedAsset;
        if (isAddOperation) {
            savedAsset = asset;

            if (photos.isEmpty()) {
                throw new BadRequestException(MISSING_ASSET_IMAGE);
            }

            if (assetRepository.existsAssetModelByDeletedIsFalseAndNameAndBrandAndType(savedAsset.getName(),
                                                                                       savedAsset.getBrand(),
                                                                                       savedAsset.getType()
            )) {
                throw new DuplicateDataException(DUPLICATE_ASSET_DATA_FOUND);
            } else {
                savedAsset.setSku(generateSkuCode(username, asset.getBrand(), asset.getType()));
                savedAsset.setDeleted(false);
                savedAsset.setCreatedBy(username);
                savedAsset.setCreatedDate(new Date());
            }
        } else {
            savedAsset = assetRepository.findByDeletedIsFalseAndSkuEquals(asset.getSku());

            if (savedAsset == null) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            savedAsset.setName(asset.getName());
            savedAsset.setBrand(asset.getBrand());
            savedAsset.setType(asset.getType());
            savedAsset.setStock(asset.getStock());
            savedAsset.setPrice(asset.getPrice());
            savedAsset.setExpendable(asset.isExpendable());
            savedAsset.setLocation(asset.getLocation());
        }

        if (!photos.isEmpty()) {
            boolean rootDirectoryCreated;

            if (!Files.exists(Paths.get(ServiceConstant.ASSET_IMAGE_DIRECTORY))) {
                rootDirectoryCreated = new File(ServiceConstant.ASSET_IMAGE_DIRECTORY).mkdir();
            } else {
                rootDirectoryCreated = true;
            }

            if (rootDirectoryCreated) {
                Path saveDir = Paths.get(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                              .concat(savedAsset.getSku()));

                if (isAddOperation) {
                    if (!Files.exists(saveDir)) {
                        //noinspection ResultOfMethodCallIgnored
                        new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                      .concat(savedAsset.getSku())).mkdir();
                    }
                } else {
                    if (Files.exists(saveDir)) {
                        File assetImageFolder = new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                                              .concat(savedAsset.getSku()));
                        for (File image : requireNonNull(assetImageFolder.listFiles())) {
                            image.delete();
                        }
                        assetImageFolder.delete();
                    }
                    new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                  .concat(savedAsset.getSku())).mkdir();
                }

                String imageDirectory = ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                             .concat(savedAsset.getSku());
                savedAsset.setImageDirectory(imageDirectory);

                savePhotos(photos, savedAsset.getSku());
            }
        }
        savedAsset.setUpdatedBy(username);
        savedAsset.setUpdatedDate(new Date());

        assetRepository.save(savedAsset);
    }

    @Override
    public String generateSkuCode(
            final String username, final String brand, final String type
    ) {

        StringBuilder sku;
        LastUniqueIdentifierModel lastUniqueIdentifier;

        if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrandAndType(brand, type)) {
            lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrandAndType(brand, type);

            int lastProductIdCode = Integer.parseInt(lastUniqueIdentifier.getSku()
                                                                         .substring(12, 15));
            lastProductIdCode++;

            sku = new StringBuilder(lastUniqueIdentifier.getSku()
                                                        .substring(0, 11));
            sku.append(String.format("-%03d", lastProductIdCode));
        } else {
            if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrand(brand)) {
                lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrand(brand);

                int lastTypeCode = Integer.parseInt(lastUniqueIdentifier.getSku()
                                                                        .substring(8, 11));
                lastTypeCode++;

                sku = new StringBuilder(lastUniqueIdentifier.getSku()
                                                            .substring(0, 7));
                sku.append(String.format("-%03d", lastTypeCode));
                sku.append(String.format("-%03d", 1));
            } else {
                sku = new StringBuilder(ServiceConstant.SKU_PREFIX);

                lastUniqueIdentifier = lastUniqueIdentifierRepository.findFirstBySkuContainsOrderBySkuDesc(
                        String.valueOf(sku));

                int lastBrandCode = Integer.parseInt(lastUniqueIdentifier.getSku()
                                                                         .substring(4, 7));
                lastBrandCode++;

                sku.append(String.format("-%03d", lastBrandCode));
                sku.append(String.format("-%03d", 1));
                sku.append(String.format("-%03d", 1));
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

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void savePhotos(
            final List< MultipartFile > photos, final String sku
    ) {

        if (photos.size() != 0) {
            try {
                for (int i = 0; i < photos.size(); i++) {
                    Path saveDir = Paths.get(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                                  .concat(sku));

                    if (!Files.exists(saveDir)) {
                        new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                      .concat(sku)).mkdir();
                    }

                    StringBuilder extensionBuilder = new StringBuilder();
                    extensionBuilder.append(photos.get(i)
                                                  .getOriginalFilename());
                    extensionBuilder.reverse();
                    extensionBuilder.replace(0, extensionBuilder.length(),
                                             extensionBuilder.substring(0, String.valueOf(extensionBuilder)
                                                                                 .indexOf(".") + 1)
                    );
                    extensionBuilder.reverse();
                    File photo = new File(ServiceConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                               .concat(sku)
                                                                               .concat(File.separator)
                                                                               .concat(sku)
                                                                               .concat("-")
                                                                               .concat(String.valueOf(i + 1))
                                                                               .concat(String.valueOf(
                                                                                       extensionBuilder)));

                    photos.get(i)
                          .transferTo(photo);
                }
            } catch (IOException ioException) {
                //TODO throw real exception cause
            }
        }
    }

    /*-------------Delete Asset(s) Method-------------*/
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException {

        if (!roleDeterminer.determineRole(username)
                           .equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);
        }

        if (skus.isEmpty()) {
            throw new BadRequestException(NO_ASSET_SELECTED);
        }

        List< AssetModel > selectedAssets = new ArrayList<>();

        for (String sku : skus) {
            if (assetRepository.findByDeletedIsFalseAndSkuEquals(sku) == null) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            if (!requestRepository.findAllBySku(sku)
                                  .isEmpty()) {
                throw new BadRequestException(SELECTED_ASSET_STILL_REQUESTED);
            }

            selectedAssets.add(assetRepository.findByDeletedIsFalseAndSkuEquals(sku));
        }

        for (AssetModel selectedAsset : selectedAssets) {
            selectedAsset.setDeleted(true);
        }

        assetRepository.saveAll(selectedAssets);
    }

}
