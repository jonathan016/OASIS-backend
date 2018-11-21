package com.oasis.repository;

import com.oasis.model.entity.AdminModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends MongoRepository<AdminModel, String> {

    AdminModel findByNik(String nik);

    AdminModel save(AdminModel admin);

    void deleteByNik(String adminNik);
}
