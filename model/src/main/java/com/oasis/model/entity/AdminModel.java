package com.oasis.model.entity;

import com.oasis.model.CollectionName;
import com.oasis.model.fieldname.AdminFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = CollectionName.ADMIN_COLLECTION_NAME)
public class AdminModel {

    @Field(AdminFieldName.ADMIN_NIK)
    private String nik;

    @Field(AdminFieldName.ADMIN_USERNAME)
    private String username;

    @Field(AdminFieldName.ADMIN_PASSWORD)
    private String password;
}
