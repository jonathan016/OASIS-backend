package com.oasis.repository;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends MongoRepository<AssetModel, String> {

    List<AssetModel> findAllByStockGreaterThan(int stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderBySkuAsc(int stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderBySkuDesc(int stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderByNameAsc(int stockLimit);

    List<AssetModel> findAllByStockGreaterThanOrderByNameDesc(int stockLimit);

    List<AssetModel> findAllBySkuContainsOrNameContains(String id, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderBySkuAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderBySkuDesc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderByNameAsc(String sku, String name);

    List<AssetModel> findAllBySkuContainsOrNameContainsOrderByNameDesc(String sku, String name);

    AssetModel findBySku(String sku);
}
