package com.oasis.repository;

import com.oasis.model.entity.LastUniqueIdentifierModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastUniqueIdentifierRepository
        extends MongoRepository< LastUniqueIdentifierModel, String > {

    LastUniqueIdentifierModel findByBrandEquals(String brand);

    boolean existsLastUniqueIdentifierModelByBrandEquals(String brand);

    boolean existsLastUniqueIdentifierModelByBrandEqualsAndTypeEquals(
            String brand, String type
    );

    LastUniqueIdentifierModel findByBrandEqualsAndTypeEquals(
            String brand, String type
    );

    LastUniqueIdentifierModel findFirstBySkuContainsOrderBySkuDesc(String sku);

}
