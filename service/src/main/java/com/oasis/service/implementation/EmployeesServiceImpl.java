package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
public class EmployeesServiceImpl implements EmployeesServiceApi {

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    @Override
    public List<EmployeeListResponse.Employee> getAllEmployees(int pageNumber, String sortInfo) throws DataNotFoundException {
        if (employeeRepository.findAll().size() == 0) {
            throw new DataNotFoundException(USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
        }

        if ((int)
                Math.ceil(
                        (float) employeeRepository.findAll().size()
                                / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                < pageNumber) {
            throw new DataNotFoundException(
                    USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
        }

        Set<EmployeeModel> employees = fillData("", sortInfo);
        return mapEmployeesFound(employees);
    }

    @Override
    public List<EmployeeListResponse.Employee> mapEmployeesFound(Set<EmployeeModel> employeesFound) {
        List<EmployeeListResponse.Employee> mappedEmployees = new ArrayList<>();

        for (EmployeeModel employeeFound : employeesFound) {
            SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeFound.getNik());
            EmployeeListResponse.Employee employee;

            if (supervision != null) {
                EmployeeModel supervisor = employeeRepository.findByNik(supervision.getSupervisorNik());

                employee =
                        new EmployeeListResponse.Employee(
                                employeeFound.getNik(),
                                employeeFound.getFullname(),
                                employeeFound.getJobTitle(),
                                employeeFound.getLocation(),
                                new EmployeeListResponse.Employee.Supervisor(
                                        supervisor.getNik(),
                                        supervisor.getFullname()
                                )
                        );
            } else {
                employee =
                        new EmployeeListResponse.Employee(
                                employeeFound.getNik(),
                                employeeFound.getFullname(),
                                employeeFound.getJobTitle(),
                                employeeFound.getLocation(),
                                null
                        );
            }

            mappedEmployees.add(employee);
        }

        return mappedEmployees;
    }

    @Override
    public EmployeeModel getEmployeeData(String employeeNik) throws DataNotFoundException {
        EmployeeModel employee = employeeRepository.findByNik(employeeNik);

        if (employee == null)
            throw new DataNotFoundException(INCORRECT_EMPLOYEE_NIK.getErrorCode(), INCORRECT_EMPLOYEE_NIK.getErrorMessage());

        return employee;
    }

    @Override
    public EmployeeModel getEmployeeSupervisorData(String employeeNik) throws DataNotFoundException {
        SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeNik);
        boolean isAdmin = roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR);

        if (supervision == null && !isAdmin)
            throw new DataNotFoundException(SUPERVISION_DATA_NOT_FOUND.getErrorCode(), SUPERVISION_DATA_NOT_FOUND.getErrorMessage());

        if (!isAdmin) {
            return employeeRepository.findByNik(supervision.getSupervisorNik());
        }

        return null;
    }

    @Override
    public List<EmployeeListResponse.Employee> getEmployeesBySearchQuery(String searchQuery, int pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException {
        if (searchQuery.isEmpty())
            throw new BadRequestException(
                    EMPTY_SEARCH_QUERY.getErrorCode(), EMPTY_SEARCH_QUERY.getErrorMessage()
            );

        Set<EmployeeModel> employeesFound = new HashSet<>();

        if (!searchQuery.contains(" ")) {
            if (employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(searchQuery, searchQuery).size() == 0) {
                throw new DataNotFoundException(
                        USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
            }

            if ((int)
                    Math.ceil(
                            (float)
                                    employeeRepository
                                            .findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(searchQuery, searchQuery)
                                            .size()
                                    / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                    < pageNumber) {
                throw new DataNotFoundException(
                        USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
            }

            employeesFound.addAll(fillData(searchQuery, sortInfo));
        } else {
            String[] queries = searchQuery.split(" ");

            for (String query : queries) {
                if (employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(query, query).size() == 0 &&
                        employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(query.toLowerCase(), query.toLowerCase()).size() == 0) {
                    throw new DataNotFoundException(
                            USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
                }

                if ((int)
                        Math.ceil(
                                (float) employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCase(query, query).size()
                                        / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                        < pageNumber) {
                    throw new DataNotFoundException(
                            USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());
                }

                employeesFound.addAll(fillData(query, sortInfo));
            }
        }

        return mapEmployeesFound(employeesFound);
    }

    @Override
    public Set<EmployeeModel> fillData(String searchQuery, String sortInfo) {
        Set<EmployeeModel> employeesFound = new LinkedHashSet<>();

        if (sortInfo.substring(1).equals("employeeNik")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByNikAsc(searchQuery, searchQuery));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByNikDesc(searchQuery, searchQuery));
            }
        } else if (sortInfo.substring(1).equals("employeeFullname")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByFullnameAsc(searchQuery, searchQuery));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrFullnameContainsIgnoreCaseOrderByFullnameDesc(searchQuery, searchQuery));
            }
        }

        return employeesFound;
    }
}
