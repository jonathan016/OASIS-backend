package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.RequestFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.REQUEST_COLLECTION_NAME)
public class RequestModel extends BaseEntity {

    @Field(RequestFieldName.EMPLOYEE_NIK)
    private String employeeNik;

    @Field(RequestFieldName.ASSET_SKU)
    private String assetSku;

    @Field(RequestFieldName.ASSET_QUANTITY)
    private int assetQuantity;

    @Field(RequestFieldName.STATUS)
    private String status;

    @Field(RequestFieldName.REQUEST_NOTE)
    private String requestNote;

    @Field(RequestFieldName.TRANSACTION_NOTE)
    private String transactionNote;
}
