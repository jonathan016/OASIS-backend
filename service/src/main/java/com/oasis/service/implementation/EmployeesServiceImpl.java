package com.oasis.service.implementation;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.webmodel.response.success.employees.EmployeeDetailResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.oasis.exception.helper.ErrorCodeAndMessage.USER_NOT_FOUND;

@Service
public class EmployeesServiceImpl implements EmployeesServiceApi {

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

        List<EmployeeModel> employees = sortData(sortInfo);

        return mapEmployeesFound(employees);
    }

    @Override
    public List<EmployeeListResponse.Employee> mapEmployeesFound(List<EmployeeModel> employeesFound) {
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
    public List<EmployeeModel> sortData(String sortInfo) {
        List<EmployeeModel> employees = employeeRepository.findAll();

        if (sortInfo.substring(1)
                .equals("employeeNik")) {
            if (sortInfo.substring(0, 1)
                    .equals("A")) {
                employees.sort(Comparator.comparing(EmployeeModel::getNik));
            } else if (sortInfo.substring(0, 1)
                    .equals("D")) {
                employees.sort(Comparator.comparing(EmployeeModel::getNik)
                        .reversed());
            }
        } else if (sortInfo.substring(1)
                .equals("employeeFullname")) {
            if (sortInfo.substring(0, 1)
                    .equals("A")) {
                employees.sort(Comparator.comparing(EmployeeModel::getFullname));
            } else if (sortInfo.substring(0, 1)
                    .equals("D")) {
                employees.sort(Comparator.comparing(EmployeeModel::getFullname)
                        .reversed());
            }
        }

        return employees;
    }

    @Override
    public EmployeeDetailResponse getEmployeeData(String employeeNik) throws DataNotFoundException {
        EmployeeModel employee = employeeRepository.findByNik(employeeNik);

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());

        SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeNik);

        EmployeeDetailResponse employeeDetailResponse;
        if (supervision != null) {
            EmployeeModel supervisor = employeeRepository.findByNik(supervision.getSupervisorNik());
            employeeDetailResponse =
                    new EmployeeDetailResponse(
                            employee.getNik(),
                            employee.getUsername(),
                            employee.getFullname(),
                            new SimpleDateFormat("dd-MMM-yyyy").format(employee.getDob()),
                            employee.getPhone(),
                            employee.getJobTitle(),
                            employee.getDivision(),
                            employee.getLocation(),
                            new EmployeeDetailResponse.Supervisor(
                                    supervisor.getNik(),
                                    supervisor.getFullname()
                            )
                    );
        } else {
            employeeDetailResponse =
                    new EmployeeDetailResponse(
                            employee.getNik(),
                            employee.getUsername(),
                            employee.getFullname(),
                            new SimpleDateFormat("dd-MMM-yyyy").format(employee.getDob()),
                            employee.getPhone(),
                            employee.getJobTitle(),
                            employee.getDivision(),
                            employee.getLocation(),
                            null
                    );
        }

        return employeeDetailResponse;
    }
}
