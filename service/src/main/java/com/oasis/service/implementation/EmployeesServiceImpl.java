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
import com.oasis.webmodel.request.AddEmployeeRequest;
import com.oasis.webmodel.request.DeleteEmployeeRequest;
import com.oasis.webmodel.request.DeleteEmployeeSupervisorRequest;
import com.oasis.webmodel.request.UpdateEmployeeRequest;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
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
    public List<EmployeeListResponse.Employee> getEmployeesList(final int pageNumber, final String sortInfo) throws DataNotFoundException {

        int totalEmployees = employeeRepository.findAll().size();

        if (pageNumber < 1 || totalEmployees == 0)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if ((int) Math.ceil((float) totalEmployees / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE) < pageNumber)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Set<EmployeeModel> employees = getSortedEmployeesListFromSearchQuery("", sortInfo);

        return mapEmployeesList(employees);

    }

    @Override
    public List<EmployeeListResponse.Employee> getEmployeesListBySearchQuery(final String searchQuery,
                                                                             final int pageNumber,
                                                                             final String sortInfo) throws BadRequestException, DataNotFoundException {

        if (searchQuery.isEmpty())
            throw new BadRequestException(EMPTY_SEARCH_QUERY);

        if (pageNumber < 1)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Set<EmployeeModel> employeesFound = new LinkedHashSet<>();

        String[] queries = searchQuery.split(" ");

        for (String query : queries) {
            int totalEmployees = employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(
                    query,
                    query
            ).size();
            if (totalEmployees == 0)
                throw new DataNotFoundException(USER_NOT_FOUND);

            if ((int) Math.ceil((float) totalEmployees / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                < pageNumber) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }

            employeesFound.addAll(getSortedEmployeesListFromSearchQuery(query, sortInfo));
        }

        return mapEmployeesList(employeesFound);

    }

    @Override
    public Set<EmployeeModel> getSortedEmployeesListFromSearchQuery(final String searchQuery, final String sortInfo) {

        Set<EmployeeModel> sortedEmployeesList = new LinkedHashSet<>();

        if (sortInfo.substring(1).equals("nik")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1).equals("D")) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikDesc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            }
        } else if (sortInfo.substring(1).equals("name")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                                        searchQuery,
                                        searchQuery
                                )
                );
            } else if (sortInfo.substring(0, 1).equals("D")) {
                sortedEmployeesList.addAll(
                        employeeRepository
                                .findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
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

            SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeFound.getNik());
            if (supervision != null) {
                EmployeeModel supervisor = employeeRepository.findByNik(supervision.getSupervisorNik());

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
    public EmployeeModel getEmployeeDetail(final String employeeNik) throws DataNotFoundException {

        EmployeeModel employee = employeeRepository.findByNik(employeeNik);

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        return employee;

    }

    @Override
    public EmployeeModel getEmployeeSupervisorData(final String employeeNik) throws DataNotFoundException {

        SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeNik);
        boolean isAdmin = roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR);

        if (supervision == null && !isAdmin)
            throw new DataNotFoundException(SUPERVISION_DATA_NOT_FOUND);

        if (isAdmin)
            return null;

        return employeeRepository.findByNik(supervision.getSupervisorNik());

    }

    /*-------------Add Employee Methods-------------*/
    @Override
    public void addEmployee(final AddEmployeeRequest.Employee employeeRequest, final String adminNik) throws UnauthorizedOperationException, DataNotFoundException, DuplicateDataException, BadRequestException {

        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);

        boolean possibleEmployeeDuplicates;
        try {
            possibleEmployeeDuplicates = employeeRepository.existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
                            employeeRequest.getFullname(),
                            new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getDob()),
                            employeeRequest.getPhone(),
                            employeeRequest.getJobTitle(),
                            employeeRequest.getDivision(),
                            employeeRequest.getLocation()
                    );
        } catch (ParseException e) {
            throw new BadRequestException(INCORRECT_DATE_FORMAT);
        }

        if (!possibleEmployeeDuplicates) {
            throw new DuplicateDataException(DUPLICATE_EMPLOYEE_DATA_FOUND);
        } else {
            EmployeeModel employee = new EmployeeModel();

            employee.setNik(generateEmployeeNik(employeeRequest.getDivision()));
            employee.setName(employeeRequest.getFullname());
            employee.setUsername(generateEmployeeUsername(employeeRequest.getFullname().toLowerCase(), employeeRequest.getDob()));
            employee.setPassword(generateEmployeeDefaultPassword(employeeRequest.getDob()));
            try {
                employee.setDob(new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getDob()));
            } catch (ParseException e) {
                throw new BadRequestException(INCORRECT_DATE_FORMAT);
            }
            employee.setPhone(employeeRequest.getPhone());
            employee.setJobTitle(employeeRequest.getJobTitle());
            employee.setDivision(employeeRequest.getDivision());
            employee.setLocation(employeeRequest.getLocation());
            employee.setSupervisingCount(ServiceConstant.ZERO);
            employee.setSupervisionId(getSupervisionId(employee.getNik(), employeeRequest.getSupervisorId(), adminNik));
            employee.setCreatedDate(new Date());
            employee.setUpdatedDate(new Date());
            employee.setCreatedBy(adminNik);
            employee.setUpdatedBy(adminNik);

            employeeRepository.save(employee);
        }

    }

    @Override
    public String generateEmployeeNik(final String division) {

        StringBuilder nik = new StringBuilder(ServiceConstant.NIK_PREFIX);

        if (employeeRepository.findAll().size() != 0) {
            EmployeeModel employeeWithDivision = employeeRepository.findFirstByDivisionOrderByNikDesc(division);

            if (employeeWithDivision != null) {
                String lastEmployeeDivisionNumber = employeeRepository.findFirstByNikContainsAndDivisionOrderByNikDesc(String.valueOf(nik), division).getNik();
                int lastEmployeeCode = Integer.valueOf((lastEmployeeDivisionNumber.substring(8, 11)));

                nik.append(String.format("-%03d", Integer.valueOf(lastEmployeeDivisionNumber.substring(4, 7))));
                nik.append(String.format("-%03d", lastEmployeeCode + 1));
            } else {
                String lastDivisionNumber = employeeRepository.findFirstByNikContainsOrderByNikDesc(String.valueOf(nik)).getNik();
                int lastDivisionCode = Integer.valueOf(lastDivisionNumber.substring(4, 7));

                nik.append(String.format("-%03d", lastDivisionCode + 1));
                nik.append(String.format("-%03d", 1));
            }
        } else {
            nik.append(String.format("-%03d", 1));
            nik.append(String.format("-%03d", 1));
        }

        return String.valueOf(nik);

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
    public void updateSupervisorSupervisingCount(final String supervisorNik, final String adminNik) {

        EmployeeModel supervisor = employeeRepository.findByNik(supervisorNik);

        supervisor.setSupervisingCount(supervisor.getSupervisingCount() + 1);
        supervisor.setUpdatedDate(new Date());
        supervisor.setUpdatedBy(adminNik);

        employeeRepository.save(supervisor);

    }

    @Override
    public void createSupervision(final String employeeNik, final String supervisorNik, final String adminNik) {

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
    public String getSupervisionId(final String employeeNik, final String supervisorNik, final String adminNik) throws DataNotFoundException {

        if (employeeRepository.findByNik(supervisorNik) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        createSupervision(employeeNik, supervisorNik, adminNik);

        return supervisionRepository.findByEmployeeNik(employeeNik).get_id();

    }

    /*-------------Update Employee Methods-------------*/
    @Override
    public void updateEmployee(final UpdateEmployeeRequest.Employee employeeRequest, final String adminNik) throws DataNotFoundException, UnauthorizedOperationException, BadRequestException {

        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);

        EmployeeModel employee = employeeRepository.findByNik(employeeRequest.getEmployeeNik());

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Date requestDob;
        try {
            requestDob = new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getEmployeeDob());
        } catch (ParseException e) {
            throw new BadRequestException(INCORRECT_DATE_FORMAT);
        }

        if (!employee.getName().equals(employeeRequest.getEmployeeFullname()) || employee.getDob().compareTo(
                Objects.requireNonNull(requestDob)) != 0)
            employee.setUsername(generateEmployeeUsername(employeeRequest.getEmployeeFullname().toLowerCase(), employeeRequest.getEmployeeDob()));

        employee.setName(employeeRequest.getEmployeeFullname());
        employee.setDob(requestDob);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        employee.setPassword(encoder.encode(employeeRequest.getEmployeePassword()));
        employee.setPhone(employeeRequest.getEmployeePhone());
        employee.setJobTitle(employeeRequest.getEmployeeJobTitle());
        employee.setDivision(employeeRequest.getEmployeeDivision());
        employee.setLocation(employeeRequest.getEmployeeLocation());

        EmployeeModel supervisor = employeeRepository.findByNik(employeeRequest.getEmployeeSupervisorId());
        if (supervisor == null) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        } else if (!employeeRequest.getEmployeeSupervisorId().equals(supervisionRepository.findByEmployeeNik(employeeRequest.getEmployeeNik()).getSupervisorNik())) {
            SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeRequest.getEmployeeNik());

            EmployeeModel previousSupervisor = employeeRepository.findByNik(supervision.getSupervisorNik());
            previousSupervisor.setSupervisingCount(previousSupervisor.getSupervisingCount() - 1);
            employeeRepository.save(previousSupervisor);

            if (checkCyclicSupervisingExists(employee.getNik(), employeeRequest.getEmployeeSupervisorId()))
                throw new UnauthorizedOperationException(CYCLIC_SUPERVISING_OCCURRED);

            supervision.setSupervisorNik(supervisor.getNik());
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(adminNik);
            supervisionRepository.save(supervision);

            if (employee.getSupervisingCount() > 0) {
                AdminModel promotedAdmin = new AdminModel();

                promotedAdmin.setNik(supervisor.getNik());
                promotedAdmin.setUsername(supervisor.getUsername());
                promotedAdmin.setPassword(supervisor.getPassword());
                promotedAdmin.setCreatedDate(new Date());
                promotedAdmin.setCreatedBy(adminNik);
                promotedAdmin.setUpdatedDate(new Date());
                promotedAdmin.setUpdatedBy(adminNik);

                adminRepository.save(promotedAdmin);
            }

            updateSupervisorSupervisingCount(supervisor.getNik(), adminNik);
        }

        employee.setUpdatedDate(new Date());
        employee.setUpdatedBy(adminNik);

        employeeRepository.save(employee);

    }

    @Override
    public boolean checkCyclicSupervisingExists(final String employeeNik, String supervisorNik) {

        String supervisorSupervisorNik = supervisionRepository.findByEmployeeNik(supervisorNik).getSupervisorNik();

        return supervisorSupervisorNik.equals(employeeNik);

    }

    /*-------------Delete Employee Methods-------------*/
    @Override
    public void deleteEmployee(final DeleteEmployeeRequest deleteEmployeeRequest) throws UnauthorizedOperationException,
                                                                                    DataNotFoundException, BadRequestException {

        if (!roleDeterminer.determineRole(deleteEmployeeRequest.getAdminNik()).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        if (deleteEmployeeRequest.getEmployeeNik().isEmpty())
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        if (deleteEmployeeRequest.getEmployeeNik().equals(deleteEmployeeRequest.getAdminNik()))
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        if (employeeRepository.findByNik(deleteEmployeeRequest.getEmployeeNik()) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if (employeeRepository.findByNik(deleteEmployeeRequest.getEmployeeNik()).getSupervisingCount() != 0)
            throw new UnauthorizedOperationException(EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT);

        if (!requestRepository.findAllByNikAndStatus(deleteEmployeeRequest.getEmployeeNik(), ServiceConstant.PENDING_RETURN).isEmpty())
            throw new UnauthorizedOperationException(UNRETURNED_ASSETS_ON_DELETION_ATTEMPT);

        if (!supervisionRepository.existsByEmployeeNik(deleteEmployeeRequest.getEmployeeNik()))
            throw new DataNotFoundException(USER_NOT_FOUND);

        List<RequestModel> requests = new ArrayList<>();
        requests.addAll(requestRepository.findAllByNikAndStatus(deleteEmployeeRequest.getEmployeeNik(), ServiceConstant.PENDING_HANDOVER));
        requests.addAll(requestRepository.findAllByNikAndStatus(deleteEmployeeRequest.getEmployeeNik(), ServiceConstant.REQUESTED));
        if (!requests.isEmpty()) {
            for (RequestModel request : requests) {
                request.setStatus(ServiceConstant.CANCELLED);
                request.setUpdatedDate(new Date());
                request.setUpdatedBy(deleteEmployeeRequest.getAdminNik());
                requestRepository.save(request);
            }
        }

        if (adminRepository.findByNik(deleteEmployeeRequest.getEmployeeNik()) != null)
            adminRepository.deleteByNik(deleteEmployeeRequest.getEmployeeNik());

        employeeRepository.deleteByNik(deleteEmployeeRequest.getEmployeeNik());

        supervisionRepository.deleteByEmployeeNik(deleteEmployeeRequest.getEmployeeNik());

    }

    @Override
    public void changeSupervisorOnPreviousSupervisorDeletion(final DeleteEmployeeSupervisorRequest deleteEmployeeSupervisorRequest) throws UnauthorizedOperationException, DataNotFoundException, BadRequestException {

        boolean isNotAdmin =
                !roleDeterminer.determineRole(deleteEmployeeSupervisorRequest.getAdminNik()).equals(ServiceConstant.ROLE_ADMINISTRATOR);
        if (isNotAdmin)
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        boolean requestDataMissing =
                deleteEmployeeSupervisorRequest.getOldSupervisorNik().isEmpty() || deleteEmployeeSupervisorRequest.getNewSupervisorNik().isEmpty();
        if (requestDataMissing)
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        boolean isIdenticalNik =
                deleteEmployeeSupervisorRequest.getOldSupervisorNik().equals(deleteEmployeeSupervisorRequest.getAdminNik());
        if (isIdenticalNik)
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        boolean userDoesNotExist =
                employeeRepository.findByNik(deleteEmployeeSupervisorRequest.getOldSupervisorNik()) == null || employeeRepository.findByNik(deleteEmployeeSupervisorRequest.getNewSupervisorNik()) == null;
        if (userDoesNotExist)
            throw new DataNotFoundException(USER_NOT_FOUND);

        boolean doesNotSupervise =
                supervisionRepository.findAllBySupervisorNik(deleteEmployeeSupervisorRequest.getOldSupervisorNik()).isEmpty();
        if (doesNotSupervise)
            throw new DataNotFoundException(SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE);

        List<SupervisionModel> supervisions = supervisionRepository.findAllBySupervisorNik(deleteEmployeeSupervisorRequest.getOldSupervisorNik());

        for (SupervisionModel supervision : supervisions) {
            EmployeeModel previousSupervisor = employeeRepository.findByNik(supervision.getSupervisorNik());
            previousSupervisor.setSupervisingCount(previousSupervisor.getSupervisingCount() - 1);
            employeeRepository.save(previousSupervisor);

            supervision.setSupervisorNik(deleteEmployeeSupervisorRequest.getNewSupervisorNik());
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(deleteEmployeeSupervisorRequest.getAdminNik());

            updateSupervisorSupervisingCount(deleteEmployeeSupervisorRequest.getNewSupervisorNik(), deleteEmployeeSupervisorRequest.getAdminNik());

            supervisionRepository.save(supervision);
        }
    }
}
