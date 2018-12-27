package com.oasis.service.api.assets;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssetUtilServiceApi {

    byte[] getAssetImage(final String sku, final String imageName, final String extension);

    List< AssetModel > findAllByDeletedIsFalseAndNameContainsIgnoreCase(final String name);

    boolean existsAssetModelByDeletedIsFalseAndSkuEquals(final String sku);

    long countAllByDeletedIsFalseAndSkuIn(final List< String > skus);

    Page< AssetModel > findAllByDeletedIsFalseAndSkuIn(final List< String > skus, final Pageable pageable);

    AssetModel findByDeletedIsFalseAndSkuEquals(final String sku);

    void save(final AssetModel asset);

    long countAllByDeletedIsFalseAndStockGreaterThan(final long stock);

}