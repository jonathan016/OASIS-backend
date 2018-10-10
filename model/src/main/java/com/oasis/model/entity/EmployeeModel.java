package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionNames;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = CollectionNames.EMPLOYEE_COLLECTION_NAME)
@Data
public class EmployeeModel extends BaseEntity {
    @Id
    @Setter(AccessLevel.PRIVATE)
    private String _id;

    @Field("employeeFullname")
    private String fullname;

    @Field("employeeDOB")
    private Date dob;

    @Field("employeeUsername")
    private String username;

    @Field("employeePassword")
    private String password;

    @Field("employeePhone")
    private String phone;

    @Field("employeeJobTitle")
    private String jobTitle;

    @Field("employeeDivision")
    private String division;

    @Field("employeeSupervisingCount")
    private Integer supervisingCount;

    private String supervisionId;
}
