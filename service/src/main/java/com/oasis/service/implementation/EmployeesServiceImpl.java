package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AdminModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.web_model.request.employees.AddEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeSupervisorRequest;
import com.oasis.web_model.request.employees.UpdateEmployeeRequest;
import com.oasis.web_model.response.success.employees.EmployeeListResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeesServiceImpl implements EmployeesServiceApi {

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private RequestRepository requestRepository;

    /*-------------Employees List Methods-------------*/
    @Override
    public List<EmployeeListResponse.Employee> getEmployeesList(final int page, final String sort) throws DataNotFoundException {

        int totalEmployees = employeeRepository.findAll().size();

        if (page < 1 || totalEmployees == 0)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if ((int) Math.ceil((float) totalEmployees / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE) < page)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Set<EmployeeModel> employees = getSortedEmployeesListFromSearchQuery("", sort);

        return mapEmployeesList(employees);
    }

    @Override
    public List<EmployeeListResponse.Employee> getEmployeesListBySearchQuery(final String query,
                                                                             final int pageNumber,
                                                                             final String sortInfo) throws BadRequestException, DataNotFoundException {

        if (query.isEmpty())
            throw new BadRequestException(EMPTY_SEARCH_QUERY);

        if (pageNumber < 1)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Set<EmployeeModel> employeesFound = new LinkedHashSet<>();

        String[] queries = query.split(" ");

        for (String word : queries) {
            int totalEmployees = employeeRepository.findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCase(
                    word,
                    word
            ).size();
            if (totalEmployees == 0)
                throw new DataNotFoundException(USER_NOT_FOUND);

            if ((int) Math.ceil((float) totalEmployees / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                < pageNumber) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }

            employeesFound.addAll(getSortedEmployeesListFromSearchQuery(word, sortInfo));
        }

        return mapEmployeesList(employeesFound);
    }

    @Override
    public Set<EmployeeModel> getSortedEmployeesListFromSearchQuery(final String searchQuery, final String sortInfo) {

        Set<EmployeeModel> sortedEmployeesList = new LinkedHashSet<>();

        if (sortInfo.substring(1).equals("nik")) {
            if (sortInfo.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByUsernameAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1).equals(ServiceConstant.DESCENDING)) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByUsernameDesc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            }
        } else if (sortInfo.substring(1).equals("name")) {
            if (sortInfo.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1).equals(ServiceConstant.DESCENDING)) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            }
        }

        return sortedEmployeesList;
    }

    @Override
    public List<EmployeeListResponse.Employee> mapEmployeesList(final Set<EmployeeModel> employeesFound) {

        List<EmployeeListResponse.Employee> mappedEmployees = new ArrayList<>();

        for (EmployeeModel employeeFound : employeesFound) {
            EmployeeListResponse.Employee employee;

            MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
            employeeDataFactory.classMap(EmployeeModel.class, EmployeeListResponse.Employee.class);
            employee = employeeDataFactory
                            .getMapperFacade(
                                    EmployeeModel.class,
                                    EmployeeListResponse.Employee.class
                            )
                            .map(employeeFound);

            SupervisionModel supervision = supervisionRepository.findByEmployeeUsername(employeeFound.getUsername());
            if (supervision != null) {
                EmployeeModel supervisor = employeeRepository.findByUsername(supervision.getSupervisorUsername());

                MapperFactory employeeSupervisorDataFactory = new DefaultMapperFactory.Builder().build();
                employeeSupervisorDataFactory.classMap(EmployeeModel.class, EmployeeListResponse.Employee.Supervisor.class);

                employee.setSupervisor(employeeSupervisorDataFactory
                                               .getMapperFacade(
                                                       EmployeeModel.class,
                                                       EmployeeListResponse.Employee.Supervisor.class
                                               )
                                               .map(supervisor));
            }

            mappedEmployees.add(employee);
        }

        return mappedEmployees;
    }

    @Override
    public EmployeeModel getEmployeeDetail(final String username) throws DataNotFoundException {

        EmployeeModel employee = employeeRepository.findByUsername(username);

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        return employee;
    }

    @Override
    public EmployeeModel getEmployeeSupervisorData(final String username) throws DataNotFoundException {

        SupervisionModel supervision = supervisionRepository.findByEmployeeUsername(username);
        boolean isAdmin = roleDeterminer.determineRole(username).equals(ServiceConstant.ROLE_ADMINISTRATOR);

        if (supervision == null && !isAdmin)
            throw new DataNotFoundException(SUPERVISION_DATA_NOT_FOUND);

        if (isAdmin)
            return null;

        return employeeRepository.findByUsername(supervision.getSupervisorUsername());
    }

    /*-------------Add Employee Methods-------------*/
    @Override
    public void addEmployee(final AddEmployeeRequest.Employee request, final String adminUsername) throws UnauthorizedOperationException, DataNotFoundException, DuplicateDataException, BadRequestException {

        if (!roleDeterminer.determineRole(adminUsername).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);

        boolean possibleEmployeeDuplicates;
        try {
            possibleEmployeeDuplicates = employeeRepository.existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
                            request.getName(),
                            new SimpleDateFormat("dd-MM-yyyy").parse(request.getDob()),
                            request.getPhone(),
                            request.getJobTitle(),
                            request.getDivision(),
                            request.getLocation()
                    );
        } catch (ParseException e) {
            throw new BadRequestException(INCORRECT_DATE_FORMAT);
        }

        if (!request.getName().matches("[A-Za-z]+")) {
            //TODO throw real exception
            throw new BadRequestException(USER_NOT_FOUND);
        }

        if (!possibleEmployeeDuplicates) {
            throw new DuplicateDataException(DUPLICATE_EMPLOYEE_DATA_FOUND);
        } else {
            EmployeeModel employee = new EmployeeModel();

            employee.setName(request.getName());
            employee.setUsername(generateEmployeeUsername(request.getName().toLowerCase(),
                                                          request.getDob()));
            employee.setPassword(generateEmployeeDefaultPassword(request.getDob()));
            try {
                employee.setDob(new SimpleDateFormat("dd-MM-yyyy").parse(request.getDob()));
            } catch (ParseException e) {
                throw new BadRequestException(INCORRECT_DATE_FORMAT);
            }
            employee.setPhone(request.getPhone());
            employee.setJobTitle(request.getJobTitle());
            employee.setDivision(request.getDivision());
            employee.setLocation(request.getLocation());
            employee.setSupervisionId(getSupervisionId(employee.getUsername(), request.getSupervisorUsername(), adminUsername));
            employee.setCreatedDate(new Date());
            employee.setUpdatedDate(new Date());
            employee.setCreatedBy(adminUsername);
            employee.setUpdatedBy(adminUsername);

            employeeRepository.save(employee);
        }
    }

    @Override
    public String generateEmployeeUsername(final String name, final String dob) {

        StringBuilder username = new StringBuilder();

        if (!name.contains(" ")) {
            String givenName = name.substring(0, name.lastIndexOf(" "));
            String firstNames[] = givenName.split(" ");
            for (String firstName : firstNames) {
                username.append(firstName.charAt(0));
                username.append(".");
            }

            String lastName = name.substring(name.lastIndexOf(" ") + 1);
            username.append(lastName);
        } else {
            username.append(name);
        }

        if (employeeRepository.findByUsername(String.valueOf(username).concat("@gdn-commerce.com")) != null) {
            int suffix = employeeRepository.findAllByUsernameContains(String.valueOf(username)).size();
            username.append(suffix);
        }

        username.append("@gdn-commerce.com");

        return String.valueOf(username);
    }

    @Override
    public String generateEmployeeDefaultPassword(final String dob) {

        StringBuilder password = new StringBuilder(ServiceConstant.NIK_PREFIX.toLowerCase());

        password.append(dob.replace("-", ""));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        password.replace(0, password.length(), encoder.encode(String.valueOf(password)));

        return String.valueOf(password);
    }

    @Override
    public void createSupervision(final String employeeUsername, final String supervisorUsername, final String adminUsername) {

        SupervisionModel supervision = new SupervisionModel();
        supervision.setSupervisorUsername(supervisorUsername);
        supervision.setEmployeeUsername(employeeUsername);
        supervision.setCreatedDate(new Date());
        supervision.setUpdatedDate(new Date());
        supervision.setCreatedBy(adminUsername);
        supervision.setUpdatedBy(adminUsername);

        supervisionRepository.save(supervision);
    }

    @Override
    public String getSupervisionId(final String employeeUsername, final String supervisorUsername, final String adminUsername) throws DataNotFoundException {

        if (employeeRepository.findByUsername(supervisorUsername) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        createSupervision(employeeUsername, supervisorUsername, adminUsername);

        return supervisionRepository.findByEmployeeUsername(employeeUsername).get_id();
    }

    /*-------------Update Employee Methods-------------*/
    @Override
    public void updateEmployee(final UpdateEmployeeRequest.Employee request, final String adminUsername) throws DataNotFoundException, UnauthorizedOperationException, BadRequestException {

        if (!roleDeterminer.determineRole(adminUsername).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);

        EmployeeModel employee = employeeRepository.findByUsername(request.getUsername());

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Date requestDob;
        try {
            requestDob = new SimpleDateFormat("dd-MM-yyyy").parse(request.getDob());
        } catch (ParseException e) {
            throw new BadRequestException(INCORRECT_DATE_FORMAT);
        }

        if (!employee.getName().equals(request.getName()) || employee.getDob().compareTo(
                Objects.requireNonNull(requestDob)) != 0)
            employee.setUsername(generateEmployeeUsername(request.getName().toLowerCase(),
                                                          request.getDob()));

        employee.setName(request.getName());
        employee.setDob(requestDob);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        employee.setPassword(encoder.encode(request.getPassword()));
        employee.setPhone(request.getPhone());
        employee.setJobTitle(request.getJobTitle());
        employee.setDivision(request.getDivision());
        employee.setLocation(request.getLocation());

        EmployeeModel supervisor = employeeRepository.findByUsername(request.getSupervisorUsername());
        if (supervisor == null) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        } else if (!request.getSupervisorUsername().equals(supervisionRepository.findByEmployeeUsername(request.getUsername()).getSupervisorUsername())) {
            SupervisionModel supervision = supervisionRepository.findByEmployeeUsername(request.getUsername());

            EmployeeModel previousSupervisor = employeeRepository.findByUsername(supervision.getSupervisorUsername());
            employeeRepository.save(previousSupervisor);

            if (checkCyclicSupervisingExists(employee.getUsername(), request.getSupervisorUsername()))
                throw new UnauthorizedOperationException(CYCLIC_SUPERVISING_OCCURRED);

            supervision.setSupervisorUsername(supervisor.getUsername());
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(adminUsername);
            supervisionRepository.save(supervision);

            if (supervisionRepository.existsSupervisionModelsBySupervisorUsername(employee.getUsername())) {
                AdminModel promotedAdmin = new AdminModel();

                promotedAdmin.setUsername(supervisor.getUsername());
                promotedAdmin.setPassword(supervisor.getPassword());
                promotedAdmin.setCreatedDate(new Date());
                promotedAdmin.setCreatedBy(adminUsername);
                promotedAdmin.setUpdatedDate(new Date());
                promotedAdmin.setUpdatedBy(adminUsername);

                adminRepository.save(promotedAdmin);
            }
        }

        employee.setUpdatedDate(new Date());
        employee.setUpdatedBy(adminUsername);

        employeeRepository.save(employee);
    }

    @Override
    public boolean checkCyclicSupervisingExists(final String employeeUsername, String supervisorUsername) {

        String supervisorSupervisorNik = supervisionRepository.findByEmployeeUsername(supervisorUsername).getSupervisorUsername();

        return supervisorSupervisorNik.equals(employeeUsername);
    }

    /*-------------Delete Employee Methods-------------*/
    @Override
    public void deleteEmployee(final DeleteEmployeeRequest request) throws UnauthorizedOperationException,
                                                                                    DataNotFoundException, BadRequestException {

        if (!roleDeterminer.determineRole(request.getAdminUsername()).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        if (request.getEmployeeUsername().isEmpty())
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        if (request.getEmployeeUsername().equals(request.getAdminUsername()))
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        if (employeeRepository.findByUsername(request.getEmployeeUsername()) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if (supervisionRepository.existsSupervisionModelsBySupervisorUsername(request.getEmployeeUsername()))
            throw new UnauthorizedOperationException(EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT);

        if (!requestRepository.findAllByUsernameAndStatus(request.getEmployeeUsername(), ServiceConstant.PENDING_RETURN).isEmpty())
            throw new UnauthorizedOperationException(UNRETURNED_ASSETS_ON_DELETION_ATTEMPT);

        if (!supervisionRepository.existsByEmployeeUsername(request.getEmployeeUsername()))
            throw new DataNotFoundException(USER_NOT_FOUND);

        List<RequestModel> requests = new ArrayList<>();
        requests.addAll(requestRepository.findAllByUsernameAndStatus(request.getEmployeeUsername(),
                                                                ServiceConstant.PENDING_HANDOVER));
        requests.addAll(requestRepository.findAllByUsernameAndStatus(request.getEmployeeUsername(), ServiceConstant.REQUESTED));
        if (!requests.isEmpty()) {
            for (RequestModel employeeRequest : requests) {
                employeeRequest.setStatus(ServiceConstant.CANCELLED);
                employeeRequest.setUpdatedDate(new Date());
                employeeRequest.setUpdatedBy(request.getAdminUsername());

                requestRepository.save(employeeRequest);
            }
        }

        if (adminRepository.findByUsername(request.getEmployeeUsername()) != null)
            adminRepository.deleteByUsername(request.getEmployeeUsername());

        employeeRepository.deleteByUsername(request.getEmployeeUsername());

        supervisionRepository.deleteByEmployeeUsername(request.getEmployeeUsername());
    }

    @Override
    public void changeSupervisorOnPreviousSupervisorDeletion(final DeleteEmployeeSupervisorRequest request) throws UnauthorizedOperationException, DataNotFoundException, BadRequestException {

        boolean isNotAdmin =
                !roleDeterminer.determineRole(request.getAdminUsername()).equals(ServiceConstant.ROLE_ADMINISTRATOR);
        if (isNotAdmin)
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        boolean requestDataMissing =
                request.getOldSupervisorUsername().isEmpty() || request.getNewSupervisorUsername().isEmpty();
        if (requestDataMissing)
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        boolean isIdenticalNik =
                request.getOldSupervisorUsername().equals(request.getAdminUsername());
        if (isIdenticalNik)
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        boolean userDoesNotExist =
                employeeRepository.findByUsername(request.getOldSupervisorUsername()) == null || employeeRepository.findByUsername(request.getNewSupervisorUsername()) == null;
        if (userDoesNotExist)
            throw new DataNotFoundException(USER_NOT_FOUND);

        boolean doesNotSupervise =
                supervisionRepository.findAllBySupervisorUsername(request.getOldSupervisorUsername()).isEmpty();
        if (doesNotSupervise)
            throw new DataNotFoundException(SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE);

        List<SupervisionModel> supervisions =
                supervisionRepository.findAllBySupervisorUsername(request.getOldSupervisorUsername());

        for (SupervisionModel supervision : supervisions) {
            EmployeeModel previousSupervisor = employeeRepository.findByUsername(supervision.getSupervisorUsername());
            employeeRepository.save(previousSupervisor);

            supervision.setSupervisorUsername(request.getNewSupervisorUsername());
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(request.getAdminUsername());

            supervisionRepository.save(supervision);
        }
    }

}
