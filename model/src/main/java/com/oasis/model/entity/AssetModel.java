package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.AssetFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.ASSET_COLLECTION_NAME)
public class AssetModel extends BaseEntity {

    @Field(AssetFieldName.SKU)
    private String sku;

    @Field(AssetFieldName.NAME)
    private String name;

    @Field(AssetFieldName.LOCATION)
    private String location;

    @Field(AssetFieldName.PRICE)
    private double price;

    @Field(AssetFieldName.STOCK)
    private long stock;

    @Field(AssetFieldName.BRAND)
    private String brand;

    @Field(AssetFieldName.TYPE)
    private String type;

    @Field(AssetFieldName.EXPENDABLE)
    private boolean expendable;

    @Field(AssetFieldName.IMAGE_DIRECTORY)
    private String imageDirectory;

}
