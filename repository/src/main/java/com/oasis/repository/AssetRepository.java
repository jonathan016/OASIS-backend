package com.oasis.repository;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository
        extends MongoRepository< AssetModel, String > {

    AssetModel findByDeletedIsFalseAndSkuEquals(String sku);

    List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuIn(long stock, List< String > skus);

    long countAllByDeletedIsFalseAndSkuIn(List< String > skus);

    long countAllByDeletedIsFalseAndStockGreaterThan(long stockLimit);

    long countAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCase(
            long stockLimit1, String sku, long stockLimit2, String name
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuAsc(
            long stockLimit, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanOrderBySkuDesc(
            long stockLimit, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanOrderByNameAsc(
            long stockLimit, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanOrderByNameDesc(
            long stockLimit, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuAsc(
            long stockLimit1, String sku, long stockLimit2, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderBySkuDesc(
            long stockLimit1, String sku, long stockLimit2, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameAsc(
            long stockLimit1, String sku, long stockLimit2, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrDeletedIsFalseAndStockGreaterThanAndNameContainsIgnoreCaseOrderByNameDesc(
            long stockLimit1, String sku, long stockLimit2, String name, Pageable pageable
    );

    boolean existsAssetModelByDeletedIsFalseAndNameEqualsAndBrandEqualsAndTypeEquals(
            String name, String brand, String type
    );

    boolean existsAssetModelByDeletedIsFalseAndSkuEquals(String sku);

    List< AssetModel > findAllByDeletedIsFalseAndNameContainsIgnoreCase(String name);

}
