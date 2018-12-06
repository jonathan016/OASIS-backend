package com.oasis.repository;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends MongoRepository<AssetModel, String> {

    AssetModel findBySku(String sku);

    int countAllByStockGreaterThan(long stockLimit);

    Page<AssetModel> findAllByStockGreaterThanOrderBySkuAsc(long stockLimit, Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanOrderBySkuDesc(long stockLimit, Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanOrderByNameAsc(long stockLimit, Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanOrderByNameDesc(long stockLimit, Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(long stockLimit, String sku, String name,
                                                                                                            Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(long stockLimit, String sku, String name,
                                                                                                             Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(long stockLimit, String sku, String name,
                                                                                                             Pageable pageable);

    Page<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(long stockLimit, String sku, String name,
                                                                                                              Pageable pageable);

    List<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(long stockLimit, String sku, String name);

    List<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(long stockLimit, String sku, String name);

    List<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(long stockLimit, String sku, String name);

    List<AssetModel> findAllByStockGreaterThanAndSkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(long stockLimit, String sku, String name);

    boolean existsAssetModelByNameAndBrandAndType(String name, String brand, String type);

    boolean existsAssetModelBySku(String sku);

}
