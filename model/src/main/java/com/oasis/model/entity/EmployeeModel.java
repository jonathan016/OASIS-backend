package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.FieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = CollectionName.EMPLOYEE_COLLECTION_NAME)
public class EmployeeModel extends BaseEntity {
    @Field(FieldName.EMPLOYEE_FULLNAME)
    private String fullname;

    @Field(FieldName.EMPLOYEE_DOB)
    private Date dob;

    @Field(FieldName.EMPLOYEE_USERNAME)
    private String username;

    @Field(FieldName.EMPLOYEE_PASSWORD)
    private String password;

    @Field(FieldName.EMPLOYEE_PHONE)
    private String phone;

    @Field(FieldName.EMPLOYEE_JOB_TITLE)
    private String jobTitle;

    @Field(FieldName.EMPLOYEE_DIVISION)
    private String division;

    @Field(FieldName.EMPLOYEE_SUPERVISING_COUNT)
    private Integer supervisingCount;

    private String supervisionId;
}
