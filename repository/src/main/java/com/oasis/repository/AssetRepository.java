package com.oasis.repository;

import com.oasis.model.entity.AssetModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends MongoRepository<AssetModel, String> {

    List<AssetModel> findAllByStockGreaterThan(int stockLimit);

    AssetModel findBy_id(String assetId);
}
