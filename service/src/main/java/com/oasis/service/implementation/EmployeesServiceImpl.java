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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
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

    @Override
    public List<EmployeeListResponse.Employee> getAllEmployees(int pageNumber, String sortInfo) throws DataNotFoundException {
        if (pageNumber < 1 || employeeRepository.findAll().size() == 0) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        }

        if ((int)
                Math.ceil(
                        (float) employeeRepository.findAll().size()
                                / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                < pageNumber) {
            throw new DataNotFoundException(USER_NOT_FOUND);
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
                                employeeFound.getName(),
                                employeeFound.getJobTitle(),
                                employeeFound.getLocation(),
                                new EmployeeListResponse.Employee.Supervisor(
                                        supervisor.getNik(),
                                        supervisor.getName()
                                )
                        );
            } else {
                employee =
                        new EmployeeListResponse.Employee(
                                employeeFound.getNik(),
                                employeeFound.getName(),
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
            throw new DataNotFoundException(INCORRECT_EMPLOYEE_NIK);

        return employee;
    }

    @Override
    public EmployeeModel getEmployeeSupervisorData(String employeeNik) throws DataNotFoundException {
        SupervisionModel supervision = supervisionRepository.findByEmployeeNik(employeeNik);
        boolean isAdmin = roleDeterminer.determineRole(employeeNik).equals(ServiceConstant.ROLE_ADMINISTRATOR);

        if (supervision == null && !isAdmin)
            throw new DataNotFoundException(SUPERVISION_DATA_NOT_FOUND);

        if (!isAdmin) {
            return employeeRepository.findByNik(supervision.getSupervisorNik());
        }

        return null;
    }

    @Override
    public List<EmployeeListResponse.Employee> getEmployeesBySearchQuery(String searchQuery, int pageNumber, String sortInfo) throws BadRequestException, DataNotFoundException {
        if (searchQuery.isEmpty())
            throw new BadRequestException(EMPTY_SEARCH_QUERY);

        Set<EmployeeModel> employeesFound = new HashSet<>();

        if (!searchQuery.contains(" ")) {
            if (pageNumber < 1 || employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(searchQuery, searchQuery).size() == 0) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }

            if ((int)
                    Math.ceil(
                            (float)
                                    employeeRepository
                                            .findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(searchQuery, searchQuery)
                                            .size()
                                    / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                    < pageNumber) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }

            employeesFound.addAll(fillData(searchQuery, sortInfo));
        } else {
            String[] queries = searchQuery.split(" ");

            for (String query : queries) {
                if (pageNumber < 1 || (employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(query, query).size() == 0 &&
                                       employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(query.toLowerCase(), query.toLowerCase()).size() == 0)) {
                    throw new DataNotFoundException(USER_NOT_FOUND);
                }

                if ((int)
                        Math.ceil(
                                (float) employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCase(query, query).size()
                                        / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                        < pageNumber) {
                    throw new DataNotFoundException(USER_NOT_FOUND);
                }

                employeesFound.addAll(fillData(query, sortInfo));
            }
        }

        return mapEmployeesFound(employeesFound);
    }

    @Override
    public Set<EmployeeModel> fillData(String searchQuery, String sortInfo) {
        Set<EmployeeModel> employeesFound = new LinkedHashSet<>();

        if (sortInfo.substring(1).equals("nik")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikAsc(searchQuery, searchQuery));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNikDesc(searchQuery, searchQuery));
            }
        } else if (sortInfo.substring(1).equals("employeeFullname")) {
            if (sortInfo.substring(0, 1).equals("A")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(searchQuery, searchQuery));
            } else if (sortInfo.substring(0, 1).equals("D")) {
                employeesFound.addAll(
                        employeeRepository.findAllByNikContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(searchQuery, searchQuery));
            }
        }

        return employeesFound;
    }

    @Override
    public void insertToDatabase(AddEmployeeRequest.Employee employeeRequest, String adminNik) throws UnauthorizedOperationException, DataNotFoundException, DuplicateDataException {

        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_INSERTION_ATTEMPT_BY_NON_ADMINISTRATOR);

        List<EmployeeModel> matchingEmployees = new ArrayList<>();
        try {
            matchingEmployees.addAll(employeeRepository.findAllByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
                    employeeRequest.getFullname(),
                    new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getDob()),
                    employeeRequest.getPhone(),
                    employeeRequest.getJobTitle(),
                    employeeRequest.getDivision(),
                    employeeRequest.getLocation()
            ));
        } catch (ParseException e) {
            //TODO handle exception
        }

        if (!matchingEmployees.isEmpty()) {
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
                //TODO Handle exception
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
    public String generateEmployeeNik(String division) {
        StringBuilder nik = new StringBuilder();

        nik.append(ServiceConstant.NIK_PREFIX);

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
        } else {
            username.append(fullname);
        }

        if (employeeRepository.findByUsername(String.valueOf(username).concat("@gdn-commerce.com")) != null) {
            int suffix = employeeRepository.findAllByUsernameContains(String.valueOf(username)).size();
            username.append(suffix);
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
    public void updateSupervisorSupervisingCount(String supervisorNik, String adminNik) {
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
            throw new DataNotFoundException(USER_NOT_FOUND);

        createSupervision(employeeNik, supervisorNik, adminNik);

        return supervisionRepository.findByEmployeeNik(employeeNik).get_id();
    }

    @Override
    public void updateEmployee(UpdateEmployeeRequest.Employee employeeRequest, String adminNik) throws DataNotFoundException, UnauthorizedOperationException {
        if (!roleDeterminer.determineRole(adminNik).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(EMPLOYEE_UPDATE_ATTEMPT_BY_NON_ADMINISTRATOR);
        }

        EmployeeModel employee = employeeRepository.findByNik(employeeRequest.getEmployeeNik());

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        Date requestDob = null;
        try {
            requestDob = new SimpleDateFormat("dd-MM-yyyy").parse(employeeRequest.getEmployeeDob());
        } catch (ParseException e) {
            //TODO Handle exception
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

            if (checkCyclicSupervisingExists(employee.getNik(), employeeRequest.getEmployeeSupervisorId())) {
                throw new UnauthorizedOperationException(CYCLIC_SUPERVISING_OCCURED);
            }

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
    public boolean checkCyclicSupervisingExists(String employeeNik, String supervisorNik) {
        String supervisorSupervisorNik = supervisionRepository.findByEmployeeNik(supervisorNik).getSupervisorNik();

        return supervisorSupervisorNik.equals(employeeNik);
    }

    @Override
    public void deleteEmployee(DeleteEmployeeRequest deleteEmployeeRequest) throws UnauthorizedOperationException, DataNotFoundException, BadRequestException {

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
            throw new UnauthorizedOperationException(EXISTING_USED_ASSETS_ON_DELETION_ATTEMPT);

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

        if (adminRepository.findByNik(deleteEmployeeRequest.getEmployeeNik()) != null) {
            adminRepository.deleteByNik(deleteEmployeeRequest.getEmployeeNik());
        }

        employeeRepository.deleteByNik(deleteEmployeeRequest.getEmployeeNik());
    }

    @Override
    public void changeSupervisorOnPreviousSupervisorDeletion(DeleteEmployeeSupervisorRequest deleteEmployeeSupervisorRequest) throws UnauthorizedOperationException, DataNotFoundException, BadRequestException {

        if (!roleDeterminer.determineRole(deleteEmployeeSupervisorRequest.getAdminNik()).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        if (deleteEmployeeSupervisorRequest.getOldSupervisorNik().isEmpty() || deleteEmployeeSupervisorRequest.getNewSupervisorNik().isEmpty())
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        if (deleteEmployeeSupervisorRequest.getOldSupervisorNik().equals(deleteEmployeeSupervisorRequest.getAdminNik()))
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        if (employeeRepository.findByNik(deleteEmployeeSupervisorRequest.getOldSupervisorNik()) == null || employeeRepository.findByNik(deleteEmployeeSupervisorRequest.getNewSupervisorNik()) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if (supervisionRepository.findAllBySupervisorNik(deleteEmployeeSupervisorRequest.getOldSupervisorNik()).isEmpty())
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
