package com.oasis.repository;

import com.oasis.model.entity.LastUniqueIdentifierModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastUniqueIdentifierRepository extends MongoRepository<LastUniqueIdentifierModel, String> {

    LastUniqueIdentifierModel findByBrand(String brand);

    boolean existsLastUniqueIdentifierModelByBrand(String brand);

    boolean existsLastUniqueIdentifierModelByBrandAndType(String brand, String type);

    LastUniqueIdentifierModel findByBrandAndType(String brand, String type);

    LastUniqueIdentifierModel findFirstBySkuContainsOrderBySkuDesc(String sku);

    boolean existsLastUniqueIdentifierModelBySku(String sku);

}
