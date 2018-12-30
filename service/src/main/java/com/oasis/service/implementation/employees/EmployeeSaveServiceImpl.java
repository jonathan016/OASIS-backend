package com.oasis.service.implementation.employees;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeSaveServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.tool.constant.ImageDirectoryConstant;
import com.oasis.tool.constant.PrefixConstant;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.util.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeeSaveServiceImpl
        implements EmployeeSaveServiceApi {

    private Logger logger = LoggerFactory.getLogger(EmployeeSaveServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;

    @Autowired
    private ImageHelper imageHelper;
    @Autowired
    private BCryptPasswordEncoder encoder;


    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    @Caching(evict = {
            @CacheEvict(value = "employeeDetailData",
                        key = "#employee.username"),
            @CacheEvict(value = "employeesListData",
                        allEntries = true)
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

        if (supervisorUsername == null || supervisorUsername.isEmpty()) {
            supervisorUsername = username;
        }

        if (!addEmployeeOperation) {
            supervisorUsername = supervisorUsername.replace(" (Default)", "");
        }

        if (!isSaveEmployeeParametersProper(photoGiven, employee, supervisorUsername, addEmployeeOperation)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
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
                                    savedEmployee.getJobTitle(), savedEmployee.getDivision(),
                                    savedEmployee.getLocation()
                            );

                    if (potentialDuplicateEmployeeData == true) {
                        throw new DuplicateDataException(DUPLICATE_DATA_FOUND);
                    } else {
                        final String dobString = new SimpleDateFormat("dd-MM-yyyy").format(savedEmployee.getDob());

                        savedEmployee.setUsername(generateUsername(savedEmployee.getName().toLowerCase()));
                        savedEmployee.setPassword(generateDefaultPassword(dobString));
                        savedEmployee.setDeleted(false);
                        savedEmployee.setCreatedBy(username);
                        savedEmployee.setCreatedDate(new Date());
                    }
                }
            } else {
                savedEmployee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(employee.getUsername());

                if (savedEmployee == null) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    savedEmployee.setName(employee.getName());
                    savedEmployee.setDob(employee.getDob());
                    savedEmployee.setPhone(employee.getPhone());
                    savedEmployee.setJobTitle(employee.getJobTitle());
                    savedEmployee.setDivision(employee.getDivision());
                    savedEmployee.setLocation(employee.getLocation());
                }
            }

            if (!addEmployeeOperation && hasCyclicSupervising(savedEmployee.getUsername(), supervisorUsername)) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                employeeUtilServiceApi.updateSupervisorDataOnEmployeeDataModification(
                        username, savedEmployee.getUsername(), supervisorUsername, addEmployeeOperation);

                validateAndSavePhoto(photoGiven, addEmployeeOperation, savedEmployee);

                savedEmployee.setSupervisionId(
                        getSupervisionId(employee.getUsername(), supervisorUsername, username));
                savedEmployee.setUpdatedDate(new Date());
                savedEmployee.setUpdatedBy(username);

                employeeRepository.save(savedEmployee);

                return savedEmployee.getUsername();
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

        final boolean emptyUsernameGiven = ( username == null || username.isEmpty() );
        final boolean emptyOldPasswordGiven = ( oldPassword == null || oldPassword.isEmpty() );
        final boolean emptyNewPasswordGiven = ( newPassword == null || newPassword.isEmpty() );
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
                    throw new UserNotAuthenticatedException(UNAUTHENTICATED_USER);
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

    @Override
    @SuppressWarnings("UnnecessaryContinue")
    public List< String > getEmployeesUsernamesForSupervisorSelection(
            final String adminUsername, final String username, final String division
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyUsernameGiven = ( username != null && username.isEmpty() );

        if (emptyUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            Set< String > possibleSupervisorsUsernames = new LinkedHashSet<>();
            final List< EmployeeModel > employees = employeeRepository
                    .findAllByDeletedIsFalseAndUsernameIsNotNullAndDivisionEqualsOrDivisionEqualsOrderByUsernameAsc(
                            division, "");

            final boolean addEmployeeOperation = ( username == null );

            if (addEmployeeOperation) {
                for (final EmployeeModel possibleSupervisor : employees) {
                    if (!possibleSupervisor.getUsername().equals(adminUsername)) {
                        possibleSupervisorsUsernames.add(possibleSupervisor.getUsername());
                    } else {
                        possibleSupervisorsUsernames.add(adminUsername.concat(" (Default)"));
                    }
                }
            } else {
                if (!employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username)) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
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
                                if (!possibleSupervisor.getUsername().equals(adminUsername)) {
                                    possibleSupervisorsUsernames.add(currentCandidateUsername);
                                } else {
                                    possibleSupervisorsUsernames.add(adminUsername.concat(" (Default)"));
                                }
                            }
                        }
                    }
                }
            }

            return new ArrayList<>(possibleSupervisorsUsernames);
        }
    }

    @SuppressWarnings({ "ConstantConditions", "RedundantIfStatement" })
    private boolean isSaveEmployeeParametersProper(
            final MultipartFile photoGiven,
            final EmployeeModel employee,
            final String supervisorUsername,
            final boolean addEmployeeOperation
    ) {

        if (photoGiven != null) {
            try {
                if (!photoGiven.getOriginalFilename().matches(Regex.REGEX_JPEG_FILE_NAME) &&
                    !photoGiven.getOriginalFilename().matches(Regex.REGEX_PNG_FILE_NAME)) {
                    return false;
                }
            } catch (NullPointerException exception) {
                return false;
            }
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
            } else if (!employee.getName().matches(Regex.REGEX_EMPLOYEE_NAME)) {
                return false;
            } else if (!employee.getPhone().matches(Regex.REGEX_EMPLOYEE_PHONE)) {
                return false;
            } else if (!employee.getJobTitle().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (!employee.getDivision().matches(Regex.REGEX_DIVISIONS)) {
                return false;
            } else if (!employee.getLocation().matches(Regex.REGEX_UNIVERSAL_STRINGS)) {
                return false;
            } else if (employee.getDob() != null) {
                Calendar calendar = Calendar.getInstance();

                calendar.setTime(new Date());
                final int currYear = calendar.get(Calendar.YEAR);

                calendar.setTime(employee.getDob());
                final int dobYear = calendar.get(Calendar.YEAR);

                if (currYear - dobYear < 16 || currYear - dobYear > 65) {
                    return false;
                }
            } else if (employee.getUsername().equals(supervisorUsername)) {
                return false;
            } else if (!addEmployeeOperation) {
                final EmployeeModel recordedEmployee = employeeRepository
                        .findByDeletedIsFalseAndUsernameEquals(employee.getUsername());
                final String recordedSupervisorUsername = supervisionRepository
                        .findByDeletedIsFalseAndEmployeeUsernameEquals(employee.getUsername()).getSupervisorUsername();

                if (recordedEmployee.equals(employee) && recordedSupervisorUsername.equals(supervisorUsername)) {
                    return false;
                }
            }

            return true;
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
            final SupervisionModel createdSupervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);

            return createdSupervision.get_id();
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private boolean hasCyclicSupervising(
            final String employeeUsername, String supervisorUsername
    ) {

        final String supervisorOfSupervisorUsername;

        try {
            supervisorOfSupervisorUsername = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(supervisorUsername).getSupervisorUsername();
        } catch (NullPointerException exception) {
            // Entering this block means for the specified supervisorUsername, there is no supervison, inferring
            // that the specified supervisorUsername is the username of top of the top administrator. This is why
            // upon catching NullPointerException due to call of .getSupervisorUsername() on null object, we return
            // false, as there can be no cyclic supervising for top of the top administrator.
            return false;
        }

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
