package com.oasis.repository;

import com.oasis.model.entity.AdminModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<AdminModel, String> {

    AdminModel findByNik(String nik);
}
