package com.oasis.repository;

import com.oasis.model.entity.SupervisionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionRepository
        extends MongoRepository< SupervisionModel, String > {

    List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsername(String supervisorUsername);

    SupervisionModel findByDeletedIsFalseAndEmployeeUsername(String employeeUsername);

    boolean existsByDeletedIsFalseAndEmployeeUsername(String employeeUsername);

    boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(String supervisorUsername);

    boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
            String supervisorUsername, String employeeUsername
    );

}
