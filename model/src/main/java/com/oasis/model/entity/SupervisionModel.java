package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.SupervisionFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.SUPERVISION_COLLECTION_NAME)
public class SupervisionModel
        extends BaseEntity {

    @Field(SupervisionFieldName.SUPERVISOR_USERNAME)
    private String supervisorUsername;

    @Field(SupervisionFieldName.EMPLOYEE_USERNAME)
    private String employeeUsername;

    @Field(SupervisionFieldName.DELETED)
    private boolean deleted;

}
