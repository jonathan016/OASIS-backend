package com.oasis.model;

import lombok.Data;
import org.bson.BsonTimestamp;

@Data
public class BaseEntity {
    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private String createdBy;
    private String updatedBy;
}
