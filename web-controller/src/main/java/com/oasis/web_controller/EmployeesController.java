package com.oasis.web_controller;

import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.response_mapper.EmployeesResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.implementation.EmployeesServiceImpl;
import com.oasis.web_model.request.employees.AddEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeRequest;
import com.oasis.web_model.request.employees.DeleteEmployeeSupervisorRequest;
import com.oasis.web_model.request.employees.UpdateEmployeeRequest;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

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

    @GetMapping(value = APIMappingValue.API_LIST,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callFindEmployeeService(
            @RequestParam(value = "query", required = false, defaultValue = "defaultQuery") final String query,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "sort") final String sort
    ) {

        List<EmployeeModel> employeesFound, supervisorsFound;
        int totalRecords;

        try {
            employeesFound = employeesServiceImpl.getEmployeesList(query, page, sort);
            supervisorsFound = employeesServiceImpl.getSupervisorsList(employeesFound);
            totalRecords = employeesServiceImpl.getEmployeesCount(query, sort);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceViewFoundEmployeesSuccessResult(HttpStatus.OK.value(), employeesFound, supervisorsFound, page, totalRecords), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DETAIL_EMPLOYEE,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callViewEmployeeDetailService(@PathVariable final String username) {

        EmployeeModel employee;
        EmployeeModel supervisor;

        try {
            employee = employeesServiceImpl.getEmployeeDetailData(username);
            supervisor = employeesServiceImpl.getEmployeeSupervisorData(username);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeDetailSuccessResponse(HttpStatus.OK.value(), employee, supervisor), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE_EMPLOYEE,
                 produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callInsertToDatabaseService(@RequestBody final AddEmployeeRequest request) {

        try {
            employeesServiceImpl.addEmployee(request.getEmployee(), request.getUsername());
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

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping(value = APIMappingValue.API_SAVE_EMPLOYEE,
                produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callUpdateEmployeeService(@RequestBody final UpdateEmployeeRequest request) {

        try {
            employeesServiceImpl.updateEmployee(request.getEmployee(), request.getUsername());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                             badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_EMPLOYEE,
                   produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callDeleteEmployeeService(@RequestBody final DeleteEmployeeRequest request) {

        try {
            employeesServiceImpl.deleteEmployee(request);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_CHANGE_SUPERVISOR_ON_DELETE,
                 produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callChangeSupervisorOnPreviousSupervisorDeletion(@RequestBody final DeleteEmployeeSupervisorRequest request) {

        try {
            employeesServiceImpl.changeSupervisorOnPreviousSupervisorDeletion(request);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
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
