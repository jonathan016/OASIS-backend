package com.oasis.repository;

import com.oasis.model.entity.SupervisionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionRepository extends MongoRepository<SupervisionModel, String> {

    List<SupervisionModel> findAllBySupervisorNik(String supervisorNik);

    SupervisionModel findByEmployeeNik(String employeeNik);

    SupervisionModel save(SupervisionModel supervision);
}
