package com.oasis.service.api.employees;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;

public interface EmployeeDetailServiceApi {

    EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException;

    EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException;

    String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    );

}
