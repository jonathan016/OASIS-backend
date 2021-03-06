package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.AssetFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.ASSET_COLLECTION_NAME)
public class AssetModel
        extends BaseEntity
        implements Serializable {

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

    @Field(AssetFieldName.DELETED)
    private boolean deleted;

}
