package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.responsemapper.EmployeesResponseMapper;
import com.oasis.service.implementation.EmployeesServiceImpl;
import com.oasis.webmodel.request.AddEmployeeRequest;
import com.oasis.webmodel.request.DeleteEmployeeRequest;
import com.oasis.webmodel.request.DeleteEmployeeSupervisorRequest;
import com.oasis.webmodel.request.UpdateEmployeeRequest;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class EmployeesController {

    @Autowired
    private EmployeesServiceImpl employeesServiceImpl;
    @Autowired
    private EmployeesResponseMapper employeesResponseMapper;

    @GetMapping(value = APIMappingValue.API_LIST_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callViewAllEmployeesService(@RequestParam int pageNumber,
                                                      @RequestParam String sortInfo) {
        List<EmployeeListResponse.Employee> employeesFound;

        try {
            employeesFound = employeesServiceImpl.getEmployeesList(pageNumber, sortInfo);
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), e.getErrorCode(), e.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceViewFoundEmployeesSuccessResult(HttpStatus.OK.value(), employeesFound, pageNumber), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DETAIL_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callViewEmployeeDetailService(@PathVariable String employeeNik) {

        EmployeeModel employee;

        try {
            employee = employeesServiceImpl.getEmployeeDetail(employeeNik);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        EmployeeModel supervisor;

        try {
            supervisor = employeesServiceImpl.getEmployeeSupervisorData(employeeNik);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeDetailSuccessResponse(HttpStatus.OK.value(), employee, supervisor), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_FIND_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callFindEmployeeService(@RequestParam String searchQuery,
                                                  @RequestParam int pageNumber,
                                                  @RequestParam String sortInfo) {
        List<EmployeeListResponse.Employee> employeesFound;

        try {
            employeesFound = employeesServiceImpl.getEmployeesListBySearchQuery(searchQuery, pageNumber, sortInfo);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceViewFoundEmployeesSuccessResult(HttpStatus.OK.value(), employeesFound, pageNumber), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callInsertToDatabaseService(@RequestBody AddEmployeeRequest request) {

        try {
            employeesServiceImpl.addEmployee(request.getEmployee(), request.getEmployeeNik());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (DuplicateDataException duplicateDataException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.CONFLICT.value(), duplicateDataException.getErrorCode(), duplicateDataException.getErrorMessage()), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping(value = APIMappingValue.API_SAVE_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callUpdateEmployeeService(@RequestBody UpdateEmployeeRequest request) {

        try {
            employeesServiceImpl.updateEmployee(request.getEmployee(), request.getAdminNik());
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @DeleteMapping(value = APIMappingValue.API_DELETE_EMPLOYEE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callDeleteEmployeeService(@RequestBody DeleteEmployeeRequest request) {

        try {
            employeesServiceImpl.deleteEmployee(request);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_CHANGE_SUPERVISOR_ON_DELETE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callChangeSupervisorOnPreviousSupervisorDeletion(@RequestBody DeleteEmployeeSupervisorRequest request) {

        try {
            employeesServiceImpl.changeSupervisorOnPreviousSupervisorDeletion(request);
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.UNAUTHORIZED.value(), unauthorizedOperationException.getErrorCode(), unauthorizedOperationException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.BAD_REQUEST.value(), badRequestException.getErrorCode(), badRequestException.getErrorMessage()), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceEmployeeSaveSuccessResult(HttpStatus.OK.value()), HttpStatus.OK);
    }
}
