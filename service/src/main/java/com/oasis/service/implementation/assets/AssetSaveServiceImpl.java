package com.oasis.service.implementation.assets;

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
import com.oasis.service.api.assets.AssetSaveServiceApi;
import com.oasis.tool.constant.ImageDirectoryConstant;
import com.oasis.tool.constant.PrefixConstant;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.util.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.DUPLICATE_DATA_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;
import static com.oasis.exception.helper.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetSaveServiceImpl
        implements AssetSaveServiceApi {

    private Logger logger = LoggerFactory.getLogger(AssetUtilServiceImpl.class);

    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private LastUniqueIdentifierRepository lastUniqueIdentifierRepository;

    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private ImageHelper imageHelper;



    @Override
    @Caching(evict = {
            @CacheEvict(value = "assetDetailData",
                        key = "#asset.sku"),
            @CacheEvict(value = "availableAssetsList",
                        allEntries = true)
    })
    public void saveAsset(
            final List< MultipartFile > imagesGiven, final String username, final AssetModel asset,
            final boolean addAssetOperation
    )
            throws
            DuplicateDataException,
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        AssetModel savedAsset;

        if (!isSaveAssetParametersProper(imagesGiven, asset, addAssetOperation)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (addAssetOperation) {
                savedAsset = asset;

                if (assetRepository.existsAssetModelByDeletedIsFalseAndNameEqualsAndBrandEqualsAndTypeEquals(
                        savedAsset.getName(),
                        savedAsset.getBrand(), savedAsset.getType()
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
                } else if (savedAsset.equals(asset)) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    final boolean nameChanged = !savedAsset.getName().equals(asset.getName());
                    final boolean brandChanged = !savedAsset.getBrand().equals(asset.getBrand());
                    final boolean typeChanged = !savedAsset.getType().equals(asset.getType());

                    if (nameChanged || brandChanged || typeChanged) {
                        throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                    } else {
                        Query query = new Query();
                        query.addCriteria(Criteria.where(AssetFieldName.SKU).is(asset.getSku()));
                        Update update;

                        update = new Update().inc(AssetFieldName.STOCK, asset.getStock() - savedAsset.getStock())
                                             .set("price", asset.getPrice()).set("expendable", asset.isExpendable())
                                             .set("location", asset.getLocation());

                        savedAsset = mongoOperations.findAndModify(
                                query, update, FindAndModifyOptions.options()
                                                                   .returnNew(true), AssetModel.class,
                                CollectionName.ASSET_COLLECTION_NAME
                        );

                        if (savedAsset == null) {
                            throw new DataNotFoundException(DATA_NOT_FOUND);
                        } else if (savedAsset.getStock() < 1) {
                            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                        }
                    }
                }
            }

            validateAndSaveImages(imagesGiven, addAssetOperation, savedAsset);

            savedAsset.setUpdatedBy(username);
            savedAsset.setUpdatedDate(new Date());

            assetRepository.save(savedAsset);
        }
    }

    @SuppressWarnings({ "ConstantConditions", "RedundantIfStatement" })
    private boolean isSaveAssetParametersProper(
            final List< MultipartFile > imagesGiven, final AssetModel asset, final boolean addAssetOperation
    ) {

        for (final MultipartFile image : imagesGiven) {
            try {
                if (!image.getOriginalFilename().matches(Regex.REGEX_JPEG_FILE_NAME) &&
                    !image.getOriginalFilename().matches(Regex.REGEX_PNG_FILE_NAME)) {
                    return false;
                }
            } catch (NullPointerException exception) {
                return false;
            }
        }

        if (asset == null) {
            return false;
        } else {
            if (!addAssetOperation && asset.getSku() == null) {
                return false;
            } else if (asset.getName() == null) {
                return false;
            } else if (asset.getLocation() == null) {
                return false;
            } else if (asset.getBrand() == null) {
                return false;
            } else if (asset.getType() == null) {
                return false;
            } else if (!asset.getName().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (!asset.getLocation().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (asset.getPrice() < 100) {
                return false;
            } else if (asset.getStock() < 1) {
                return false;
            } else if (!asset.getBrand().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (!asset.getType().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private String generateSkuCode(
            final String username, final String brand, final String type
    ) {

        StringBuilder sku;
        LastUniqueIdentifierModel lastUniqueIdentifier;

        if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(brand, type)) {
            lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrandEqualsAndTypeEquals(brand, type);

            int lastProductIdCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(12, 15));
            lastProductIdCode++;

            sku = new StringBuilder(lastUniqueIdentifier.getSku().substring(0, 11));
            sku.append(String.format("-%03d", lastProductIdCode));
        } else {
            if (lastUniqueIdentifierRepository.existsLastUniqueIdentifierModelByBrandEquals(brand)) {
                lastUniqueIdentifier = lastUniqueIdentifierRepository.findByBrandEquals(brand);

                int lastTypeCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(8, 11));
                lastTypeCode++;

                sku = new StringBuilder(lastUniqueIdentifier.getSku().substring(0, 7));
                sku.append(String.format("-%03d", lastTypeCode));
                sku.append(String.format("-%03d", 1));
            } else {
                sku = new StringBuilder(PrefixConstant.PREFIX_SKU);

                lastUniqueIdentifier = lastUniqueIdentifierRepository
                        .findFirstBySkuContainsOrderBySkuDesc(String.valueOf(sku));

                int lastBrandCode = Integer.parseInt(lastUniqueIdentifier.getSku().substring(4, 7));
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void validateAndSaveImages(
            final List< MultipartFile > imagesGiven, final boolean addAssetOperation, AssetModel savedAsset
    ) {

        final boolean rootDirectoryCreated;

        if (!Files.exists(Paths.get(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY))) {
            rootDirectoryCreated = new File(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY).mkdir();
        } else {
            rootDirectoryCreated = true;
        }

        if (rootDirectoryCreated) {
            final Path saveDirectory = Paths.get(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                                             .concat(savedAsset
                                                                                                             .getSku()));

            if (addAssetOperation) {
                if (!Files.exists(saveDirectory)) {
                    new File(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                         .concat(savedAsset.getSku())).mkdir();
                }
            } else {
                if (Files.exists(saveDirectory)) {
                    final File assetImageFolder = new File(
                            ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
                                                                        .concat(savedAsset.getSku()));
                    final File[] images = assetImageFolder.listFiles();

                    if (images != null) {
                        for (File image : images) {
                            image.delete();
                        }
                        assetImageFolder.delete();
                    }
                }

                new File(
                        ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(savedAsset.getSku()))
                        .mkdir();
            }

            String imageDirectory = ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator)
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
                            .get(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku));

                    if (!Files.exists(saveDirectory)) {
                        new File(ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku))
                                .mkdir();
                    }

                    File image = new File(
                            ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY.concat(File.separator).concat(sku)
                                                                        .concat(File.separator).concat(sku).concat("-")
                                                                        .concat(String.valueOf(i + 1)).concat(".")
                                                                        .concat(imageHelper.getExtensionFromFileName(
                                                                                imagesGiven.get(i)
                                                                                           .getOriginalFilename())));

                    imagesGiven.get(i).transferTo(image);
                }
            } catch (IOException exception) {
                logger.error("Failed to save image as IOException occurred with message " + exception.getMessage());
            }
        }
    }

}
