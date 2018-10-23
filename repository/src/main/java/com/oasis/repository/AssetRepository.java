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

    List<AssetModel> findAllBySkuContainsOrNameContains(String id, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderBySkuAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderBySkuDesc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderByNameAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderByNameDesc(String sku, String name);

    AssetModel findBySku(String sku);

    AssetModel findByNameAndBrandAndType(String name, String brand, String type);

    AssetModel save(AssetModel assetModel);

    AssetModel findFirstByBrandAndTypeOrderBySkuDesc(String brand, String type);
}
