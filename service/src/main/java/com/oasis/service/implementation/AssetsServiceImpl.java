package com.oasis.service.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.webmodel.request.AddAssetRequest;
import com.oasis.webmodel.request.UpdateAssetRequest;
import com.oasis.webmodel.response.success.assets.AssetListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.ASSET_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.EMPTY_SEARCH_QUERY;
import static com.oasis.exception.helper.ErrorCodeAndMessage.MISSING_ASSET_IMAGE;
import static com.oasis.exception.helper.ErrorCodeAndMessage.NO_ASSET_SELECTED;
import static com.oasis.exception.helper.ErrorCodeAndMessage.ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR;
import static com.oasis.exception.helper.ErrorCodeAndMessage.ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR;
import static com.oasis.exception.helper.ErrorCodeAndMessage.SAME_ASSET_EXISTS;
import static com.oasis.exception.helper.ErrorCodeAndMessage.SELECTED_ASSET_STILL_REQUESTED;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetsServiceImpl implements AssetsServiceApi {

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;

    /*-------------Assets List Methods-------------*/
    @Override
    public List<AssetListResponse.Asset> getAvailableAssets(
            final int pageNumber,
            final String sortInfo
    )
            throws DataNotFoundException {

        int foundDataSize = assetRepository.countAllByStockGreaterThan(ServiceConstant.ZERO);

        if (pageNumber < 1 || foundDataSize == 0) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        if ((int) Math.ceil((float) foundDataSize / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        Set<AssetModel> availableAssets = getSortedAvailableAssets(sortInfo, ServiceConstant.ZERO);

        return mapAvailableAssets(availableAssets);
    }

    @Override
    public Set<AssetModel> getSortedAvailableAssets(
            final String sortInfo,
            final long stockLimit
    ) {

        Set<AssetModel> sortedAvailableAssets = new LinkedHashSet<>();

        if (sortInfo.substring(1).equals("assetId")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                sortedAvailableAssets.addAll(assetRepository.findAllByStockGreaterThanOrderBySkuAsc(stockLimit));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                sortedAvailableAssets.addAll(assetRepository.findAllByStockGreaterThanOrderBySkuDesc(stockLimit));
            }
        } else if (sortInfo.substring(1).equals("assetName")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                sortedAvailableAssets.addAll(assetRepository.findAllByStockGreaterThanOrderByNameAsc(stockLimit));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                sortedAvailableAssets.addAll(assetRepository.findAllByStockGreaterThanOrderByNameDesc(stockLimit));
            }
        }

        return sortedAvailableAssets;
    }

    @Override
    public List<AssetListResponse.Asset> getAvailableAssetsBySearchQuery(
            final String searchQuery,
            final int pageNumber,
            final String sortInfo
    )
            throws BadRequestException,
                   DataNotFoundException {

        if (searchQuery.isEmpty()) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY.getErrorCode(), EMPTY_SEARCH_QUERY.getErrorMessage());
        }

        Set<AssetModel> availableAssets = new LinkedHashSet<>();

        if (!searchQuery.contains(" ")) {
            int foundDataSize = assetRepository
                    .countAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCase(
                            searchQuery,
                            searchQuery
                    );

            if (pageNumber < 1 || foundDataSize == 0) {
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            }

            if ((int) Math.ceil((float) foundDataSize / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            }

            availableAssets.addAll(getSortedAvailableAssetsFromSearchQuery(searchQuery, sortInfo));
        } else {
            String[] queries = searchQuery.split(" ");

            for (String query : queries) {
                int foundDataSize = assetRepository
                        .countAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCase(
                                query,
                                query
                        );

                if (pageNumber < 1 || foundDataSize == 0) {
                    throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
                }

                if ((int) Math.ceil((float) foundDataSize / ServiceConstant.ASSETS_FIND_ASSET_PAGE_SIZE) < pageNumber) {
                    throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
                }

                availableAssets.addAll(getSortedAvailableAssetsFromSearchQuery(query, sortInfo));
            }
        }

        return mapAvailableAssets(availableAssets);
    }

    @Override
    public Set<AssetModel> getSortedAvailableAssetsFromSearchQuery(
            final String searchQuery,
            final String sortInfo
    ) {

        Set<AssetModel> sortedAvailableAssets = new LinkedHashSet<>();

        if (sortInfo.substring(1)
                    .equals("assetId")) {
            if (sortInfo.substring(0, 1)
                        .equals("A")) {
                sortedAvailableAssets.addAll(
                        assetRepository
                                .findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1)
                               .equals("D")) {
                sortedAvailableAssets.addAll(
                        assetRepository
                                .findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            }
        } else if (sortInfo.substring(1)
                           .equals("assetName")) {
            if (sortInfo.substring(0, 1)
                        .equals("A")) {
                sortedAvailableAssets.addAll(
                        assetRepository
                                .findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1)
                               .equals("D")) {
                sortedAvailableAssets.addAll(
                        assetRepository
                                .findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            }
        }

        return sortedAvailableAssets;
    }

    @Override
    public List<AssetListResponse.Asset> mapAvailableAssets(
            final Set<AssetModel> availableAssets
    ) {
        List<AssetListResponse.Asset> mappedAvailableAssets = new ArrayList<>();

        for (AssetModel availableAsset : availableAssets) {
            AssetListResponse.Asset asset =
                    new AssetListResponse.Asset(
                            availableAsset.getSku(),
                            availableAsset.getName(),
                            availableAsset.getBrand(),
                            availableAsset.getType(),
                            availableAsset.getLocation(),
                            availableAsset.getStock()
                    );

            mappedAvailableAssets.add(asset);
        }

        return mappedAvailableAssets;
    }

    @Override
    public AssetModel getAssetDetail(
            final String sku
    )
            throws DataNotFoundException {

        AssetModel asset = assetRepository.findBySku(sku);

        if (asset == null) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        return asset;
    }

    @Override
    public byte[] getAssetDetailInPdf(
            final String sku,
            final ClassLoader classLoader
    )
            throws DataNotFoundException {

        AssetModel asset = assetRepository.findBySku(sku);

        if (asset == null) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        Document document = new Document(PageSize.A4, 36, 36, 90, 36);

        if (!Files.exists(Paths.get(sku.concat(".pdf")))) {
            new File(sku.concat(".pdf"));
        }

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(sku.concat(".pdf")));

            FileHeader event = new FileHeader(classLoader);
            writer.setPageEvent(event);

            document.open();

            document.add(Chunk.NEWLINE);

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, new BaseColor(32,162,223));
            Chunk chunk = new Chunk(asset.getName(), font);
            Paragraph paragraph = new Paragraph(chunk);
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);

            document.add(Chunk.NEWLINE);

            File possibleDirectory = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku()));

            if (possibleDirectory.exists()){
                File detailImageFile = null;
                if (possibleDirectory.isDirectory()){
                    for (final File image : Objects.requireNonNull(possibleDirectory.listFiles())){
                        detailImageFile = image;
                        break;  //Takes only first result
                    }
                }

                if (Files.exists(detailImageFile.toPath())){
                    Image detailImage = Image.getInstance(
                            Files.readAllBytes(Paths.get(
                                    Objects.requireNonNull(detailImageFile).toURI())));
                    detailImage.scaleToFit(detailImage.getWidth(), document.getPageSize().getWidth() / 5);
                    detailImage.setAlignment(Element.ALIGN_CENTER);
                    document.add(detailImage);
                }
            } else {
                Paragraph altDetailImageParagraph = new Paragraph("No Image Available!");
                altDetailImageParagraph.setAlignment(Element.ALIGN_CENTER);
                document.add(altDetailImageParagraph);
            }
            document.add(Chunk.NEWLINE);

            PdfPTable detailTable = new PdfPTable(2);
            detailTable.setWidths(new float[] { 2, 7 });

            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.BLACK);
            Paragraph attribute = new Paragraph("Attribute", infoFont);
            attribute.setAlignment(Element.ALIGN_CENTER);
            PdfPCell attrCell = new PdfPCell(attribute);
            attrCell.setPadding(5);
            attrCell.setPaddingBottom(10);

            Paragraph description = new Paragraph("Description", infoFont);
            description.setAlignment(Element.ALIGN_CENTER);
            PdfPCell descCell = new PdfPCell(description);
            descCell.setPadding(5);
            descCell.setPaddingBottom(10);

            detailTable.addCell(attrCell);
            detailTable.addCell(descCell);

            detailTable.addCell("SKU");
            PdfPCell skuCell = new PdfPCell(new Phrase(asset.getSku()));
            skuCell.setPadding(2);
            skuCell.setPaddingLeft(10);
            skuCell.setPaddingBottom(5);
            detailTable.addCell(skuCell);

            detailTable.addCell("Name");
            PdfPCell nameCell = new PdfPCell(new Phrase(asset.getName()));
            nameCell.setPadding(2);
            nameCell.setPaddingLeft(10);
            nameCell.setPaddingBottom(5);
            detailTable.addCell(nameCell);

            detailTable.addCell("Brand");
            PdfPCell brandCell = new PdfPCell(new Phrase(asset.getBrand()));
            brandCell.setPadding(2);
            brandCell.setPaddingLeft(10);
            brandCell.setPaddingBottom(5);
            detailTable.addCell(brandCell);

            detailTable.addCell("Type");
            PdfPCell typeCell = new PdfPCell(new Phrase(asset.getType()));
            typeCell.setPadding(2);
            typeCell.setPaddingLeft(10);
            typeCell.setPaddingBottom(5);
            detailTable.addCell(typeCell);

            detailTable.addCell("Stock");
            PdfPCell stockCell = new PdfPCell(new Phrase(String.valueOf(asset.getStock())));
            stockCell.setPadding(2);
            stockCell.setPaddingLeft(10);
            stockCell.setPaddingBottom(5);
            detailTable.addCell(stockCell);

            detailTable.addCell("Location");
            PdfPCell locationCell = new PdfPCell(new Phrase(asset.getLocation()));
            locationCell.setPadding(2);
            locationCell.setPaddingLeft(10);
            locationCell.setPaddingBottom(5);
            detailTable.addCell(locationCell);

            detailTable.addCell("Price");
            PdfPCell priceCell = new PdfPCell(new Phrase(String.format("Rp. %.2f", asset.getPrice())));
            priceCell.setPadding(2);
            priceCell.setPaddingLeft(10);
            priceCell.setPaddingBottom(5);
            detailTable.addCell(priceCell);

            document.add(detailTable);

            document.close();
        } catch (DocumentException | FileNotFoundException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        } catch (MalformedURLException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        } catch (IOException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        }

        byte[] assetPdf = new byte[20];
        try {
            assetPdf = Files.readAllBytes(Paths.get(sku.concat(".pdf")));
        } catch (IOException e) {
            //TODO throw real exception cause
            e.printStackTrace();
        }

        return assetPdf;
    }

    @Override
    public byte[] getAssetImage(
            final String sku,
            final String photoName,
            final String extension,
            final ClassLoader classLoader
    )
            throws DataNotFoundException {

        byte[] image;
        File file = new File(
                ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(sku).concat("\\").concat(photoName).concat(".")
                                                    .concat(extension));

        try {
            image = Files.readAllBytes(file.toPath());
        } catch (IOException | NullPointerException exception) {
            throw new DataNotFoundException(MISSING_ASSET_IMAGE.getErrorCode(), MISSING_ASSET_IMAGE.getErrorMessage());
        }

        return image;
    }

    /*-------------Add Asset Methods-------------*/
    @Override
    @SuppressWarnings("Duplicates")
    public void addAsset(
            final MultipartFile[] assetPhotos,
            final String rawAssetData
    )
            throws DuplicateDataException,
                   UnauthorizedOperationException,
                   DataNotFoundException {

        String adminNik;
        AddAssetRequest.Asset assetRequest;

        try {
            adminNik = new ObjectMapper().readTree(rawAssetData).path("employeeNik").asText();

            JsonNode asset = new ObjectMapper().readTree(rawAssetData).path("asset");

            assetRequest = new AddAssetRequest.Asset(
                    asset.path("assetName").asText(),
                    asset.path("assetLocation").asText(),
                    asset.path("assetBrand").asText(),
                    asset.path("assetType").asText(),
                    asset.path("assetQty").asLong(),
                    asset.path("assetPrice").asDouble()
            );
        } catch (IOException e) {
            //TODO throw real exception cause
            throw new UnauthorizedOperationException(
                    NO_ASSET_SELECTED.getErrorCode(),
                    NO_ASSET_SELECTED.getErrorMessage()
            );
        }

        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(
                    ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                    ASSET_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage()
            );
        }

        if (assetRepository.existsAssetModelByNameAndBrandAndType(
                assetRequest.getAssetName(), assetRequest.getAssetBrand(), assetRequest.getAssetType()
        )) {
            throw new DuplicateDataException(SAME_ASSET_EXISTS.getErrorCode(), SAME_ASSET_EXISTS.getErrorMessage());
        } else {
            AssetModel asset = new AssetModel();

            asset.setSku(
                    generateAssetSkuCode(
                            assetRequest.getAssetBrand(),
                            assetRequest.getAssetType(),
                            assetRequest.getAssetName()
                    ));
            asset.setName(assetRequest.getAssetName());
            asset.setLocation(assetRequest.getAssetLocation());
            asset.setPrice(assetRequest.getAssetPrice());
            asset.setStock(assetRequest.getAssetQty());
            asset.setBrand(assetRequest.getAssetBrand());
            asset.setType(assetRequest.getAssetType());
            asset.setCreatedBy(adminNik);
            asset.setUpdatedBy(adminNik);
            asset.setCreatedDate(new Date());
            asset.setUpdatedDate(new Date());

            boolean rootDirectoryCreated;
            boolean directoryCreated;

            if (!Files.exists(Paths.get(ServiceConstant.IMAGE_ROOT_DIRECTORY))) {
                rootDirectoryCreated = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY).mkdir();
            } else {
                rootDirectoryCreated = true;
            }

            if(rootDirectoryCreated){
                Path saveDir = Paths.get(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku()));

                if (!Files.exists(saveDir)) {
                    directoryCreated = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku())).mkdir();
                } else {
                    directoryCreated = false;
                }

                if (directoryCreated){
                    String[] imageDirectory = new String[assetPhotos.length];
                    for(int i = 0; i < assetPhotos.length; i++){
                        StringBuilder extensionBuilder = new StringBuilder();
                        extensionBuilder.append(assetPhotos[i].getOriginalFilename());
                        extensionBuilder.reverse();
                        extensionBuilder.replace(
                                0,
                                extensionBuilder.length(),
                                extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf(".") + 1)
                        );
                        extensionBuilder.reverse();
                        imageDirectory[i] = imageDirectory[i] = "http://localhost:8085/oasis/api/assets/" + asset.getSku() +
                                                                "/"+ asset.getSku().concat("-")
                                                                          .concat(String.valueOf(i + 1))
                                                                          .concat(String.valueOf(extensionBuilder));
//                        imageDirectory[i] =
//                                ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku())
//                                                                    .concat(File.separator)
//                                                                    .concat(asset.getSku()).concat("-")
//                                                                    .concat(String.valueOf(i + 1))
//                                                                    .concat(String.valueOf(extensionBuilder));
                    }
                    asset.setImageDirectory(imageDirectory);

                    savePhotos(assetPhotos, asset.getSku());
                }
            }

            assetRepository.save(asset);
        }
    }

    @Override
    public String generateAssetSkuCode(
            final String brand,
            final String type,
            final String name
    ) {

        StringBuilder sku = new StringBuilder();

        sku.append(ServiceConstant.SKU_PREFIX);

        if (assetRepository.existsAssetModelByBrand(brand)) {
            String lastBrandSku = assetRepository.findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku)).getSku();
            int lastBrandCode = Integer.valueOf(lastBrandSku.substring(4, 7));

            sku.append(String.format("-%03d", lastBrandCode));

            int lastTypeCode = Integer.valueOf(lastBrandSku.substring(8, 11));

            if (assetRepository.existsAssetModelByBrandAndType(brand, type)) {
                sku.append(String.format("-%03d", lastTypeCode));

                int lastProductIdCode = Integer.valueOf(lastBrandSku.substring(12, 15));
                sku.append(String.format("-%03d", lastProductIdCode + 1));
            } else {
                sku.append(String.format("-%03d", lastTypeCode + 1));

                sku.append(String.format("-%03d", 1));
            }
        } else {
            String lastBrandSku = assetRepository.findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku)).getSku();
            int lastBrandCode = Integer.valueOf(lastBrandSku.substring(4, 7));

            sku.append(String.format("-%03d", lastBrandCode + 1));
            sku.append(String.format("-%03d", 1));
            sku.append(String.format("-%03d", 1));
        }

        return String.valueOf(sku);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void savePhotos(
            final MultipartFile[] photos,
            final String sku
    ) {

        if (photos.length != 0) {
            try {
                for (int i = 0; i < photos.length; i++) {
                    Path saveDir = Paths.get(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(sku));

                    if (!Files.exists(saveDir)) {
                        new File(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(sku)).mkdir();
                    }

                    StringBuilder extensionBuilder = new StringBuilder();
                    extensionBuilder.append(photos[i].getOriginalFilename());
                    extensionBuilder.reverse();
                    extensionBuilder.replace(
                            0,
                            extensionBuilder.length(),
                            extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf(".") + 1)
                    );
                    extensionBuilder.reverse();
                    File photo = new File(
                            ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(sku).concat(File.separator)
                                                                .concat(sku).concat("-").concat(String.valueOf(i + 1))
                                                                .concat(String.valueOf(extensionBuilder)));

                    photos[i].transferTo(photo);
                }
            } catch (IOException ioException) {
                //TODO throw real exception cause
            }
        }
    }

    /*-------------Update Asset Method-------------*/
    @Override
    @SuppressWarnings("Duplicates")
    public void updateAsset(
            final MultipartFile[] assetPhotos,
            final String rawAssetData
    )
            throws UnauthorizedOperationException,
                   DataNotFoundException {

        String adminNik;
        UpdateAssetRequest.Asset assetRequest;

        try {
            adminNik = new ObjectMapper().readTree(rawAssetData).path("employeeNik").asText();

            JsonNode asset = new ObjectMapper().readTree(rawAssetData).path("asset");

            assetRequest = new UpdateAssetRequest.Asset(
                    asset.path("assetSku").asText(),
                    asset.path("assetName").asText(),
                    asset.path("assetLocation").asText(),
                    asset.path("assetBrand").asText(),
                    asset.path("assetType").asText(),
                    asset.path("assetQty").asLong(),
                    asset.path("assetPrice").asDouble()
            );
        } catch (IOException e){
            //TODO throw real exception cause
            throw new UnauthorizedOperationException(
                    NO_ASSET_SELECTED.getErrorCode(),
                    NO_ASSET_SELECTED.getErrorMessage()
            );
        }

        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(
                    ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                    ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage()
            );
        }

        AssetModel asset = assetRepository.findBySku(assetRequest.getAssetSku());

        if (asset == null) {
            throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
        }

        asset.setName(assetRequest.getAssetName());
        asset.setLocation(assetRequest.getAssetLocation());
        asset.setPrice(assetRequest.getAssetPrice());
        asset.setStock(assetRequest.getAssetQty());
        asset.setBrand(assetRequest.getAssetBrand());
        asset.setType(assetRequest.getAssetType());

        boolean rootDirectoryCreated;
        boolean directoryCreated;

        if (!Files.exists(Paths.get(ServiceConstant.IMAGE_ROOT_DIRECTORY))) {
            rootDirectoryCreated = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY).mkdir();
        } else {
            rootDirectoryCreated = true;
        }

        if(rootDirectoryCreated){
            Path saveDir = Paths.get(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku()));

            if (!Files.exists(saveDir)) {
                directoryCreated = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku())).mkdir();
            } else {
                directoryCreated = false;
            }

            if (directoryCreated){
                String[] imageDirectory = new String[assetPhotos.length];
                for(int i = 0; i < assetPhotos.length; i++){
                    StringBuilder extensionBuilder = new StringBuilder();
                    extensionBuilder.append(assetPhotos[i].getOriginalFilename());
                    extensionBuilder.reverse();
                    extensionBuilder.replace(
                            0,
                            extensionBuilder.length(),
                            extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf(".") + 1)
                    );
                    extensionBuilder.reverse();
                    imageDirectory[i] = imageDirectory[i] = "http://localhost:8085/oasis/api/assets/" + asset.getSku() +
                                                            "/"+ asset.getSku().concat("-")
                                                                      .concat(String.valueOf(i + 1))
                                                                      .concat(String.valueOf(extensionBuilder));
//                    imageDirectory[i] =
//                            ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(asset.getSku())
//                                                                            .concat(File.separator)
//                                                                            .concat(asset.getSku()).concat("-")
//                                                                            .concat(String.valueOf(i + 1))
//                                                                            .concat(String.valueOf(extensionBuilder));
                }
                asset.setImageDirectory(imageDirectory);

                savePhotos(assetPhotos, asset.getSku());
            }
        }
        asset.setUpdatedBy(adminNik);
        asset.setUpdatedDate(new Date());

        assetRepository.save(asset);
    }

    /*-------------Delete Asset(s) Method-------------*/
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteAssets(
            final List<String> skus,
            final String adminNik
    )
            throws UnauthorizedOperationException,
                   BadRequestException,
                   DataNotFoundException {
        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(
                    ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(),
                    ASSET_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage()
            );
        }

        if (skus.isEmpty()) {
            throw new BadRequestException(NO_ASSET_SELECTED.getErrorCode(), NO_ASSET_SELECTED.getErrorMessage());
        }

        List<AssetModel> selectedAssets = new ArrayList<>();

        for (String sku : skus) {
            if (assetRepository.findBySku(sku) == null) {
                throw new DataNotFoundException(ASSET_NOT_FOUND.getErrorCode(), ASSET_NOT_FOUND.getErrorMessage());
            }

            if (!requestRepository.findAllByAssetSku(sku).isEmpty()) {
                throw new BadRequestException(
                        SELECTED_ASSET_STILL_REQUESTED.getErrorCode(),
                        SELECTED_ASSET_STILL_REQUESTED.getErrorMessage()
                );
            }

            selectedAssets.add(assetRepository.findBySku(sku));

            File folder = new File(ServiceConstant.IMAGE_ROOT_DIRECTORY.concat("\\").concat(sku).concat(File.separator)
                                                                  .concat(sku));
            folder.delete();
        }

        //TODO change with deleteAll
        for (AssetModel selectedAsset : selectedAssets) {
            assetRepository.delete(selectedAsset);
        }
    }
}

