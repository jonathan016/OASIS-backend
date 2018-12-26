package com.oasis.service.implementation;

import com.oasis.exception.*;
import com.oasis.model.entity.AdminModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.service.api.RequestsServiceApi;
import com.oasis.tool.constant.*;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.helper.RoleDeterminer;
import com.oasis.tool.util.Regex;
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
    private BCryptPasswordEncoder encoder;
    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RequestsServiceApi requestsServiceApi;
    @Autowired
    private SupervisionRepository supervisionRepository;

    /*-------------Employees List Methods-------------*/
    @Override
    @Cacheable(value = "employeesListData", unless = "#result.size() == 0")
    public Map< String, List< ? > > getEmployeesListData(
            final String username, final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > employeesListData = new HashMap<>();

        final List< EmployeeModel > employees = getEmployeesList(username, query, page, sort);
        final List< EmployeeModel > supervisors = getSupervisorsList(employees);
        final List< String > employeePhotos = getEmployeesPhotos(employees);

        employeesListData.put("employees", employees);
        employeesListData.put("supervisors", supervisors);
        employeesListData.put("employeePhotos", employeePhotos);

        return employeesListData;
    }

    @Override
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
        } else {
            final boolean useParameterSort = (sort != null);

            if (useParameterSort) {
                final boolean properSortFormatGiven = sort.matches(Regex.REGEX_EMPLOYEE_SORT);

                if (!properSortFormatGiven) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                }
            } else {
                sort = "A";
            }

            final Set< EmployeeModel > employees;
            final long employeesCount = getEmployeesCount(username, query);
            final long availablePages = (long) Math.ceil((double) employeesCount / PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

            final boolean noEmployees = (employeesCount == 0);
            final boolean pageIndexOutOfBounds = ((page < 1) || (page > availablePages));

            if (noEmployees || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final boolean viewAllEmployees = (query == null);

                if (viewAllEmployees) {
                    employees = new LinkedHashSet<>(getSortedEmployees(username, page, sort));
                } else {
                    employees = new LinkedHashSet<>(getSortedEmployeesFromQuery(page, query, sort));
                }

                return new ArrayList<>(employees);
            }
        }
    }

    @Override
    public long getEmployeesCount(
            final String username, final String query
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
    @Cacheable(value = "employeeDetailData", key = "#username")
    public EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException {

        final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);

        final boolean employeeWithUsernameExists = (employee != null);

        if (!employeeWithUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            return employee;
        }
    }

    @Override
    public String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    ) {

        final boolean validPhotoLocation = (photoLocation != null && !photoLocation.isEmpty());

        if (validPhotoLocation) {
            final File photo = new File(photoLocation);

            if (photo.exists() && Files.exists(photo.toPath())) {
                return "http://localhost:8085/oasis/api/employees/" + username + "/" +
                       username.concat("?extension=").concat(imageHelper.getExtensionFromFileName(photo.getName()));
            }
        }

        return "http://localhost:8085/oasis/api/employees/" + username + "/image_not_found".concat("?extension=jpeg");
    }

    @Override
    @SuppressWarnings("UnnecessaryContinue")
    public List< String > getEmployeesUsernamesForSupervisorSelection(
            final String username
    )
            throws
            BadRequestException {

        final boolean emptyUsernameGiven = username.isEmpty();

        if (emptyUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            Set< String > possibleSupervisorsUsernames = new LinkedHashSet<>();
            final List< EmployeeModel > employees = employeeRepository
                    .findAllByDeletedIsFalseAndUsernameIsNotNullOrderByUsernameAsc();

            final boolean addEmployeeOperation = username.equals("-1");

            if (addEmployeeOperation) {
                for (final EmployeeModel possibleSupervisor : employees) {
                    possibleSupervisorsUsernames.add(possibleSupervisor.getUsername());
                }
            } else {
                List< String > supervisedEmployeesUsernames = new ArrayList<>();
                final List< SupervisionModel > supervisions = supervisionRepository
                        .findAllByDeletedIsFalseAndSupervisorUsernameEquals(username);

                for (final SupervisionModel supervision : supervisions) {
                    supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
                }

                for (final EmployeeModel possibleSupervisor : employees) {
                    final String currentCandidateUsername = possibleSupervisor.getUsername();
                    final boolean selfUsername = username.equals(currentCandidateUsername);

                    if (selfUsername) {
                        continue;
                    } else {
                        final boolean safeFromCyclicSupervising = isSafeFromCyclicSupervising(
                                currentCandidateUsername, supervisedEmployeesUsernames);

                        if (safeFromCyclicSupervising) {
                            possibleSupervisorsUsernames.add(currentCandidateUsername);
                        }
                    }
                }
            }

            return new ArrayList<>(possibleSupervisorsUsernames);
        }
    }

    @Override
    public byte[] getEmployeePhoto(
            final String username, final String photoName, final String extension
    ) {

        final boolean employeeWithUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username);

        if (!employeeWithUsernameExists) {
            logger.info("Failed to load employee photo as username does not refer any employee in database");
            return new byte[0];
        } else {
            final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);

            File file = new File(employee.getPhoto());

            final boolean photoNameIsImageNotFound = photoName.equals("image_not_found");
            final boolean correctExtensionForPhoto = file.getName().endsWith(extension);

            if (!correctExtensionForPhoto || photoNameIsImageNotFound) {
                file = new File(ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator)
                        .concat("image_not_found.jpeg"));
            }

            final byte[] photo;

            try {
                photo = Files.readAllBytes(file.toPath());
            } catch (IOException | NullPointerException exception) {
                logger.error("Failed to read photo as IOException or NullPointerException occurred with message " +
                        exception.getMessage());
                return new byte[0];
            }

            return photo;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException {

        final SupervisionModel supervision = supervisionRepository.findByDeletedIsFalseAndEmployeeUsernameEquals(username);
        final boolean employeeIsAdministrator = roleDeterminer.determineRole(username)
                                                              .equals(RoleConstant.ROLE_ADMINISTRATOR);

        final boolean noSupervisionForEmployeeWithUsername = (supervision == null);

        if (noSupervisionForEmployeeWithUsername && !employeeIsAdministrator) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final boolean employeeIsTopAdministrator = noSupervisionForEmployeeWithUsername && employeeIsAdministrator;

            if (employeeIsTopAdministrator) {
                return null;
            }

            return employeeRepository.findByDeletedIsFalseAndUsernameEquals(supervision.getSupervisorUsername());
        }
    }

    /*-------------Save Employee Methods-------------*/
    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    @Caching(evict = {
            @CacheEvict(value = "employeeDetailData", key = "#employee.username"),
            @CacheEvict(value = "employeesListData", allEntries = true)
    })
    public String saveEmployee(
            final MultipartFile photoGiven, final String username, final EmployeeModel employee,
            String supervisorUsername, final boolean addEmployeeOperation
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException {

        if (!isSaveEmployeeParametersProper(photoGiven, employee, addEmployeeOperation)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (supervisorUsername == null) {
                supervisorUsername = username;
            }

            EmployeeModel savedEmployee;

            if (addEmployeeOperation) {
                final boolean properNameFormatGiven = employee.getName().matches(Regex.REGEX_EMPLOYEE_NAME);

                if (!properNameFormatGiven) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    savedEmployee = employee;

                    final boolean potentialDuplicateEmployeeData = employeeRepository
                            .existsByDeletedIsFalseAndNameEqualsAndDobEqualsAndPhoneEqualsAndJobTitleEqualsAndDivisionEqualsAndLocationEquals(
                                    savedEmployee.getName(), savedEmployee.getDob(), savedEmployee.getPhone(),
                                    savedEmployee.getJobTitle(), savedEmployee.getDivision(), savedEmployee.getLocation()
                            );

                    if (potentialDuplicateEmployeeData == true) {
                        throw new DuplicateDataException(DUPLICATE_DATA_FOUND);
                    } else {
                        final String dobString = new SimpleDateFormat("dd-MM-yyyy").format(savedEmployee.getDob());

                        savedEmployee.setUsername(generateUsername(savedEmployee.getName().toLowerCase()));
                        savedEmployee.setPassword(generateDefaultPassword(dobString));
                        savedEmployee.setSupervisionId(getSupervisionId(employee.getUsername(), supervisorUsername, username));
                        savedEmployee.setDeleted(false);
                        savedEmployee.setCreatedBy(username);
                        savedEmployee.setCreatedDate(new Date());
                    }
                }
            } else {
                savedEmployee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(employee.getUsername());

                if (savedEmployee == null) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else if (savedEmployee.equals(employee)) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    savedEmployee.setName(employee.getName());
                    savedEmployee.setDob(employee.getDob());
                    savedEmployee.setPhone(employee.getPhone());
                    savedEmployee.setJobTitle(employee.getJobTitle());
                    savedEmployee.setDivision(employee.getDivision());
                    savedEmployee.setLocation(employee.getLocation());

                    updateSupervisorDataOnEmployeeUpdate(username, savedEmployee, employee.getUsername(), supervisorUsername);
                }
            }

            validateAndSavePhoto(photoGiven, addEmployeeOperation, savedEmployee);

            savedEmployee.setUpdatedDate(new Date());
            savedEmployee.setUpdatedBy(username);

            employeeRepository.save(savedEmployee);

            return savedEmployee.getUsername();
        }
    }

    @SuppressWarnings({"ConstantConditions", "RedundantIfStatement"})
    private boolean isSaveEmployeeParametersProper(
            final MultipartFile photoGiven,
            final EmployeeModel employee,
            final boolean addEmployeeOperation
    ) {

        try {
            if (!photoGiven.getOriginalFilename().matches(Regex.REGEX_JPEG_FILE_NAME) &&
                    !photoGiven.getOriginalFilename().matches(Regex.REGEX_PNG_FILE_NAME)) {
                return false;
            }
        } catch (NullPointerException exception) {
            return false;
        }

        if (employee == null) {
            return false;
        } else {
            if (!addEmployeeOperation && employee.getUsername() == null) {
                return false;
            } else if (employee.getName() == null) {
                return false;
            } else if (employee.getDob() == null) {
                return false;
            } else if (employee.getPhone() == null) {
                return false;
            } else if (employee.getJobTitle() == null) {
                return false;
            } else if (employee.getDivision() == null) {
                return false;
            } else if (employee.getLocation() == null) {
                return false;
            } else if (!addEmployeeOperation && employee.getSupervisionId() == null) {
                return false;
            } else if (!employee.getName().matches(Regex.REGEX_EMPLOYEE_NAME)) {
                return false;
            } else if (!employee.getPhone().matches(Regex.REGEX_EMPLOYEE_PHONE)) {
                return false;
            } else if (!employee.getJobTitle().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (!employee.getDivision().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (!employee.getLocation().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private String generateUsername(
            final String name
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

    private String generateDefaultPassword(
            final String dobString
    ) {

        StringBuilder password = new StringBuilder(PrefixConstant.PREFIX_DEFAULT_PASSWORD);

        final String dobWithoutDash = dobString.replace("-", "");

        password.append(dobWithoutDash);

        final String encodedPassword = encoder.encode(String.valueOf(password));

        password.replace(0, password.length(), encodedPassword);

        return String.valueOf(password);
    }

    private String getSupervisionId(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    )
            throws
            DataNotFoundException {

        final boolean employeeWithSupervisorUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(supervisorUsername);

        if (!employeeWithSupervisorUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            createSupervision(employeeUsername, supervisorUsername, adminUsername);

            final SupervisionModel createdSupervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);

            return createdSupervision.get_id();
        }
    }

    private void updateSupervisorDataOnEmployeeUpdate(
            final String username, final EmployeeModel savedEmployee, final String employeeUsername,
            final String supervisorUsername
    )
            throws
            DataNotFoundException,
            UnauthorizedOperationException,
            BadRequestException {

        final EmployeeModel supervisor = employeeRepository.findByDeletedIsFalseAndUsernameEquals(supervisorUsername);

        if (supervisor == null) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(savedEmployee.getUsername());

            if (supervisorUsername.equals(supervision.getSupervisorUsername())) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            } else {
                if (hasCyclicSupervising(employeeUsername, supervisorUsername)) {
                    throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                } else {
                    supervision.setSupervisorUsername(supervisor.getUsername());
                    supervision.setUpdatedDate(new Date());
                    supervision.setUpdatedBy(username);

                    supervisionRepository.save(supervision);

                    if (supervisionRepository
                            .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(employeeUsername)) {
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
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void validateAndSavePhoto(
            final MultipartFile photoGiven, final boolean addEmployeeOperation, EmployeeModel savedEmployee
    ) {

        final boolean rootDirectoryCreated;

        if (!Files.exists(Paths.get(ImageDirectoryConstant.EMPLOYEE_PHOTO_DIRECTORY))) {
            rootDirectoryCreated = new File(ImageDirectoryConstant.EMPLOYEE_PHOTO_DIRECTORY).mkdir();
        } else {
            rootDirectoryCreated = true;
        }

        if (rootDirectoryCreated) {
            if (!addEmployeeOperation && Files.exists(Paths.get(savedEmployee.getPhoto()))) {
                File photo = new File(savedEmployee.getPhoto());
                photo.delete();
            }

            if (photoGiven == null) {
                savedEmployee.setPhoto("");
            } else {
                final String photoLocation = ImageDirectoryConstant.EMPLOYEE_PHOTO_DIRECTORY.concat(File.separator)
                                                                                            .concat(savedEmployee
                                                                                                            .getUsername())
                                                                                            .concat(".")
                                                                                            .concat(imageHelper
                                                                                                            .getExtensionFromFileName(
                                                                                                                    photoGiven
                                                                                                                            .getOriginalFilename()));

                savedEmployee.setPhoto(photoLocation);

                savePhoto(photoGiven, savedEmployee.getUsername());
            }
        }
    }

    private void createSupervision(
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private boolean hasCyclicSupervising(
            final String employeeUsername, String supervisorUsername
    ) {

        final String supervisorOfSupervisorUsername = supervisionRepository
                .findByDeletedIsFalseAndEmployeeUsernameEquals(supervisorUsername).getSupervisorUsername();

        final boolean isEmployeeSupervisorOfSupervisor = supervisorOfSupervisorUsername.equals(employeeUsername);

        return isEmployeeSupervisorOfSupervisor;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void savePhoto(
            final MultipartFile photoGiven, final String username
    ) {

        if (photoGiven != null) {
            try {
                File photo = new File(
                        ImageDirectoryConstant.EMPLOYEE_PHOTO_DIRECTORY.concat(File.separator).concat(username)
                                                                       .concat(".").concat(imageHelper
                                                                                                   .getExtensionFromFileName(
                                                                                                           photoGiven
                                                                                                                   .getOriginalFilename())));

                photoGiven.transferTo(photo);
            } catch (IOException ioException) {
                logger.error("Failed to save photo as IOException occurred with message " + ioException.getMessage());
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
        } else {
            EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);

            if (employee == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final boolean passwordsMatch = encoder.matches(oldPassword, employee.getPassword());

                if (!passwordsMatch) {
                    throw new UserNotAuthenticatedException(INVALID_PASSWORD);
                } else {
                    final boolean unchangedPassword = oldPassword.equals(newPassword);
                    final boolean newPasswordConfirmed = newPassword.equals(newPasswordConfirmation);
                    final boolean sufficientNewPasswordLength = newPassword.length() > 6;

                    if (unchangedPassword || !newPasswordConfirmed || !sufficientNewPasswordLength) {
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
        } else {
            final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                    .equals(RoleConstant.ROLE_ADMINISTRATOR);
            final boolean selfDeletionAttempt = employeeUsername.equals(adminUsername);
            final boolean employeeWithEmployeeUsernameStillSupervises = supervisionRepository
                    .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(employeeUsername);
            final boolean allDeliveredAssetsHaveBeenReturned = requestsServiceApi
                    .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_DELIVERED).isEmpty();

            if (!validAdministrator || selfDeletionAttempt || employeeWithEmployeeUsernameStillSupervises ||
                    !allDeliveredAssetsHaveBeenReturned) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                EmployeeModel targetEmployee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(employeeUsername);
                SupervisionModel supervisionOfTargetEmployee = supervisionRepository
                        .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);

                final boolean targetEmployeeDoesNotExist = (targetEmployee == null);
                final boolean supervisionOfTargetEmployeeDoesNotExist = (supervisionOfTargetEmployee == null);

                if (targetEmployeeDoesNotExist || supervisionOfTargetEmployeeDoesNotExist) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    List< RequestModel > requests = new ArrayList<>();

                    final List< RequestModel > acceptedRequests = requestsServiceApi
                            .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_ACCEPTED);
                    final List< RequestModel > requestedRequests = requestsServiceApi
                            .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_REQUESTED);

                    requests.addAll(acceptedRequests);
                    requests.addAll(requestedRequests);

                    final boolean acceptedOrRequestedRequestsExist = !requests.isEmpty();

                    if (acceptedOrRequestedRequestsExist) {
                        for (RequestModel request : requests) {
                            request.setStatus(StatusConstant.STATUS_CANCELLED);
                            request.setUpdatedDate(new Date());
                            request.setUpdatedBy(adminUsername);

                            requestsServiceApi.save(request);
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
            }
        }
    }

    @Override
    @CacheEvict(value = { "employeesList", "employeeDetailData" }, allEntries = true)
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
        } else {
            final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                    .equals(RoleConstant.ROLE_ADMINISTRATOR);
            final boolean selfSupervisorChangeOnDeletionAttempt = oldSupervisorUsername.equals(adminUsername);

            if (!validAdministrator || selfSupervisorChangeOnDeletionAttempt) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                final boolean employeeWithOldSupervisorUsernameExists = employeeRepository
                        .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(oldSupervisorUsername);
                final boolean employeeWithNewSupervisorUsernameExists = employeeRepository
                        .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(newSupervisorUsername);

                if (!employeeWithOldSupervisorUsernameExists || !employeeWithNewSupervisorUsernameExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    List< SupervisionModel > supervisions = supervisionRepository
                            .findAllByDeletedIsFalseAndSupervisorUsernameEquals(oldSupervisorUsername);

                    final boolean employeeWithOldSupervisorUsernameDoesNotSupervise = supervisions.isEmpty();

                    if (employeeWithOldSupervisorUsernameDoesNotSupervise) {
                        throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                    } else {
                        demotePreviousSupervisorFromAdminIfNecessary(adminUsername, oldSupervisorUsername,
                                newSupervisorUsername, supervisions
                        );
                    }
                }
            }
        }
    }

    @SuppressWarnings("PointlessBooleanExpression")
    private void demotePreviousSupervisorFromAdminIfNecessary(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername,
            final List< SupervisionModel > supervisions
    ) {

        boolean hadSupervisingEmployees = false;

        for (SupervisionModel supervision : supervisions) {
            final boolean correctAssumptionOfNotHavingSupervisingEmployees = (hadSupervisingEmployees == false);
            final boolean supervisedEmployeeFromSupervisionSupervises = supervisionRepository
                    .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(supervision.getEmployeeUsername());

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

    @Override
    public EmployeeModel findByDeletedIsFalseAndUsername(final String username) {

        return employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsAdminModelByDeletedIsFalseAndUsernameEquals(final String username) {

        return adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsEmployeeModelByDeletedIsFalseAndUsername(final String username) {

        return employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(final String username) {

        return employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(username);
    }

    @Override
    public boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
            final String supervisorUsername, final String employeeUsername
    ) {

        return supervisionRepository
                .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameEqualsAndEmployeeUsernameEquals(supervisorUsername,
                                                                                                employeeUsername
                );
    }

    @Override
    public List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername) {

        return supervisionRepository.findAllByDeletedIsFalseAndSupervisorUsernameEquals(supervisorUsername);
    }

    @Override
    public boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername) {

        return supervisionRepository.existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(supervisorUsername);
    }

    private List< EmployeeModel > getSupervisorsList(
            final List< EmployeeModel > employees
    ) {

        List< EmployeeModel > supervisors = new ArrayList<>();

        for (final EmployeeModel employee : employees) {

            final SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(employee.getUsername());

            final boolean supervisionForCurrentEmployeeExists = (supervision != null);

            if (supervisionForCurrentEmployeeExists) {
                final EmployeeModel supervisor = employeeRepository
                        .findByDeletedIsFalseAndUsernameEquals(supervision.getSupervisorUsername());

                supervisors.add(supervisor);
            } else {    // For top administrator, who does not have any supervisor at all
                supervisors.add(null);
            }
        }

        return supervisors;
    }

    private List< String > getEmployeesPhotos(
            final List< EmployeeModel > employees
    ) {

        List< String > photos = new ArrayList<>();

        for (final EmployeeModel employee : employees) {
            photos.add(getEmployeeDetailPhoto(employee.getUsername(), employee.getPhoto()));
        }

        return photos;
    }

    private Set< EmployeeModel > getSortedEmployees(
            final String username, final int page, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

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

    private Set< EmployeeModel > getSortedEmployeesFromQuery(
            final int page, final String query, final String sort
    ) {

        Set< EmployeeModel > sortedEmployees = new LinkedHashSet<>();

        final int zeroBasedIndexPage = page - 1;
        final Pageable pageable = PageRequest.of(zeroBasedIndexPage, PageSizeConstant.EMPLOYEES_LIST_PAGE_SIZE);

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

    private boolean isSafeFromCyclicSupervising(
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

}
