package com.oasis.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

import static com.oasis.model.CollectionNames.EMPLOYEE_COLLECTION_NAME;

@Document(collection = EMPLOYEE_COLLECTION_NAME)
@Data
public class EmployeeModel {
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
    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private String createdBy;
    private String updatedBy;
}
