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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;
import static java.util.Objects.requireNonNull;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeesServiceImpl implements EmployeesServiceApi {

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    /*-------------Employees List Methods-------------*/
    @Override
    public List<EmployeeModel> getEmployeesList(
            final String query,
            final int page,
            final String sort
    ) throws BadRequestException, DataNotFoundException {

        if (query != null && query.equals("defaultQuery")) {
            throw new BadRequestException(EMPTY_SEARCH_QUERY);
        }

        if (query == null){
            int foundDataSize = employeeRepository.countAllByUsernameContains("");

            if (page < 1 || foundDataSize == 0) {
                throw new DataNotFoundException(ASSET_NOT_FOUND);
            }

            if ((int) Math.ceil((float) foundDataSize / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE) < page) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }

            return new ArrayList<>(getSortedEmployeesList(page, sort));
        } else {
             if (page < 1 || (int) Math.ceil((double) getEmployeesCount(query, sort)
                                             / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE) < page) {
                 throw new DataNotFoundException(USER_NOT_FOUND);
             }

             return new ArrayList<>(getSortedEmployeesListFromQuery(page, query, sort));

//            Set<EmployeeModel> employeesSet = new LinkedHashSet<>();
//
//            String[] queries = query.split(ServiceConstant.SPACE);
//
//            if (page < 1 || (int) Math.ceil((double) getEmployeesCount(query, sort)
//                                / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE) < page) {
//                throw new DataNotFoundException(USER_NOT_FOUND);
//            }
//
//            for (String word : queries) {
//                int foundDataSize = employeeRepository
//                        .countAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCase(
//                                word,
//                                word
//                        );
//
//                if (foundDataSize == 0) {
//                    continue;
//                }
//
//                employeesSet.addAll(getSortedEmployeesListFromQuery(page, word, sort));
//            }
//
//            List<EmployeeModel> employeesList = new ArrayList<>(employeesSet);
//
//            if (sort.equals(ServiceConstant.ASCENDING)) {
//                employeesList.sort(Comparator.comparing(EmployeeModel::getUsername));
//            } else {
//                employeesList.sort(Comparator.comparing(EmployeeModel::getUsername).reversed());
//            }
//
//            return employeesList;
        }
    }

    @Override
    public Set<EmployeeModel> getSortedEmployeesList(
            final int page,
            final String sort
    ) {

        if (sort.equals(ServiceConstant.ASCENDING)) {
            return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsOrderByNameAsc(
                    "",
                    PageRequest.of(page - 1, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)).getContent());
        } else if (sort.equals(ServiceConstant.DESCENDING)) {
            return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsOrderByNameDesc(
                    "",
                    PageRequest.of(page - 1, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)).getContent());
        }

        return new LinkedHashSet<>();
    }

    @Override
    public Set<EmployeeModel> getSortedEmployeesListFromQuery(
            final int page,
            final String query,
            final String sort
    ) {

        if (page == -1) {
            if (sort.equals(ServiceConstant.ASCENDING)) {
                return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                        query,
                        query));
            } else if (sort.equals(ServiceConstant.DESCENDING)) {
                return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                        query,
                        query));
            }
        } else {
            if (sort.equals(ServiceConstant.ASCENDING)) {
                return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameAsc(
                        query,
                        query,
                        PageRequest.of(page - 1, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)).getContent());
            } else if (sort.equals(ServiceConstant.DESCENDING)) {
                return new LinkedHashSet<>(employeeRepository.findAllByUsernameContainsIgnoreCaseOrNameContainsIgnoreCaseOrderByNameDesc(
                        query,
                        query,
                        PageRequest.of(page - 1, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)).getContent());
            }
        }

        return new LinkedHashSet<>();
    }

    @Override
    public List<String> getEmployeePhotos(
            final List<EmployeeModel> employees
    ) {

        List<String> photos = new ArrayList<>();
        for (EmployeeModel employee : employees) {
            photos.add(getEmployeeDetailPhoto(employeeRepository.findByUsername(employee.getUsername()).getUsername(),
                                              employeeRepository.findByUsername(employee.getUsername()).getPhoto()));
        }
        return photos;
    }

    @Override
    public int getEmployeesCount(
            final String query,
            final String sort
    ) {
        if (query == null){
            return employeeRepository.countAllByUsernameContains("");
        } else {
            return getSortedEmployeesListFromQuery(-1, query, sort).size();
        }
    }

    @Override
    public List<EmployeeModel> getSupervisorsList(
            final List<EmployeeModel> employees
    ) {
        List<EmployeeModel> supervisors = new ArrayList<>();

        for (EmployeeModel employee : employees) {
            if (supervisionRepository.findByEmployeeUsername(employee.getUsername()) == null) {
                // For top administrator, who does not have any supervisor at all
                supervisors.add(null);
            } else {
                supervisors.add(
                        employeeRepository.findByUsername(
                                supervisionRepository.findByEmployeeUsername(employee.getUsername()).getSupervisorUsername()
                        )
                );
            }
        }

        return supervisors;
    }

    @Override
    public List<String> getEmployeesUsername() {

        List<EmployeeModel> employees = employeeRepository.findAllByUsernameIsNotNullOrderByUsernameAsc();

        List<String> usernames = new ArrayList<>();
        for (EmployeeModel employee : employees) {
            usernames.add(employee.getUsername());
        }

        return usernames;
    }

    @Override
    public EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws DataNotFoundException {

        EmployeeModel employee = employeeRepository.findByUsername(username);

        if (employee == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        return employee;
    }

    @Override
    public String getEmployeeDetailPhoto(
            final String username,
            final String photoDirectory
    ) {

        if (photoDirectory == null || photoDirectory.isEmpty()) {
            return "http://localhost:8085/oasis/api/employees/" + username +
                   "/image_not_found"
                           .concat("?extension=jpeg");
        } else {
            File photo = new File(photoDirectory);
            if(Files.exists(photo.toPath())) {
                StringBuilder extensionBuilder = new StringBuilder();
                extensionBuilder.append(photo.getName());
                extensionBuilder.reverse();
                extensionBuilder.replace(
                        0,
                        extensionBuilder.length(),
                        extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf("."))
                );
                extensionBuilder.reverse();

                return "http://localhost:8085/oasis/api/employees/" + username +
                       "/" + username
                               .concat("?extension=")
                               .concat(String.valueOf(extensionBuilder));
            } else {
                return "http://localhost:8085/oasis/api/employees/" + username +
                       "/image_not_found"
                               .concat("?extension=jpeg");
            }
        }
    }

    @Override
    public byte[] getEmployeePhoto(
            final String username,
            final String photoName,
            final String extension
    ) {

        byte[] photo;

        File file = new File(employeeRepository.findByUsername(username).getPhoto());

        if (photoName.equals("image_not_found") || !file.getName().endsWith(extension)) {
            file = new File(ServiceConstant.RESOURCE_IMAGE_DIRECTORY.concat(File.separator).concat("image_not_found.jpeg"));
        }

        try {
            photo = Files.readAllBytes(file.toPath());
        } catch (IOException | NullPointerException exception) {
            photo = new byte[0];
        }

        return photo;

    }

    @Override
    public EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws DataNotFoundException {

        SupervisionModel supervision = supervisionRepository.findByEmployeeUsername(username);
        boolean isAdmin = roleDeterminer.determineRole(username).equals(ServiceConstant.ROLE_ADMINISTRATOR);

        if (supervision == null && !isAdmin)
            throw new DataNotFoundException(SUPERVISION_DATA_NOT_FOUND);

        if (isAdmin)
            return null;

        return employeeRepository.findByUsername(supervision.getSupervisorUsername());
    }

    /*-------------Save Employee Methods-------------*/
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveEmployee(
            final MultipartFile employeePhoto,
            final String username,
            final EmployeeModel employee,
            final String supervisorUsername,
            final boolean isAddOperation
    ) throws UnauthorizedOperationException, DataNotFoundException, DuplicateDataException, BadRequestException {

        if (!roleDeterminer.determineRole(username).equals(ServiceConstant.ROLE_ADMINISTRATOR)) {
            throw new UnauthorizedOperationException(ASSET_SAVE_ATTEMPT_BY_NON_ADMINISTRATOR);
        }

        EmployeeModel savedEmployee;
        if (isAddOperation) {
            if (!employee.getName().matches("^([A-Za-z]+ ?)*[A-Za-z]+$")) {
                //TODO throw real exception
                throw new BadRequestException(USER_NOT_FOUND);
            }

            savedEmployee = employee;

            if (employeeRepository.existsByNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
                    savedEmployee.getName(),
                    savedEmployee.getDob(),
                    savedEmployee.getPhone(),
                    savedEmployee.getJobTitle(),
                    savedEmployee.getDivision(),
                    savedEmployee.getLocation()
            )) {
                throw new DuplicateDataException(DUPLICATE_ASSET_DATA_FOUND);
            } else {
                savedEmployee.setUsername(generateEmployeeUsername(
                        savedEmployee.getName().toLowerCase(),
                        new SimpleDateFormat("dd-MM-yyyy").format(savedEmployee.getDob())
                ));
                savedEmployee.setPassword(generateEmployeeDefaultPassword(
                        new SimpleDateFormat("dd-MM-yyyy").format(savedEmployee.getDob()))
                );
                savedEmployee.setSupervisionId(getSupervisionId(
                        employee.getUsername(),
                        supervisorUsername,
                        username)
                );
                savedEmployee.setCreatedBy(username);
                savedEmployee.setCreatedDate(new Date());
            }
        } else {
            savedEmployee = employeeRepository.findByUsername(employee.getUsername());

            if (savedEmployee == null)
                throw new DataNotFoundException(ASSET_NOT_FOUND);

            savedEmployee.setName(employee.getName());
            savedEmployee.setDob(employee.getDob());
            savedEmployee.setPhone(employee.getPhone());
            savedEmployee.setJobTitle(employee.getJobTitle());
            savedEmployee.setDivision(employee.getDivision());
            savedEmployee.setLocation(employee.getLocation());
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            savedEmployee.setPassword(encoder.encode(employee.getPassword()));

            EmployeeModel supervisor = employeeRepository.findByUsername(supervisorUsername);
            if (supervisor == null) {
                throw new DataNotFoundException(USER_NOT_FOUND);
            } else if (!supervisorUsername.equals(supervisionRepository.findByEmployeeUsername(savedEmployee.getUsername()).getSupervisorUsername())) {
                SupervisionModel supervision = supervisionRepository.findByEmployeeUsername(savedEmployee.getUsername());

                EmployeeModel previousSupervisor = employeeRepository.findByUsername(supervision.getSupervisorUsername());
                employeeRepository.save(previousSupervisor);

                if (checkCyclicSupervisingExists(employee.getUsername(), supervisorUsername))
                    throw new UnauthorizedOperationException(CYCLIC_SUPERVISING_OCCURRED);

                supervision.setSupervisorUsername(supervisor.getUsername());
                supervision.setUpdatedDate(new Date());
                supervision.setUpdatedBy(username);
                supervisionRepository.save(supervision);

                if (supervisionRepository.existsSupervisionModelsBySupervisorUsername(employee.getUsername())) {
                    AdminModel promotedAdmin = new AdminModel();

                    promotedAdmin.setUsername(supervisor.getUsername());
                    promotedAdmin.setPassword(supervisor.getPassword());
                    promotedAdmin.setCreatedDate(new Date());
                    promotedAdmin.setCreatedBy(username);
                    promotedAdmin.setUpdatedDate(new Date());
                    promotedAdmin.setUpdatedBy(username);

                    adminRepository.save(promotedAdmin);
                }
            }
        }

        if (employeePhoto == null){
            throw new BadRequestException(MISSING_ASSET_IMAGE);
        } else {
            boolean rootDirectoryCreated;

            if (!Files.exists(Paths.get(ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY))) {
                rootDirectoryCreated = new File(ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY).mkdir();
            } else {
                rootDirectoryCreated = true;
            }

            if(rootDirectoryCreated){
                Path photoDir = Paths.get(ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY.concat(File.separator).concat(savedEmployee.getUsername()));

                if (!isAddOperation && Files.exists(photoDir)) {
                    File photo = new File(savedEmployee.getPhoto());
                    photo.delete();
                }

                StringBuilder extensionBuilder = new StringBuilder();
                extensionBuilder.append(employeePhoto.getOriginalFilename());
                extensionBuilder.reverse();
                extensionBuilder.replace(
                        0,
                        extensionBuilder.length(),
                        extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf(".") + 1)
                );
                extensionBuilder.reverse();

                String photoDirectory = ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY.concat(File.separator)
                                                                                .concat(savedEmployee.getUsername())
                                                                                .concat(String.valueOf(extensionBuilder)
                                                                                );
                savedEmployee.setPhoto(photoDirectory);

                savePhoto(employeePhoto, savedEmployee.getUsername());
            }
        }

        savedEmployee.setUpdatedDate(new Date());
        savedEmployee.setUpdatedBy(username);

        employeeRepository.save(savedEmployee);
    }

    @Override
    public String generateEmployeeUsername(
            final String name,
            final String dob
    ) {

        StringBuilder username = new StringBuilder();

        if (name.contains(" ")) {
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

        if (employeeRepository.findByUsername(String.valueOf(username)) != null) {
            int suffix = employeeRepository.findAllByUsernameContains(String.valueOf(username)).size();
            username.append(suffix);
        }

        return String.valueOf(username);
    }

    @Override
    public String generateEmployeeDefaultPassword(
            final String dob
    ) {

        StringBuilder password = new StringBuilder(ServiceConstant.NIK_PREFIX.toLowerCase());

        password.append(dob.replace("-", ""));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        password.replace(0, password.length(), encoder.encode(String.valueOf(password)));

        return String.valueOf(password);
    }

    @Override
    public void createSupervision(
            final String employeeUsername,
            final String supervisorUsername,
            final String adminUsername
    ) {

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
    public String getSupervisionId(
            final String employeeUsername,
            final String supervisorUsername,
            final String adminUsername
    )
            throws DataNotFoundException {

        if (employeeRepository.findByUsername(supervisorUsername) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        createSupervision(employeeUsername, supervisorUsername, adminUsername);

        return supervisionRepository.findByEmployeeUsername(employeeUsername).get_id();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void savePhoto(
            final MultipartFile employeePhoto,
            final String username
    ) {

        if (employeePhoto != null) {
            try {
                StringBuilder extensionBuilder = new StringBuilder();
                extensionBuilder.append(employeePhoto.getOriginalFilename());
                extensionBuilder.reverse();
                extensionBuilder.replace(
                        0,
                        extensionBuilder.length(),
                        extensionBuilder.substring(0, String.valueOf(extensionBuilder).indexOf(".") + 1)
                );
                extensionBuilder.reverse();
                File photo = new File(
                        ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY.concat(File.separator).concat(username)
                                                                .concat(String.valueOf(extensionBuilder)));

                employeePhoto.transferTo(photo);
            } catch (IOException ioException) {
                //TODO throw real exception cause
            }
        }
    }

    @Override
    public boolean checkCyclicSupervisingExists(
            final String employeeUsername,
            String supervisorUsername
    ) {

        String supervisorSupervisorNik = supervisionRepository.findByEmployeeUsername(supervisorUsername).getSupervisorUsername();

        return supervisorSupervisorNik.equals(employeeUsername);
    }

    /*-------------Delete Employee Methods-------------*/
    @Override
    public void deleteEmployee(
            final String adminUsername,
            final String employeeUsername
    )
            throws UnauthorizedOperationException,
                   DataNotFoundException,
                   BadRequestException {

        if (!roleDeterminer.determineRole(adminUsername).equals(ServiceConstant.ROLE_ADMINISTRATOR))
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        if (employeeUsername.isEmpty())
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        if (employeeUsername.equals(adminUsername))
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        if (employeeRepository.findByUsername(employeeUsername) == null)
            throw new DataNotFoundException(USER_NOT_FOUND);

        if (supervisionRepository.existsSupervisionModelsBySupervisorUsername(employeeUsername))
            throw new UnauthorizedOperationException(EXISTING_SUPERVISED_EMPLOYEES_ON_DELETION_ATTEMPT);

        if (!requestRepository.findAllByUsernameAndStatus(employeeUsername, ServiceConstant.PENDING_RETURN).isEmpty())
            throw new UnauthorizedOperationException(UNRETURNED_ASSETS_ON_DELETION_ATTEMPT);

        if (!supervisionRepository.existsByEmployeeUsername(employeeUsername))
            throw new DataNotFoundException(USER_NOT_FOUND);

        List<RequestModel> requests = new ArrayList<>();
        requests.addAll(requestRepository.findAllByUsernameAndStatus(employeeUsername,
                                                                ServiceConstant.PENDING_HANDOVER));
        requests.addAll(requestRepository.findAllByUsernameAndStatus(employeeUsername, ServiceConstant.REQUESTED));
        if (!requests.isEmpty()) {
            for (RequestModel employeeRequest : requests) {
                employeeRequest.setStatus(ServiceConstant.CANCELLED);
                employeeRequest.setUpdatedDate(new Date());
                employeeRequest.setUpdatedBy(adminUsername);

                requestRepository.save(employeeRequest);
            }
        }

        if (adminRepository.findByUsername(employeeUsername) != null)
            adminRepository.deleteByUsername(employeeUsername);

        employeeRepository.deleteByUsername(employeeUsername);

        supervisionRepository.deleteByEmployeeUsername(employeeUsername);
    }

    @Override
    public void changeSupervisorOnPreviousSupervisorDeletion(
            final String adminUsername,
            final String oldSupervisorUsername,
            final String newSupervisorUsername
    )
            throws UnauthorizedOperationException,
                   DataNotFoundException,
                   BadRequestException {

        boolean isNotAdmin =
                !roleDeterminer.determineRole(adminUsername).equals(ServiceConstant.ROLE_ADMINISTRATOR);
        if (isNotAdmin)
            throw new UnauthorizedOperationException(EMPLOYEE_DELETE_ATTEMPT_BY_NON_ADMINISTRATOR);

        boolean requestDataMissing = oldSupervisorUsername.isEmpty() || newSupervisorUsername.isEmpty();
        if (requestDataMissing)
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);

        boolean isIdenticalNik = oldSupervisorUsername.equals(adminUsername);
        if (isIdenticalNik)
            throw new UnauthorizedOperationException(SELF_DELETION_ATTEMPT);

        boolean userDoesNotExist =
                employeeRepository.findByUsername(oldSupervisorUsername) == null ||
                employeeRepository.findByUsername(newSupervisorUsername) == null;
        if (userDoesNotExist)
            throw new DataNotFoundException(USER_NOT_FOUND);

        boolean doesNotSupervise =
                supervisionRepository.findAllBySupervisorUsername(oldSupervisorUsername).isEmpty();
        if (doesNotSupervise)
            throw new DataNotFoundException(SELECTED_EMPLOYEE_DOES_NOT_SUPERVISE);

        List<SupervisionModel> supervisions =
                supervisionRepository.findAllBySupervisorUsername(oldSupervisorUsername);

        for (SupervisionModel supervision : supervisions) {
            EmployeeModel previousSupervisor = employeeRepository.findByUsername(supervision.getSupervisorUsername());
            employeeRepository.save(previousSupervisor);

            supervision.setSupervisorUsername(newSupervisorUsername);
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(adminUsername);

            supervisionRepository.save(supervision);
        }
    }

}
