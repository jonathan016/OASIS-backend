package com.oasis.repository;

import com.oasis.model.entity.EmployeeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeModel, String> {
    EmployeeModel findByUsername(String username);
    EmployeeModel findBy_id(String _id);
}