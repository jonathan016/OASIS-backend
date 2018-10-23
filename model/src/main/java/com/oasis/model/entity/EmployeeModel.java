package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.EmployeeFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Document(collection = CollectionName.EMPLOYEE_COLLECTION_NAME)
public class EmployeeModel extends BaseEntity {

    @Field(EmployeeFieldName.EMPLOYEE_NIK)
    private String nik;

    @Field(EmployeeFieldName.EMPLOYEE_FULLNAME)
    private String fullname;

    @Field(EmployeeFieldName.EMPLOYEE_DOB)
    private Date dob;

    @Field(EmployeeFieldName.EMPLOYEE_USERNAME)
    private String username;

    @Field(EmployeeFieldName.EMPLOYEE_PASSWORD)
    private String password;

    @Field(EmployeeFieldName.EMPLOYEE_PHONE)
    private String phone;

    @Field(EmployeeFieldName.EMPLOYEE_JOB_TITLE)
    private String jobTitle;

    @Field(EmployeeFieldName.EMPLOYEE_DIVISION)
    private String division;

    @Field(EmployeeFieldName.EMPLOYEE_SUPERVISING_COUNT)
    private Integer supervisingCount;

    @Field(EmployeeFieldName.EMPLOYEE_SUPERVISION_ID)
    private String supervisionId;
}
