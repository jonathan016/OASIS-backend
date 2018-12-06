package com.oasis.repository;

import com.oasis.model.entity.AdminModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<AdminModel, String> {

    AdminModel findByUsername(String username);

    void deleteByUsername(String username);

    boolean existsAdminModelByUsername(String username);

}
