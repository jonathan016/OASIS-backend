package com.example.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.Date;

@Document(collection = "employees")
@Getter @Setter
public class EmployeeModel {
    @Id
    @Setter(AccessLevel.PRIVATE)
    private BigInteger _id;

    private String employeeFullname;
    private Date employeeDOB;
    private String employeeUsername;
    private String employeePassword;
    private String employeePhone;
    private String employeeJobTitle;
    private String employeeDivision;
    private Integer employeeSupervisingCount;
    private BigInteger supervisionId;
    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private BigInteger createdBy;
    private BigInteger updatedBy;
}
