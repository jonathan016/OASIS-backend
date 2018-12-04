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

    int countAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCase(String sku, String name);

    Page<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(String sku, String name,
                                                                                         Pageable pageable);

    Page<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(String sku, String name,
                                                                                          Pageable pageable);

    Page<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(String sku, String name,
                                                                                          Pageable pageable);

    Page<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(String sku, String name,
                                                                                           Pageable pageable);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(String sku, String name);

    boolean existsAssetModelByNameAndBrandAndType(String name, String brand, String type);

}
