package com.oasis.repository;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends MongoRepository<AssetModel, String> {

    List<AssetModel> findAllByStockGreaterThan(long stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderBySkuAsc(long stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderBySkuDesc(long stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderByNameAsc(long stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderByNameDesc(long stockLimit);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCase(String id, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderBySkuDesc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(String sku, String name);

    AssetModel findBySku(String sku);

    AssetModel findByNameAndBrandAndType(String name, String brand, String type);

    AssetModel save(AssetModel assetModel);

    AssetModel findFirstByBrandOrderBySkuDesc(String brand);

    AssetModel findFirstByBrandAndTypeOrderBySkuDesc(String brand, String type);

    AssetModel findFirstBySkuContainsOrderBySkuDesc(String sku);

    void delete(AssetModel entity);
}
