package com.oasis.repository;

import com.oasis.model.entity.SupervisionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionRepository extends MongoRepository<SupervisionModel, String> {

    List<SupervisionModel> findAllBySupervisorUsername(String supervisorUsername);

    SupervisionModel findByEmployeeUsername(String employeeUsername);

    void deleteByEmployeeUsername(String employeeUsername);

    boolean existsByEmployeeUsername(String employeeUsername);

    boolean existsSupervisionModelsBySupervisorUsername(String supervisorUsername);

    boolean existsSupervisionModelBySupervisorUsernameAndEmployeeUsername(String supervisorUsername,
                                                                          String employeeUsername);

}
