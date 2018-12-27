package com.oasis.service.api.employees;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;

public interface EmployeeDeleteServiceApi {

    void deleteEmployee(
            final String adminUsername, final String employeeUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

    void changeSupervisorOnPreviousSupervisorDeletion(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

}
