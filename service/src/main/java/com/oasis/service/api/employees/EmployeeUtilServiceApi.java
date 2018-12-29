package com.oasis.service.api.employees;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;

import java.util.List;

public interface EmployeeUtilServiceApi {

    void demotePreviousSupervisorFromAdminIfNecessary(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername,
            final List< SupervisionModel > supervisions
    );

    byte[] getEmployeePhoto(
            final String username, final String photoName, final String extension
    );

    void updateSupervisorDataOnEmployeeDataModification(
            final String adminUsername, final String employeeUsername, final String supervisorUsername,
            final boolean addEmployeeOperation
    ) throws DataNotFoundException;

    boolean isEmployeeTopAdministrator(final String username);

    EmployeeModel findByDeletedIsFalseAndUsername(final String username);

    boolean existsAdminModelByDeletedIsFalseAndUsernameEquals(final String username);

    boolean existsEmployeeModelByDeletedIsFalseAndUsername(final String username);

    boolean existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(final String username);

    boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
            final String supervisorUsername, final String employeeUsername
    );

    List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername);

    boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername);

}
