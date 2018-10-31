package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.webmodel.request.AddEmployeeRequest;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Override
    public void insertToDatabase(AddEmployeeRequest.Employee employeeRequest, String adminNik) throws UnauthorizedOperationException, DataNotFoundException {

        try {
            if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR))
                throw new UnauthorizedOperationException(EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorCode(), EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR.getErrorMessage());
        } catch (DataNotFoundException dataNotFoundException) {
            throw dataNotFoundException;
        }

        //TODO Handle check for duplicate data (possibly check with phone number)

        EmployeeModel employee = new EmployeeModel();

        employee.setNik(generateEmployeeNik(employeeRequest.getDivision()));
        employee.setFullname(employeeRequest.getFullname());
        employee.setUsername(generateEmployeeUsername(employeeRequest.getFullname().toLowerCase(), employeeRequest.getDob()));
        employee.setPassword(generateEmployeeDefaultPassword(employeeRequest.getDob()));
        try {
            employee.setDob(new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getDob()));
        } catch (ParseException e) {
            //TODO Handle exception
        }
        employee.setDivision(employeeRequest.getDivision());
        employee.setJobTitle(employeeRequest.getJobTitle());
        employee.setLocation(employeeRequest.getLocation());
        employee.setSupervisingCount(ServiceConstant.ZERO);
        employee.setSupervisionId(getSupervisionId(employee.getNik(), employeeRequest.getSupervisorId(), adminNik));
        employee.setCreatedDate(new Date());
        employee.setUpdatedDate(new Date());
        employee.setCreatedBy(adminNik);
        employee.setUpdatedBy(adminNik);

        employeeRepository.save(employee);
    }

    @Override
    public String generateEmployeeNik(String division) {
        StringBuilder nik = new StringBuilder();

        nik.append(ServiceConstant.NIK_PREFIX);

        if (employeeRepository.findAll().size() != 0) {
            EmployeeModel employeeWithDivision = employeeRepository.findFirstByDivisionOrderByNikDesc(division);

            if (employeeWithDivision == null) {
                String lastDivisionNumber = employeeRepository.findFirstByDivisionOrderByNikDesc(division).getNik();
                int lastDivisionCode = Integer.valueOf(lastDivisionNumber.substring(4, 7));

                nik.append(String.format("-%03d", lastDivisionCode + 1));
                nik.append(String.format("-%03d", 1));
            } else {
                String lastEmployeeDivisionNumber = employeeRepository.findFirstByNikContainsAndDivisionOrderByNikDesc(String.valueOf(nik), division).getNik();
                int lastEmployeeCode = Integer.valueOf((lastEmployeeDivisionNumber.substring(8, 11)));

                nik.append(String.format("-%03d", Integer.valueOf(lastEmployeeDivisionNumber.substring(4, 7))));
                nik.append(String.format("-%03d", lastEmployeeCode + 1));
            }
        } else {
            nik.append(String.format("-%03d", 1));
            nik.append(String.format("-%03d", 1));
        }

        return String.valueOf(nik);
    }

    @Override
    public String generateEmployeeUsername(String fullname, String dob) {
        StringBuilder username = new StringBuilder();

        boolean singleWordName = !fullname.contains(" ");

        if (!singleWordName) {
            String firstName = fullname.substring(0, fullname.lastIndexOf(" "));
            String firstNames[] = firstName.split(" ");
            for (String name : firstNames) {
                username.append(name.charAt(0));
                username.append(".");
            }

            String lastName = fullname.substring(fullname.lastIndexOf(" ") + 1);
            username.append(lastName);
        }

        while (employeeRepository.findByUsername(String.valueOf(username)) != null) {
            username.append(dob, 0, 2);
        }

        username.append("@gdn-commerce.com");

        return String.valueOf(username);
    }

    @Override
    public String generateEmployeeDefaultPassword(String dob) {
        StringBuilder password = new StringBuilder();

        password.append(ServiceConstant.NIK_PREFIX.toLowerCase());
        password.append(dob.replace("-", ""));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        password.replace(0, password.length(), encoder.encode(String.valueOf(password)));

        return String.valueOf(password);
    }

    @Override
    public void updateSupervisorSupervisingCount(String supervisorNik, String adminNik){
        EmployeeModel supervisor = employeeRepository.findByNik(supervisorNik);

        supervisor.setSupervisingCount(supervisor.getSupervisingCount() + 1);
        supervisor.setUpdatedDate(new Date());
        supervisor.setUpdatedBy(adminNik);

        employeeRepository.save(supervisor);
    }

    @Override
    public void createSupervision(String employeeNik, String supervisorNik, String adminNik) {
        updateSupervisorSupervisingCount(supervisorNik, adminNik);

        SupervisionModel supervision = new SupervisionModel();

        supervision.setSupervisorNik(supervisorNik);
        supervision.setEmployeeNik(employeeNik);
        supervision.setCreatedDate(new Date());
        supervision.setUpdatedDate(new Date());
        supervision.setCreatedBy(adminNik);
        supervision.setUpdatedBy(adminNik);

        supervisionRepository.save(supervision);
    }

    @Override
    public String getSupervisionId(String employeeNik, String supervisorNik, String adminNik) throws DataNotFoundException {
        if (employeeRepository.findByNik(supervisorNik) == null)
            throw new DataNotFoundException(USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getErrorMessage());

        createSupervision(employeeNik, supervisorNik, adminNik);

        return supervisionRepository.findByEmployeeNik(employeeNik).get_id();
    }
}
