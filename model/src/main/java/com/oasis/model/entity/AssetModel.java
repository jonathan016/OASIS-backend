package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.FieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.ASSET_COLLECTION_NAME)
public class AssetModel extends BaseEntity {
    @Field(FieldName.ASSET_NAME)
    private String name;

    @Field(FieldName.ASSET_LOCATION)
    private String location;

    @Field(FieldName.ASSET_PRICE)
    private Double price;

    @Field(FieldName.ASSET_STOCK)
    private Integer stock;

    @Field(FieldName.ASSET_BRAND)
    private String brand;

    @Field(FieldName.ASSET_TYPE)
    private String type;
}
