package com.oasis.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.oasis.model.CollectionNames.EMPLOYEE_COLLECTION_NAME;

@Document(collection = EMPLOYEE_COLLECTION_NAME)
@Data
public class EmployeeModel {
    @Id
    @Setter(AccessLevel.PRIVATE)
    private String _id;

    private String employeeFullname;
    private Date employeeDOB;
    private String employeeUsername;
    private String employeePassword;
    private String employeePhone;
    private String employeeJobTitle;
    private String employeeDivision;
    private Integer employeeSupervisingCount;
    private String supervisionId;
    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private String createdBy;
    private String updatedBy;
}
