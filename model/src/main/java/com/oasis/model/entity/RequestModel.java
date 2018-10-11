package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.FieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.REQUEST_COLLECTION_NAME)
public class RequestModel extends BaseEntity {
    @Field(FieldName.REQUEST_EMPLOYEE_ID)
    private String employeeId;

    @Field(FieldName.REQUEST_ASSET_ID)
    private String assetId;

    @Field(FieldName.REQUEST_ASSET_QUANTITY)
    private Integer assetQuantity;

    @Field(FieldName.REQUEST_STATUS)
    private String status;

    @Field(FieldName.REQUEST_REQUEST_NOTE)
    private String requestNote;

    @Field(FieldName.REQUEST_TRANSACTION_NOTE)
    private String transactionNote;
}
