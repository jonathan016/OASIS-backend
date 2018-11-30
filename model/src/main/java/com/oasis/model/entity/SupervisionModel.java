package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.SupervisionFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.SUPERVISION_COLLECTION_NAME)
public class SupervisionModel extends BaseEntity {

    @Field(SupervisionFieldName.SUPERVISOR_USERNAME)
    private String supervisorUsername;

    @Field(SupervisionFieldName.EMPLOYEE_USERNAME)
    private String employeeUsername;

}
