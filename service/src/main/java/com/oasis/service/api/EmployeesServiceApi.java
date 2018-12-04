package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.web_model.request.employees.AddEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeSupervisorRequest;
import com.oasis.web_model.request.employees.UpdateEmployeeRequest;
import com.oasis.web_model.response.success.employees.EmployeeListResponse;

import java.util.List;
import java.util.Set;

public interface EmployeesServiceApi {

    List<EmployeeModel> getEmployeesList(
            final String query,
            final int page,
            final String sort
    ) throws BadRequestException, DataNotFoundException;

    Set<EmployeeModel> getSortedEmployeesList(
            final int page,
            final String sort
    );

    Set<EmployeeModel> getSortedEmployeesListFromQuery(
            final int page,
            final String query,
            final String sort
    );

    int getEmployeesCount(
            final String query,
            final String sort
    );

    List<EmployeeModel> getSupervisorsList(
            final List<EmployeeModel> employees
    );

    List<EmployeeListResponse.Employee> mapEmployeesList(Set<EmployeeModel> employeesFound);

    EmployeeModel getEmployeeDetailData(String username) throws DataNotFoundException;

    EmployeeModel getEmployeeSupervisorData(String username) throws DataNotFoundException;

    void addEmployee(AddEmployeeRequest.Employee request, String username) throws UnauthorizedOperationException,
                                                                            DataNotFoundException, DuplicateDataException, BadRequestException;

    String generateEmployeeUsername(String name, String dob);

    String generateEmployeeDefaultPassword(String dob);

    String getSupervisionId(String employeeUsername, String supervisorUsername, String adminUsername) throws DataNotFoundException;

    void createSupervision(String employeeUsername, String supervisorUsername, String adminUsername);

    void updateEmployee(UpdateEmployeeRequest.Employee request, String adminUsername) throws DataNotFoundException,
                                                                                  UnauthorizedOperationException, BadRequestException;

    boolean checkCyclicSupervisingExists(String employeeUsername, String supervisorUsername);

    void deleteEmployee(DeleteEmployeeRequest request) throws UnauthorizedOperationException, DataNotFoundException,
                                                        BadRequestException;

    void changeSupervisorOnPreviousSupervisorDeletion(DeleteEmployeeSupervisorRequest request) throws UnauthorizedOperationException, DataNotFoundException, BadRequestException;

}
