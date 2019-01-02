package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.RequestFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.REQUEST_COLLECTION_NAME)
public class RequestModel
        extends BaseEntity
        implements Serializable {

    @Field(RequestFieldName.USERNAME)
    private String username;

    @Field(RequestFieldName.SKU)
    private String sku;

    @Field(RequestFieldName.QUANTITY)
    private long quantity;

    @Field(RequestFieldName.STATUS)
    private String status;

    @Field(RequestFieldName.REQUEST_NOTE)
    private String requestNote;

    @Field(RequestFieldName.TRANSACTION_NOTE)
    private String transactionNote;

}
