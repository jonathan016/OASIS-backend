package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.webmodel.request.AddEmployeeRequest;
import com.oasis.webmodel.request.UpdateEmployeeRequest;
import com.oasis.webmodel.response.success.employees.EmployeeDetailResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;

import java.util.List;
import java.util.Set;

public interface EmployeesServiceApi {

    List<EmployeeListResponse.Employee> getAllEmployees(final int pageNumber, final String sortInfo) throws DataNotFoundException;

    List<EmployeeListResponse.Employee> mapEmployeesFound(Set<EmployeeModel> employees);

    EmployeeModel getEmployeeData(String employeeNik) throws DataNotFoundException;

    EmployeeModel getEmployeeSupervisorData(String employeeNik) throws DataNotFoundException;

    List<EmployeeListResponse.Employee> getEmployeesBySearchQuery(String searchQuery, int pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException;

    Set<EmployeeModel> fillData(String searchQuery, String sortInfo);

    void insertToDatabase(AddEmployeeRequest.Employee employee, String employeeNik) throws UnauthorizedOperationException, DataNotFoundException, DuplicateDataException;

    String generateEmployeeNik(String division);

    String generateEmployeeUsername(String fullname, String dob);

    String generateEmployeeDefaultPassword(String dob);

    String getSupervisionId(String employeeNik, String supervisorNik, String adminNik) throws DataNotFoundException;

    void createSupervision(String employeeNik, String supervisorNik, String adminNik);

    void updateSupervisorSupervisingCount(String adminNik, String supervisorNik);

    void updateEmployee(UpdateEmployeeRequest.Employee employeeRequest, String adminNik) throws DataNotFoundException, UnauthorizedOperationException;

    boolean checkCyclicSupervisingExists(String employeeNik, String supervisorNik);
}
