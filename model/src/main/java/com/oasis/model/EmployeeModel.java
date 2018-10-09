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

    private String fullname;
    private Date dob;
    private String username;
    private String password;
    private String phone;
    private String jobTitle;
    private String division;
    private Integer supervisingCount;
    private String supervisionId;
    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private String createdBy;
    private String updatedBy;
}
