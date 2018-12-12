package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.*;
import com.oasis.model.entity.AdminModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ImageHelper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.EmployeesServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeesServiceImpl
        implements EmployeesServiceApi {

    private Logger logger = LoggerFactory.getLogger(EmployeesServiceImpl.class);
    @Autowired
    private ImageHelper imageHelper;
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
    @Cacheable(value = "employeesList", unless = "#result.size() == 0")
    public List< EmployeeModel > getEmployeesList(
            final String username, final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = (query != null && query.isEmpty());
        final boolean emptySortGiven = (sort != null && sort.isEmpty());

        if (emptyQueryGiven || emptySortGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        final boolean useParameterSort = (sort != null);

        if (useParameterSort) {
            final boolean properSortFormatGiven = sort.matches("^[AD]$");

            if (!properSortFormatGiven) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            }
        } else {
            sort = "A";
        }

        final Set< EmployeeModel > employees;
        final long employeesCount;
        final long availablePages;
        final boolean noEmployees;
        final boolean pageIndexOutOfBounds;

        final boolean viewAllEmployees = (query == null);

        if (viewAllEmployees) {
            employeesCount = employeeRepository.countAllByDeletedIsFalseAndUsernameIsNot(username);
            availablePages = (long) Math.ceil((float) employeesCount / ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE);

            noEmployees = (employeesCount == 0);
            pageIndexOutOfBounds = ((page < 1) || (page > availablePages));

            if (noEmployees || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            employees = new LinkedHashSet<>(getSortedEmployees(username, page, sort));
        } else {
            employeesCount = employeeRepository
                    .countAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCase(
                            query, query);
            availablePages = (long) Math.ceil((double) employeesCount / ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE);

            noEmployees = (employeesCount == 0);
            pageIndexOutOfBounds = ((page < 1) || (page > availablePages));

            if (noEmployees || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            employees = new LinkedHashSet<>(getSortedEmployeesFromQuery(page, query, sort));
        }

        return new ArrayList<>(employees);
    }

    @Override
    public Set< EmployeeModel > getSortedEmployees(
            final String username, final int page, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE);

        if (sort.equals(ServiceConstant.ASCENDING)) {
            sortedEmployees
                    .addAll(employeeRepository.findAllByDeletedIsFalseAndUsernameIsNotOrderByNameAsc(username, pageable)
                                              .getContent());
        } else {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameIsNotOrderByNameDesc(username, pageable)
                                           .getContent());
        }

        return sortedEmployees;
    }

    @Override
    public Set< EmployeeModel > getSortedEmployeesFromQuery(
            final int page, final String query, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE);

        if (sort.equals(ServiceConstant.ASCENDING)) {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameAsc(
                                                   query, query, pageable).getContent());
        } else {
            sortedEmployees.addAll(employeeRepository
                                           .findAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCaseOrderByNameDesc(
                                                   query, query, pageable).getContent());
        }

        return sortedEmployees;
    }

    @Override
    public long getEmployeesCount(
            final String username, final String query, String sort
    ) {

        final boolean viewAllEmployees = (query == null);

        if (viewAllEmployees) {
            return employeeRepository.countAllByDeletedIsFalseAndUsernameIsNot(username);
        } else {
            return employeeRepository
                    .countAllByDeletedIsFalseAndUsernameContainsIgnoreCaseOrDeletedIsFalseAndNameContainsIgnoreCase(
                            query, query);
        }
    }

    @Override
    public List< EmployeeModel > getSupervisorsList(
            final List< EmployeeModel > employees
    ) {

        List< EmployeeModel > supervisors = new ArrayList<>();

        for (final EmployeeModel employee : employees) {

            final SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsername(employee.getUsername());

            final boolean supervisionForCurrentEmployeeExists = (supervision != null);

            if (supervisionForCurrentEmployeeExists) {
                final EmployeeModel supervisor = employeeRepository
                        .findByDeletedIsFalseAndUsername(supervision.getSupervisorUsername());

                supervisors.add(supervisor);
            } else {    // For top administrator, who does not have any supervisor at all
                supervisors.add(null);
            }
        }

        return supervisors;
    }

    @Override
    @Cacheable(value = "employeeDetailData", key = "#username")
    public EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException {

        final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsername(username);

        final boolean employeeWithUsernameExists = (employee != null);

        if (!employeeWithUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        return employee;
    }

    @Override
    public String getEmployeeDetailImage(
            final String username, final String imageLocation
    ) {

        final boolean validImageLocation = (imageLocation != null && imageLocation.isEmpty());

        if (validImageLocation) {
            final File image = new File(imageLocation);

            if (image.exists() && Files.exists(image.toPath())) {
                return "http://localhost:8085/oasis/api/employees/" + username + "/" +
                       username.concat("?extension=").concat(imageHelper.getExtensionFromFileName(image.getName()));
            }
        }

        return "http://localhost:8085/oasis/api/employees/" + username + "/image_not_found".concat("?extension=jpeg");
    }

    @Override
    public List< String > getEmployeesUsernamesForSupervisorSelection(
            final String username
    )
            throws
            BadRequestException {

        final boolean emptyUsernameGiven = username.isEmpty();

        if (emptyUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        Set< String > possibleSupervisorsUsernames = new LinkedHashSet<>();
        final List< EmployeeModel > employees = employeeRepository
                .findAllByDeletedIsFalseAndUsernameIsNotNullOrderByUsernameAsc();

        final boolean createEmployeeOperation = username.equals("-1");

        if (createEmployeeOperation) {
            for (final EmployeeModel possibleSupervisor : employees) {
                possibleSupervisorsUsernames.add(possibleSupervisor.getUsername());
            }
        } else {
            List< String > supervisedEmployeesUsernames = new ArrayList<>();
            final List< SupervisionModel > supervisions = supervisionRepository
                    .findAllByDeletedIsFalseAndSupervisorUsername(username);

            for (final SupervisionModel supervision : supervisions) {
                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
            }

            for (final EmployeeModel possibleSupervisor : employees) {
                final String currentCandidateUsername = possibleSupervisor.getUsername();

                if (username.equals(currentCandidateUsername)) {
                    continue;
                }

                boolean safeFromCyclicSupervising = isSafeFromCyclicSupervising(
                        currentCandidateUsername, supervisedEmployeesUsernames);

                if (safeFromCyclicSupervising) {
                    possibleSupervisorsUsernames.add(currentCandidateUsername);
                }
            }
        }

        return new ArrayList<>(possibleSupervisorsUsernames);
    }

    @Override
    public boolean isSafeFromCyclicSupervising(
            final String targetUsername, final List< String > usernames
    ) {

        if (usernames.size() - 1 >= 1) {
            final String middleUsername = usernames.get(usernames.size() / 2);

            if (targetUsername.equals(middleUsername)) {
                return false;
            }

            if (targetUsername.compareTo(middleUsername) < 0) {
                return isSafeFromCyclicSupervising(
                        targetUsername, usernames.subList(0, usernames.indexOf(middleUsername) - 1));
            }
            if (targetUsername.compareTo(middleUsername) > 0) {
                return isSafeFromCyclicSupervising(
                        targetUsername, usernames.subList(usernames.indexOf(middleUsername) + 1, usernames.size()));
            }
        }

        return true;
    }

    @Override
    public byte[] getEmployeeImage(
            final String username, final String imageName, final String extension
    ) {

        final boolean employeeWithUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsername(username);

        if (!employeeWithUsernameExists) {
            logger.info("Failed to load employee image as username does not refer any employee in database");
            return new byte[0];
        }

        final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsername(username);

        File file = new File(employee.getPhoto());

        final boolean imageNameIsImageNotFound = imageName.equals("image_not_found");
        final boolean correctExtensionForImage = file.getName().endsWith(extension);

        if (!correctExtensionForImage || imageNameIsImageNotFound) {
            file = new File(
                    ServiceConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator).concat("image_not_found.jpeg"));
        }

        final byte[] image;

        try {
            image = Files.readAllBytes(file.toPath());
        } catch (IOException | NullPointerException exception) {
            logger.error("Failed to read image as IOException or NullPointerException occurred with message " +
                         exception.getMessage());
            return new byte[0];
        }

        return image;

    }

    @Override
    public List< String > getEmployeesImages(
            final List< EmployeeModel > employees
    ) {

        List< String > images = new ArrayList<>();

        for (final EmployeeModel employee : employees) {
            images.add(getEmployeeDetailImage(employee.getUsername(), employee.getPhoto()));
        }

        return images;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException {

        final SupervisionModel supervision = supervisionRepository.findByDeletedIsFalseAndEmployeeUsername(username);
        final boolean employeeIsAdministrator = roleDeterminer.determineRole(username)
                                                              .equals(ServiceConstant.ROLE_ADMINISTRATOR);

        final boolean noSupervisionForEmployeeWithUsername = (supervision == null);

        if (noSupervisionForEmployeeWithUsername && !employeeIsAdministrator) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        final boolean employeeIsTopAdministrator = noSupervisionForEmployeeWithUsername && employeeIsAdministrator;

        if (employeeIsTopAdministrator) {
            return null;
        }

        return employeeRepository.findByDeletedIsFalseAndUsername(supervision.getSupervisorUsername());
    }

    /*-------------Save Employee Methods-------------*/
    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    @Caching(evict = {
            @CacheEvict(value = "employeeDetailData", key = "#employee.username"),
            @CacheEvict(value = "employeesList", allEntries = true)
    })
    public String saveEmployee(
            final MultipartFile imageGiven, final String username, final EmployeeModel employee,
            final String supervisorUsername, final boolean createEmployeeOperation
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException {

        final boolean validAdministrator = roleDeterminer.determineRole(username)
                                                         .equals(ServiceConstant.ROLE_ADMINISTRATOR);
        if (!validAdministrator) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        }

        EmployeeModel savedEmployee;

        if (createEmployeeOperation) {
            final boolean properNameFormatGiven = employee.getName().matches("^([A-Za-z]+ ?)*[A-Za-z]+$");
            final boolean noImageGiven = (imageGiven == null);

            if (!properNameFormatGiven || noImageGiven) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            }

            savedEmployee = employee;

            final boolean potentialDuplicateEmployeeData = employeeRepository
                    .existsByDeletedIsFalseAndNameAndDobAndPhoneAndJobTitleAndDivisionAndLocation(
                            savedEmployee.getName(), savedEmployee.getDob(), savedEmployee.getPhone(),
                            savedEmployee.getJobTitle(), savedEmployee.getDivision(), savedEmployee.getLocation()
                    );

            if (potentialDuplicateEmployeeData == true) {
                throw new DuplicateDataException(DUPLICATE_DATA_FOUND);
            } else {
                final String dobString = new SimpleDateFormat("dd-MM-yyyy").format(savedEmployee.getDob());

                savedEmployee.setUsername(generateUsername(savedEmployee.getName().toLowerCase(), dobString));
                savedEmployee.setPassword(generateDefaultPassword(dobString));
                savedEmployee.setSupervisionId(getSupervisionId(employee.getUsername(), supervisorUsername, username));
                savedEmployee.setDeleted(false);
                savedEmployee.setCreatedBy(username);
                savedEmployee.setCreatedDate(new Date());
            }
        } else {
            savedEmployee = employeeRepository.findByDeletedIsFalseAndUsername(employee.getUsername());

            if (savedEmployee == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            if (savedEmployee.equals(employee)) {
                throw new UnauthorizedOperationException(SAME_DATA_ON_UPDATE);
            }

            savedEmployee.setName(employee.getName());
            savedEmployee.setDob(employee.getDob());
            savedEmployee.setPhone(employee.getPhone());
            savedEmployee.setJobTitle(employee.getJobTitle());
            savedEmployee.setDivision(employee.getDivision());
            savedEmployee.setLocation(employee.getLocation());

            updateSupervisorDataOnEmployeeUpdate(username, savedEmployee, employee.getUsername(), supervisorUsername);
        }

        validateAndSaveImage(imageGiven, createEmployeeOperation, savedEmployee);

        savedEmployee.setUpdatedDate(new Date());
        savedEmployee.setUpdatedBy(username);

        employeeRepository.save(savedEmployee);

        return savedEmployee.getUsername();
    }

    @Override
    public void updateSupervisorDataOnEmployeeUpdate(
            final String username, final EmployeeModel savedEmployee, final String employeeUsername,
            final String supervisorUsername
    )
            throws
            DataNotFoundException,
            UnauthorizedOperationException {

        final EmployeeModel supervisor = employeeRepository.findByDeletedIsFalseAndUsername(supervisorUsername);

        if (supervisor == null) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsername(savedEmployee.getUsername());

            if (supervisorUsername.equals(supervision.getSupervisorUsername())) {
                throw new UnauthorizedOperationException(SAME_DATA_ON_UPDATE);
            } else {
                if (hasCyclicSupervising(employeeUsername, supervisorUsername)) {
                    throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                }

                supervision.setSupervisorUsername(supervisor.getUsername());
                supervision.setUpdatedDate(new Date());
                supervision.setUpdatedBy(username);

                supervisionRepository.save(supervision);

                if (supervisionRepository
                        .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(employeeUsername)) {
                    AdminModel promotedAdmin;

                    final boolean adminWithUsernameAndIsDeletedExists = adminRepository
                            .existsAdminModelByDeletedIsTrueAndUsernameEquals(supervisor.getUsername());

                    if (adminWithUsernameAndIsDeletedExists) {
                        promotedAdmin = adminRepository.findByDeletedIsTrueAndUsernameEquals(supervisor.getUsername());
                    } else {
                        promotedAdmin = new AdminModel();

                        promotedAdmin.setUsername(supervisor.getUsername());
                        promotedAdmin.setPassword(supervisor.getPassword());
                        promotedAdmin.setCreatedDate(new Date());
                        promotedAdmin.setCreatedBy(username);
                    }
                    promotedAdmin.setDeleted(false);
                    promotedAdmin.setUpdatedDate(new Date());
                    promotedAdmin.setUpdatedBy(username);

                    adminRepository.save(promotedAdmin);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void validateAndSaveImage(
            final MultipartFile imageGiven, final boolean createEmployeeOperation, final EmployeeModel savedEmployee
    ) {

        if (imageGiven != null) {
            boolean rootDirectoryCreated;

            if (!Files.exists(Paths.get(ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY))) {
                rootDirectoryCreated = new File(ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY).mkdir();
            } else {
                rootDirectoryCreated = true;
            }

            if (rootDirectoryCreated) {
                if (!createEmployeeOperation && Files.exists(Paths.get(savedEmployee.getPhoto()))) {
                    File image = new File(savedEmployee.getPhoto());
                    image.delete();
                }

                final String imageLocation = ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY.concat(File.separator)
                                                                                     .concat(savedEmployee
                                                                                                     .getUsername())
                                                                                     .concat(".").concat(imageHelper
                                                                                                                 .getExtensionFromFileName(
                                                                                                                         imageGiven
                                                                                                                                 .getOriginalFilename()));
                savedEmployee.setPhoto(imageLocation);

                savePhoto(imageGiven, savedEmployee.getUsername());
            }
        }
    }

    @Override
    public String generateUsername(
            final String name, final String dobString
    ) {

        StringBuilder username = new StringBuilder();

        if (name.contains(" ")) {
            final String givenName = name.substring(0, name.lastIndexOf(" "));
            final String firstNames[] = givenName.split(" ");

            for (final String firstName : firstNames) {
                username.append(firstName.charAt(0));
                username.append(".");
            }

            final String lastName = name.substring(name.lastIndexOf(" ") + 1);

            username.append(lastName);
        } else {
            username.append(name);
        }

        final long usernamesCountStartingWithGeneratedUsername = employeeRepository
                .countAllByUsernameStartsWith(String.valueOf(username));
        final boolean moreThanOneUsernameStartingWithGeneratedUsername = (
                usernamesCountStartingWithGeneratedUsername != 0
        );

        if (moreThanOneUsernameStartingWithGeneratedUsername) {
            username.append(usernamesCountStartingWithGeneratedUsername);
        }

        return String.valueOf(username);
    }

    @Override
    public String generateDefaultPassword(
            final String dobString
    ) {

        StringBuilder password = new StringBuilder(ServiceConstant.PREFIX_DEFAULT_PASSWORD);

        final String dobWithoutDash = dobString.replace("-", "");

        password.append(dobWithoutDash);

        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        final String encodedPassword = encoder.encode(String.valueOf(password));

        password.replace(0, password.length(), encodedPassword);

        return String.valueOf(password);
    }

    @Override
    public String getSupervisionId(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    )
            throws
            DataNotFoundException {

        final boolean employeeWithSupervisorUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsername(supervisorUsername);

        if (!employeeWithSupervisorUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        createSupervision(employeeUsername, supervisorUsername, adminUsername);

        final SupervisionModel createdSupervision = supervisionRepository
                .findByDeletedIsFalseAndEmployeeUsername(employeeUsername);

        return createdSupervision.get_id();
    }

    @Override
    public void createSupervision(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
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
    @SuppressWarnings("UnnecessaryLocalVariable")
    public boolean hasCyclicSupervising(
            final String employeeUsername, String supervisorUsername
    ) {

        final String supervisorOfSupervisorUsername = supervisionRepository
                .findByDeletedIsFalseAndEmployeeUsername(supervisorUsername).getSupervisorUsername();

        final boolean isEmployeeSupervisorOfSupervisor = supervisorOfSupervisorUsername.equals(employeeUsername);

        return isEmployeeSupervisorOfSupervisor;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void savePhoto(
            final MultipartFile photoFile, final String username
    ) {

        if (photoFile != null) {
            try {
                File image = new File(
                        ServiceConstant.EMPLOYEE_IMAGE_DIRECTORY.concat(File.separator).concat(username).concat(".")
                                                                .concat(imageHelper.getExtensionFromFileName(
                                                                        photoFile.getOriginalFilename())));

                photoFile.transferTo(image);
            } catch (IOException ioException) {
                logger.error("Failed to save image as IOException occurred with message " + ioException.getMessage());
            }
        }
    }

    @Override
    public void changePassword(
            final String username, final String oldPassword, final String newPassword,
            final String newPasswordConfirmation
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException,
            BadRequestException {

        final boolean emptyUsernameGiven = (username == null || username.isEmpty());
        final boolean emptyOldPasswordGiven = (oldPassword == null || oldPassword.isEmpty());
        final boolean emptyNewPasswordGiven = (newPassword == null || newPassword.isEmpty());
        final boolean emptyNewPasswordConfirmationGiven = (
                newPasswordConfirmation == null || newPasswordConfirmation.isEmpty()
        );

        if (emptyUsernameGiven || emptyOldPasswordGiven || emptyNewPasswordGiven || emptyNewPasswordConfirmationGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsername(username);

        if (employee == null) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            final boolean passwordsMatch = encoder.matches(oldPassword, employee.getPassword());

            if (!passwordsMatch) {
                throw new UserNotAuthenticatedException(INVALID_PASSWORD);
            } else {
                final boolean unchangedPassword = oldPassword.equals(newPassword);
                final boolean newPasswordConfirmed = newPassword.equals(newPasswordConfirmation);

                if (unchangedPassword || !newPasswordConfirmed) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    employee.setPassword(encoder.encode(newPassword));

                    employee.setUpdatedBy(username);
                    employee.setUpdatedDate(new Date());

                    employeeRepository.save(employee);
                }
            }
        }
    }

    /*-------------Delete Employee Methods-------------*/
    @Override
    @CacheEvict(value = { "employeesList", "employeeDetailData" }, allEntries = true)
    public void deleteEmployee(
            final String adminUsername, final String employeeUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        final boolean emptyEmployeeUsernameGiven = employeeUsername.isEmpty();
        final boolean emptyAdministratorUsernameGiven = adminUsername.isEmpty();

        if (emptyAdministratorUsernameGiven || emptyEmployeeUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                                                         .equals(ServiceConstant.ROLE_ADMINISTRATOR);
        final boolean selfDeletionAttempt = employeeUsername.equals(adminUsername);
        final boolean employeeWithEmployeeUsernameStillSupervises = supervisionRepository
                .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(employeeUsername);
        final boolean allDeliveredAssetsHaveBeenReturned = requestRepository
                .findAllByUsernameAndStatus(employeeUsername, ServiceConstant.STATUS_DELIVERED).isEmpty();

        if (!validAdministrator || selfDeletionAttempt || employeeWithEmployeeUsernameStillSupervises ||
            !allDeliveredAssetsHaveBeenReturned) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        }

        EmployeeModel targetEmployee = employeeRepository.findByDeletedIsFalseAndUsername(employeeUsername);
        SupervisionModel supervisionOfTargetEmployee = supervisionRepository
                .findByDeletedIsFalseAndEmployeeUsername(employeeUsername);

        final boolean targetEmployeeDoesNotExist = (targetEmployee == null);
        final boolean supervisionOfTargetEmployeeDoesNotExist = (supervisionOfTargetEmployee == null);

        if (targetEmployeeDoesNotExist || supervisionOfTargetEmployeeDoesNotExist) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        List< RequestModel > requests = new ArrayList<>();

        final List< RequestModel > acceptedRequests = requestRepository
                .findAllByUsernameAndStatus(employeeUsername, ServiceConstant.STATUS_ACCEPTED);
        final List< RequestModel > requestedRequests = requestRepository
                .findAllByUsernameAndStatus(employeeUsername, ServiceConstant.STATUS_REQUESTED);

        requests.addAll(acceptedRequests);
        requests.addAll(requestedRequests);

        final boolean acceptedOrRequestedRequestsExist = !requests.isEmpty();

        if (acceptedOrRequestedRequestsExist) {
            for (RequestModel request : requests) {
                request.setStatus(ServiceConstant.STATUS_CANCELLED);
                request.setUpdatedDate(new Date());
                request.setUpdatedBy(adminUsername);

                requestRepository.save(request);
            }
        }

        final boolean employeeWithEmployeeUsernameIsAdministrator = adminRepository
                .existsAdminModelByDeletedIsFalseAndUsernameEquals(employeeUsername);

        if (employeeWithEmployeeUsernameIsAdministrator) {
            AdminModel administrator = adminRepository.findByDeletedIsFalseAndUsernameEquals(employeeUsername);

            administrator.setDeleted(true);

            adminRepository.save(administrator);
        }

        targetEmployee.setDeleted(true);

        employeeRepository.save(targetEmployee);

        supervisionOfTargetEmployee.setDeleted(true);

        supervisionRepository.save(supervisionOfTargetEmployee);
    }

    @Override
    public void changeSupervisorOnPreviousSupervisorDeletion(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        final boolean noAdministratorUsernameGiven = adminUsername.isEmpty();
        final boolean noOldSupervisorUsernameGiven = oldSupervisorUsername.isEmpty();
        final boolean noNewSupervisorUsernameGiven = newSupervisorUsername.isEmpty();

        if (noAdministratorUsernameGiven || noOldSupervisorUsernameGiven || noNewSupervisorUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                                                         .equals(ServiceConstant.ROLE_ADMINISTRATOR);
        final boolean selfSupervisorChangeOnDeletionAttempt = oldSupervisorUsername.equals(adminUsername);

        if (!validAdministrator || selfSupervisorChangeOnDeletionAttempt) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        }

        final boolean employeeWithOldSupervisorUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsername(oldSupervisorUsername);
        final boolean employeeWithNewSupervisorUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsername(newSupervisorUsername);

        if (!employeeWithOldSupervisorUsernameExists || !employeeWithNewSupervisorUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        List< SupervisionModel > supervisions = supervisionRepository
                .findAllByDeletedIsFalseAndSupervisorUsername(oldSupervisorUsername);

        final boolean employeeWithOldSupervisorUsernameDoesNotSupervise = supervisions.isEmpty();

        if (employeeWithOldSupervisorUsernameDoesNotSupervise) {
            throw new DataNotFoundException(UNAUTHORIZED_OPERATION);
        } else {
            demotePreviousSupervisorFromAdminIfNecessary(adminUsername, oldSupervisorUsername, newSupervisorUsername,
                                                         supervisions
            );
        }
    }

    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    public void demotePreviousSupervisorFromAdminIfNecessary(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername,
            final List< SupervisionModel > supervisions
    ) {

        boolean hadSupervisingEmployees = false;

        for (SupervisionModel supervision : supervisions) {
            final boolean correctAssumptionOfNotHavingSupervisingEmployees = (hadSupervisingEmployees == false);
            final boolean supervisedEmployeeFromSupervisionSupervises = supervisionRepository
                    .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(supervision.getEmployeeUsername());

            if (correctAssumptionOfNotHavingSupervisingEmployees && supervisedEmployeeFromSupervisionSupervises) {
                hadSupervisingEmployees = true;

                AdminModel demotedAdmin = adminRepository.findByDeletedIsFalseAndUsernameEquals(oldSupervisorUsername);

                demotedAdmin.setDeleted(true);

                adminRepository.save(demotedAdmin);
            }
            supervision.setSupervisorUsername(newSupervisorUsername);
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(adminUsername);

            supervisionRepository.save(supervision);
        }
    }

}
