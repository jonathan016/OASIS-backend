package com.oasis.repository;

import com.oasis.model.entity.AdminModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository
        extends MongoRepository< AdminModel, String > {

    AdminModel findByDeletedIsFalseAndUsernameEquals(String username);

    AdminModel findByDeletedIsTrueAndUsernameEquals(String username);

    boolean existsAdminModelByDeletedIsFalseAndUsernameEquals(String username);

    boolean existsAdminModelByDeletedIsTrueAndUsernameEquals(String username);

}
