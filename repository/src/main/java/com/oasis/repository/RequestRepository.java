package com.oasis.repository;

import com.oasis.model.entity.RequestModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<RequestModel, String> {
    List<RequestModel> findAllByStatusAndEmployeeId(String status, String employeeId);
}
