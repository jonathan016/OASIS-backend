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

    @Field(RequestFieldName.REQUEST_EMPLOYEE_ID)
    private String employeeNik;

    @Field(RequestFieldName.REQUEST_ASSET_ID)
    private String assetId;

    @Field(RequestFieldName.REQUEST_ASSET_QUANTITY)
    private int assetQuantity;

    @Field(RequestFieldName.REQUEST_STATUS)
    private String status;

    @Field(RequestFieldName.REQUEST_REQUEST_NOTE)
    private String requestNote;

    @Field(RequestFieldName.REQUEST_TRANSACTION_NOTE)
    private String transactionNote;
}
