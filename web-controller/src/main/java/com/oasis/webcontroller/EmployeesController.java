package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.responsemapper.EmployeesResponseMapper;
import com.oasis.service.implementation.EmployeesServiceImpl;
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

    @GetMapping(value = APIMappingValue.API_EMPLOYEE_LIST,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callViewAllEmployeesService(@RequestParam int pageNumber,
                                                      @RequestParam String sortInfo) {
        List<EmployeeListResponse.Employee> employeesFound;

        try {
            employeesFound = employeesServiceImpl.getAllEmployees(pageNumber, sortInfo);
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>(employeesResponseMapper.produceEmployeesFailedResult(HttpStatus.NOT_FOUND.value(), e.getErrorCode(), e.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(employeesResponseMapper.produceViewAllEmployeesSuccessResult(HttpStatus.OK.value(), employeesFound, pageNumber), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_EMPLOYEE_DETAIL,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callViewEmployeeDetailService(@PathVariable String employeeNik) {

        EmployeeModel employee;

        try {
            employee = employeesServiceImpl.getEmployeeData(employeeNik);
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
}
