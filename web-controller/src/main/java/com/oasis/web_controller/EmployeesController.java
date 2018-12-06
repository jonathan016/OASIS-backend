package com.oasis.web_controller;

import com.oasis.request_mapper.EmployeesRequestMapper;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.response_mapper.EmployeesResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.implementation.EmployeesServiceImpl;
import com.oasis.web_model.request.employees.DeleteEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeSupervisorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping(value = APIMappingValue.API_EMPLOYEE)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeesController {

    @Autowired
    private EmployeesServiceImpl employeesServiceImpl;
    @Autowired
    private EmployeesResponseMapper employeesResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;
    @Autowired
    private EmployeesRequestMapper employeesRequestMapper;

    @GetMapping(value = APIMappingValue.API_LIST,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeesList(
            @RequestParam(value = "username") final String username,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "sort") final String sort
    ) {

        List<EmployeeModel> employeesFound, supervisorsFound;
        int totalRecords;
        List<String> employeePhotos;

        try {
            if (query != null && query.isEmpty()) query = "defaultQuery";
            employeesFound = employeesServiceImpl.getEmployeesList(username, query, page, sort);
            supervisorsFound = employeesServiceImpl.getSupervisorsList(employeesFound);
            totalRecords = employeesServiceImpl.getEmployeesCount(username, query, sort);
            employeePhotos = employeesServiceImpl.getEmployeePhotos(employeesFound);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceViewFoundEmployeesSuccessResult(HttpStatus.OK.value(), employeesFound, supervisorsFound, employeePhotos, page, totalRecords), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DETAIL_EMPLOYEE,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeeDetailData(
            @PathVariable final String username
    ) {

        EmployeeModel employee;
        EmployeeModel supervisor;

        try {
            employee = employeesServiceImpl.getEmployeeDetailData(username);
            supervisor = employeesServiceImpl.getEmployeeSupervisorData(username);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        String photo = employeesServiceImpl.getEmployeeDetailPhoto(employee.getUsername(), employee.getPhoto());

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeDetailSuccessResponse(HttpStatus.OK.value(), employee, photo, supervisor), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_USERNAMES,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeesUsername() {
        return new ResponseEntity<>(employeesServiceImpl.getEmployeesUsername(), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_EMPLOYEE_PHOTO,
                produces = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE},
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getEmployeePhoto(
            @PathVariable(value = "username") final String username,
            @PathVariable(value = "image") final String image,
            @RequestParam(value = "extension") final String extension
    ) {

        byte[] photo;

        photo = employeesServiceImpl.getEmployeePhoto(
                username,
                image,
                extension
        );

        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE_EMPLOYEE,
                 produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity saveEmployee(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "data") final String rawEmployeeData
    ) {

        String adminUsername;
        boolean isAddOperation;
        String username;

        try {
            adminUsername = employeesRequestMapper.getAdminUsernameFromRawData(rawEmployeeData);
            isAddOperation = employeesRequestMapper.checkAddOperationFromRawData(rawEmployeeData);

            username = employeesServiceImpl.saveEmployee(
                    photo,
                    adminUsername,
                    employeesRequestMapper.getEmployeeModelFromRawData(rawEmployeeData, isAddOperation),
                    employeesRequestMapper.getSupervisorUsernameFromRawData(rawEmployeeData),
                    isAddOperation
            );
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.CONFLICT.value(), duplicateDataException.getErrorCode(), duplicateDataException.getErrorMessage()), HttpStatus.CONFLICT);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                             badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        if (isAddOperation) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveAddSuccessResult(HttpStatus.CREATED.value(), username), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.CREATED.value()), HttpStatus.CREATED);
        }
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_EMPLOYEE,
                   produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity deleteEmployee(
            @RequestBody final DeleteEmployeeRequest request
    ) {

        try {
            employeesServiceImpl.deleteEmployee(request.getAdminUsername(), request.getEmployeeUsername());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_CHANGE_SUPERVISOR_ON_DELETE,
                 produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity changeSupervisorOnPreviousSupervisorDeletion(
            @RequestBody final DeleteEmployeeSupervisorRequest request
    ) {

        try {
            employeesServiceImpl.changeSupervisorOnPreviousSupervisorDeletion(
                    request.getAdminUsername(),
                    request.getOldSupervisorUsername(),
                    request.getNewSupervisorUsername()
            );
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveUpdateSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @RequestMapping(value = APIMappingValue.API_MISDIRECT,
                    method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
                              RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.TRACE})
    public ResponseEntity returnIncorrectMappingCalls() {

        return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(), "Incorrect mapping/method"), HttpStatus.BAD_REQUEST);
    }

}
