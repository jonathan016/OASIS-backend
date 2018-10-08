package com.oasis.repository;

import com.oasis.model.EmployeeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends MongoRepository<EmployeeModel, String> {
    EmployeeModel findByUsername(String username);
}
