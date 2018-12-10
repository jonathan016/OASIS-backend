package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.AssetFieldName;
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

    public boolean equals(Object object) {

        if (object instanceof AssetModel) {
            return this.sku.equals(((AssetModel) object).getSku()) &&
                   this.name.equals(((AssetModel) object).getName()) &&
                   this.location.equals(((AssetModel) object).getLocation()) &&
                   this.price == ((AssetModel) object).getPrice() && this.stock == ((AssetModel) object).getStock() &&
                   this.brand.equals(((AssetModel) object).getBrand()) &&
                   this.type.equals(((AssetModel) object).getType()) &&
                   this.expendable == ((AssetModel) object).isExpendable();
        }

        return false;
    }

}
