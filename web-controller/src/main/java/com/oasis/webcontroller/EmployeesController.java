package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.responsemapper.EmployeesResponseMapper;
import com.oasis.service.implementation.EmployeesServiceImpl;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

public class EmployeesController {

    @Autowired
    private EmployeesServiceImpl employeesServiceImpl;
    @Autowired
    private EmployeesResponseMapper employeesResponseMapper;

    @GetMapping(value = APIMappingValue.API_EMPLOYEE_LIST,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callViewAllEmployeesService(@RequestParam int pageNumber,
                                                         @RequestParam String sortInfo) {
        List<EmployeeListResponse.Employee> employeesFound;

        try {
            employeesFound = employeesServiceImpl.getAllEmployees(pageNumber, sortInfo);
        } catch (DataNotFoundException e) {
            return employeesResponseMapper.produceViewAllEmployeesFailedResult(e.getErrorCode(), e.getErrorMessage());
        }


        return employeesResponseMapper.produceViewAllEmployeesSuccessResult(employeesFound, pageNumber);
    }
}