class FileHeader extends PdfPageEventHelper {
    private PdfPTable table;
    private float tableHeight;

    FileHeader(final ClassLoader loader) {
        table = new PdfPTable(5);
        table.setTotalWidth(523);
        table.setLockedWidth(true);

        Image blibli = null;
        try {
            blibli = Image.getInstance(
                    Files.readAllBytes(Paths.get(
                            Objects.requireNonNull(loader.getResource("pdf_header_images/blibli.png"))
                                   .toURI())));
            } catch (IOException | BadElementException | URISyntaxException e) {
            e.printStackTrace();
        }
        PdfPCell blibliCell = new PdfPCell(blibli, true);
        blibliCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        blibliCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(blibliCell);

        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(Rectangle.NO_BORDER);
        for(int i = 0; i < 3; i++){
            table.addCell(emptyCell);
        }

        Image oasis = null;
        try {
            oasis = Image.getInstance(
                    Files.readAllBytes(Paths.get(
                            Objects.requireNonNull(loader.getResource("pdf_header_images/oasis.png"))
                                   .toURI())));
        } catch (IOException | BadElementException | URISyntaxException e) {
            e.printStackTrace();
        }
        PdfPCell oasisCell = new PdfPCell(oasis, true);
//        PdfPCell oasisCell = new PdfPCell(new Paragraph("OASIS"));
        oasisCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        oasisCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(oasisCell);

        tableHeight = table.getTotalHeight();
    }

    public float getTableHeight() {
        return tableHeight;
    }

    public void onEndPage(PdfWriter writer, Document document) {
        table.writeSelectedRows(0, -1,
                                document.left(),
                                document.top() + ((document.topMargin() + tableHeight) / 2),
                                writer.getDirectContent());
    }
}