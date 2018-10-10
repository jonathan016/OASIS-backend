package com.oasis.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bson.BsonTimestamp;
import org.springframework.data.annotation.Id;

@Data
public class BaseEntity {
    @Id
    @Setter(AccessLevel.PRIVATE)
    private String _id;

    private BsonTimestamp createdDate;
    private BsonTimestamp updatedDate;
    private String createdBy;
    private String updatedBy;
}
