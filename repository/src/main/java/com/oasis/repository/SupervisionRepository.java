package com.oasis.repository;

import com.oasis.model.entity.SupervisionModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionRepository
        extends MongoRepository< SupervisionModel, String > {

    List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsernameEquals(String supervisorUsername);

    SupervisionModel findByDeletedIsFalseAndEmployeeUsernameEquals(String employeeUsername);

    boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(String supervisorUsername);

    boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameEqualsAndEmployeeUsernameEquals(
            String supervisorUsername, String employeeUsername
    );

}
