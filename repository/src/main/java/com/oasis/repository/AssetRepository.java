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

    long countAllByDeletedIsFalseAndStockGreaterThan(long stockLimit);

    long countAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCase(long stockLimit,
            String sku, String name);

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

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
            long stockLimit, String sku, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
            long stockLimit, String sku, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
            long stockLimit, String sku, String name, Pageable pageable
    );

    Page< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
            long stockLimit, String sku, String name, Pageable pageable
    );

    List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(
            long stockLimit, String sku, String name
    );

    List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(
            long stockLimit, String sku, String name
    );

    List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
            long stockLimit, String sku, String name
    );

    List< AssetModel > findAllByDeletedIsFalseAndStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
            long stockLimit, String sku, String name
    );

    boolean existsAssetModelByDeletedIsFalseAndNameAndBrandAndType(
            String name, String brand, String type
    );

    boolean existsAssetModelByDeletedIsFalseAndSkuEquals(String sku);

}
