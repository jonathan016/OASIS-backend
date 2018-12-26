package com.oasis.web_controller;

import com.oasis.exception.*;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.request_mapper.EmployeesRequestMapper;
import com.oasis.response_mapper.EmployeesResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.tool.helper.ActiveComponentManager;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.web_model.request.employees.ChangePasswordRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeSupervisorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping(value = APIMappingValue.API_EMPLOYEE)
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeesController {

    @Autowired
    private EmployeesServiceApi employeesServiceApi;
    @Autowired
    private FailedResponseMapper failedResponseMapper;
    @Autowired
    private ActiveComponentManager activeComponentManager;
    @Autowired
    private EmployeesRequestMapper employeesRequestMapper;
    @Autowired
    private EmployeesResponseMapper employeesResponseMapper;

    @SuppressWarnings("unchecked")
    @GetMapping(value = APIMappingValue.API_LIST, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeesList(
            @RequestParam(value = "query", required = false)
            final String query,
            @RequestParam(value = "page")
            final int page,
            @RequestParam(value = "sort", required = false)
            final String sort,
            @AuthenticationPrincipal
            final User user
    ) {

        final Map< String, List< ? > > employeesListData;
        final List< EmployeeModel > employees;
        final List< EmployeeModel > supervisors;
        final List< String > employeePhotos;
        final long totalRecords;

        try {
            employeesListData = employeesServiceApi.getEmployeesListData(user.getUsername(), query, page, sort);

            employees = (List< EmployeeModel >) employeesListData.get("employees");
            supervisors = (List< EmployeeModel >) employeesListData.get("supervisors");
            employeePhotos = (List< String >) employeesListData.get("employeePhotos");
            totalRecords = employeesServiceApi.getEmployeesCount(user.getUsername(), query);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper
                                            .produceViewFoundEmployeesSuccessResult(HttpStatus.OK.value(), employees,
                                                                                    supervisors, employeePhotos,
                                                                                    activeComponentManager
                                                                                            .getEmployeesListActiveComponents(
                                                                                                    user.getUsername()
                                                                                            ), page, totalRecords
                                            ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DATA_EMPLOYEE, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeeDetailData(
            @PathVariable
            final String username,
            @AuthenticationPrincipal
            final User user
    ) {

        final EmployeeModel employee;
        final EmployeeModel supervisor;

        try {
            employee = employeesServiceApi.getEmployeeDetailData(username);
            supervisor = employeesServiceApi.getEmployeeSupervisorData(username);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        String photo = employeesServiceApi.getEmployeeDetailPhoto(employee.getUsername(), employee.getPhoto());

        return new ResponseEntity<>(employeesResponseMapper
                .produceEmployeeDetailSuccessResponse(HttpStatus.OK.value(),
                        activeComponentManager.getEmployeeDetailActiveComponents(
                                new ArrayList<>(user.getAuthorities()).get(0).getAuthority()),
                        employee, photo, supervisor
        ), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_USERNAMES, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeesUsernamesForSupervisorSelection(
            @RequestParam(value = "username")
            final String username
    ) {

        final List< String > usernames;

        try {
            usernames = employeesServiceApi.getEmployeesUsernamesForSupervisorSelection(username);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(usernames, HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_PHOTO_EMPLOYEE, produces = { IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE },
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeePhoto(
            @PathVariable(value = "username")
            final String username,
            @PathVariable(value = "photo")
            final String photoGiven,
            @RequestParam(value = "extension")
            final String extension
    ) {

        final byte[] photo = employeesServiceApi.getEmployeePhoto(username, photoGiven, extension);

        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE, produces = APPLICATION_JSON_VALUE,
                 consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity saveEmployee(
            @RequestParam(value = "photo", required = false)
                    MultipartFile photoGiven,
            @RequestParam(value = "data")
            final String rawEmployeeData,
            @AuthenticationPrincipal
            final User user
    ) {

        final String adminUsername;
        final boolean addEmployeeOperation;
        final String username;

        try {
            adminUsername = user.getUsername();
            addEmployeeOperation = employeesRequestMapper.isCreateEmployeeOperation(rawEmployeeData);

            username = employeesServiceApi.saveEmployee(photoGiven, adminUsername, employeesRequestMapper
                                                                .getEmployeeModelFromRawData(rawEmployeeData,
                                                                                             addEmployeeOperation),
                                                        employeesRequestMapper
                                                                .getSupervisorUsernameFromRawData(rawEmployeeData),
                                                        addEmployeeOperation
            );
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorCode(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.CONFLICT.value(),
                                                                                 duplicateDataException.getErrorCode(),
                                                                                 duplicateDataException
                                                                                         .getErrorMessage()
            ), HttpStatus.CONFLICT);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        if (addEmployeeOperation) {
            return new ResponseEntity<>(
                    employeesResponseMapper.produceEmployeeSaveAddSuccessResult(HttpStatus.CREATED.value(), username),
                    HttpStatus.CREATED
            );
        } else {
            return new ResponseEntity<>(
                    employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()),
                    HttpStatus.OK
            );
        }
    }

    @PostMapping(value = APIMappingValue.API_PASSWORD_CHANGE, produces = APPLICATION_JSON_VALUE,
                 consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity changePassword(
            @RequestBody
            final ChangePasswordRequest request,
            @AuthenticationPrincipal
            final User user
    ) {

        try {
            employeesServiceApi
                    .changePassword(user.getUsername(), request.getOldPassword(), request.getNewPassword(),
                                    request.getNewPasswordConfirmation()
                    );
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (UserNotAuthenticatedException userNotAuthenticatedException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 userNotAuthenticatedException
                                                                                         .getErrorCode(),
                                                                                 userNotAuthenticatedException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);

    }

    @DeleteMapping(value = APIMappingValue.API_DELETE, produces = APPLICATION_JSON_VALUE,
                   consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity deleteEmployee(
            @RequestParam(value = "target")
            final String employeeUsername,
            @AuthenticationPrincipal
            final User user
    ) {

        try {
            employeesServiceApi.deleteEmployee(user.getUsername(), employeeUsername);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorCode(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_CHANGE_SUPERVISOR_ON_DELETE, produces = APPLICATION_JSON_VALUE,
                 consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity changeSupervisorOnPreviousSupervisorDeletion(
            @RequestBody
            final DeleteEmployeeSupervisorRequest request,
            @AuthenticationPrincipal
            final User user
    ) {

        try {
            employeesServiceApi.changeSupervisorOnPreviousSupervisorDeletion(user.getUsername(),
                                                                             request.getOldSupervisorUsername(),
                                                                             request.getNewSupervisorUsername()
            );
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorCode(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT, method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.HEAD,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,
            RequestMethod.TRACE
    })
    public ResponseEntity returnIncorrectMappingCalls(
            final MissingServletRequestParameterException exception
    ) {

        final String message;

        if (exception.getParameterName() != null) {
            message = exception.getMessage();
        } else {
            message = "Incorrect mapping/method!";
        }

        return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                             HttpStatus.BAD_REQUEST.name(), message
        ), HttpStatus.BAD_REQUEST);
    }

}
