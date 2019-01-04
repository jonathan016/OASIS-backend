package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.LastUniqueIdentifierFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.LAST_UNIQUE_IDENTIFIER_COLLECTION_NAME)
public class LastUniqueIdentifierModel
        extends BaseEntity {

    @Field(LastUniqueIdentifierFieldName.SKU)
    private String sku;

    @Field(LastUniqueIdentifierFieldName.BRAND)
    private String brand;

    @Field(LastUniqueIdentifierFieldName.TYPE)
    private String type;

}
