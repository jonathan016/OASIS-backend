package com.oasis.service.implementation.assets;

import com.oasis.model.entity.AssetModel;
import com.oasis.repository.AssetRepository;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.model.constant.service_constant.ImageDirectoryConstant;
import com.oasis.model.constant.service_constant.ServiceConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AssetUtilServiceImpl
        implements AssetUtilServiceApi {

    private Logger logger = LoggerFactory.getLogger(AssetUtilServiceImpl.class);

    @Autowired
    private AssetRepository assetRepository;



    @Override
    public byte[] getAssetImage(final String sku, final String imageName, final String extension) {

        byte[] image;

        final boolean assetWithSkuExists = assetRepository.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

        if (!assetWithSkuExists) {
            logger.info("Failed to load asset image as SKU does not refer any asset in database");
            return new byte[ 0 ];
        } else {
            File file = new File(
                    assetRepository.findByDeletedIsFalseAndSkuEquals(sku).getImageDirectory().concat(File.separator)
                                   .concat(imageName).concat(".").concat(extension));

            if (!file.exists()) {
                file = new File(ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator)
                                                                             .concat("image_not_found.jpeg"));
            }

            try {
                image = Files.readAllBytes(file.toPath());
            } catch (IOException | NullPointerException exception) {
                logger.error("Failed to read image as IOException or NullPointerException occurred with message " +
                             exception.getMessage());
                return new byte[ 0 ];
            }

            return image;
        }
    }

    @Override
    public List< AssetModel > findAllByDeletedIsFalseAndNameContainsIgnoreCase(final String name) {

        return assetRepository.findAllByDeletedIsFalseAndNameContainsIgnoreCase(name);
    }

    @Override
    public boolean existsAssetModelByDeletedIsFalseAndSkuEquals(final String sku) {

        return assetRepository.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);
    }

    @Override
    public long countAllByDeletedIsFalseAndSkuIn(final List< String > skus) {

        return assetRepository.countAllByDeletedIsFalseAndSkuIn(skus);
    }

    public List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanZeroAndSkuIn(final List< String > skus) {

        return assetRepository.findAllByDeletedIsFalseAndStockGreaterThanAndSkuIn(ServiceConstant.ZERO, skus);
    }

    @Override
    public AssetModel findByDeletedIsFalseAndSkuEquals(final String sku) {

        return assetRepository.findByDeletedIsFalseAndSkuEquals(sku);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "assetDetailData",
                        key = "#asset.sku"),
            @CacheEvict(value = "availableAssetsList",
                        allEntries = true)
    })
    public void save(final AssetModel asset) {

        assetRepository.save(asset);
    }

    @Override
    public long countAllByDeletedIsFalseAndStockGreaterThan(final long stock) {

        return assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(stock);
    }

}
