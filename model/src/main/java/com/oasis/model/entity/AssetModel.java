package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.AssetFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.ASSET_COLLECTION_NAME)
public class AssetModel extends BaseEntity {
    @Field(AssetFieldName.ASSET_NAME)
    private String name;

    @Field(AssetFieldName.ASSET_LOCATION)
    private String location;

    @Field(AssetFieldName.ASSET_PRICE)
    private Double price;

    @Field(AssetFieldName.ASSET_STOCK)
    private Integer stock;

    @Field(AssetFieldName.ASSET_BRAND)
    private String brand;

    @Field(AssetFieldName.ASSET_TYPE)
    private String type;
}
