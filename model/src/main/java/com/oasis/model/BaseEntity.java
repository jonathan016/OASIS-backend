package com.oasis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class BaseEntity {

    @Id
    private String _id;

    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;

}
