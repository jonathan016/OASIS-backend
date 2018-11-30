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

    List<EmployeeListResponse.Employee> getEmployeesList(final int page, final String sort) throws DataNotFoundException;

    List<EmployeeListResponse.Employee> mapEmployeesList(Set<EmployeeModel> employeesFound);

    EmployeeModel getEmployeeDetail(String username) throws DataNotFoundException;

    EmployeeModel getEmployeeSupervisorData(String username) throws DataNotFoundException;

    List<EmployeeListResponse.Employee> getEmployeesListBySearchQuery(String query, int page, String sort) throws BadRequestException, DataNotFoundException;

    Set<EmployeeModel> getSortedEmployeesListFromSearchQuery(String query, String sort);

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
