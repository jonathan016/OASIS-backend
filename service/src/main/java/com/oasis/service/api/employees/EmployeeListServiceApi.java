package com.oasis.service.api.employees;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;

import java.util.List;
import java.util.Map;

public interface EmployeeListServiceApi {

    Map< String, List< ? > > getEmployeesListData(
            final String username, final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< EmployeeModel > getEmployeesList(
            final String username, final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    long getEmployeesCount(
            final String username, final String query
    );

}
