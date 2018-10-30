package com.oasis.service.api;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.webmodel.response.success.employees.EmployeeDetailResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;

import java.util.List;

public interface EmployeesServiceApi {

    List<EmployeeListResponse.Employee> getAllEmployees(final int pageNumber, final String sortInfo) throws DataNotFoundException;

    List<EmployeeModel> sortData(final String sortInfo);

    List<EmployeeListResponse.Employee> mapEmployeesFound(List<EmployeeModel> employees);

    EmployeeModel getEmployeeData(String employeeNik) throws DataNotFoundException;

    SupervisionModel getEmployeeSupervisionData(String employeeNik) throws DataNotFoundException;
}
