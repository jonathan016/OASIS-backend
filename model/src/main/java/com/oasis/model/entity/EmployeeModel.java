package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.EmployeeFieldName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@NoArgsConstructor
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.EMPLOYEE_COLLECTION_NAME)
public class EmployeeModel extends BaseEntity {

    @Field(EmployeeFieldName.NAME)
    private String name;

    @Field(EmployeeFieldName.DOB)
    private Date dob;

    @Field(EmployeeFieldName.USERNAME)
    private String username;

    @Field(EmployeeFieldName.PASSWORD)
    private String password;

    @Field(EmployeeFieldName.PHOTO)
    private String photo;

    @Field(EmployeeFieldName.PHONE)
    private String phone;

    @Field(EmployeeFieldName.JOB_TITLE)
    private String jobTitle;

    @Field(EmployeeFieldName.DIVISION)
    private String division;

    @Field(EmployeeFieldName.LOCATION)
    private String location;

    @Field(EmployeeFieldName.SUPERVISION_ID)
    private String supervisionId;

}
