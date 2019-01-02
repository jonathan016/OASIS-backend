package com.oasis.service.api.employees;

import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;

public interface EmployeeDetailServiceApi {

    EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException,
            UnauthorizedOperationException;

    EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException;

    String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    );

}
