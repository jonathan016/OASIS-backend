package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.SupervisionFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.SUPERVISION_COLLECTION_NAME)
public class SupervisionModel extends BaseEntity {
    @Field(SupervisionFieldName.SUPERVISION_SUPERVISOR_ID)
    private String supervisorId;

    @Field(SupervisionFieldName.SUPERVISION_EMPLOYEE_ID)
    private String employeeId;
}
