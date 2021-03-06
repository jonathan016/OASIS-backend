package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.EmployeeFieldName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.EMPLOYEE_COLLECTION_NAME)
public class EmployeeModel
        extends BaseEntity
        implements Serializable {

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

    @Field(EmployeeFieldName.DELETED)
    private boolean deleted;

    @Override
    public boolean equals(Object object) {

        if (object instanceof EmployeeModel) {
            return this.username.equals(( (EmployeeModel) object ).getUsername()) &&
                   this.name.equals(( (EmployeeModel) object ).getName()) &&
                   this.dob.equals(( (EmployeeModel) object ).getDob()) &&
                   this.phone.equals(( (EmployeeModel) object ).getPhone()) &&
                   this.jobTitle.equals(( (EmployeeModel) object ).getJobTitle()) &&
                   this.division.equals(( (EmployeeModel) object ).getDivision()) &&
                   this.location.equals(( (EmployeeModel) object ).getLocation());
        }

        return false;
    }

}
