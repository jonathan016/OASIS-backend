package com.oasis.model.entity;

import com.oasis.model.base.BaseEntity;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.constant.entity_constant.field_name.AdminFieldName;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuppressWarnings("Lombok")
@Document(collection = CollectionName.ADMIN_COLLECTION_NAME)
public class AdminModel
        extends BaseEntity {

    @Field(AdminFieldName.USERNAME)
    private String username;

    @Field(AdminFieldName.PASSWORD)
    private String password;

    @Field(AdminFieldName.DELETED)
    private boolean deleted;

}
