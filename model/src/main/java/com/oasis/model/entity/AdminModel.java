package com.oasis.model.entity;

import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.AdminFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.ADMIN_COLLECTION_NAME)
public class AdminModel extends BaseEntity {

    @Field(AdminFieldName.NIK)
    private String nik;

    @Field(AdminFieldName.USERNAME)
    private String username;

    @Field(AdminFieldName.PASSWORD)
    private String password;
}
