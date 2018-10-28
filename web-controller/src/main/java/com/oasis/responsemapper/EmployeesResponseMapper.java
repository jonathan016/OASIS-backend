package com.oasis.responsemapper;

import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeesResponseMapper {

    public PagingResponse<EmployeeListResponse>
    produceViewAllEmployeesSuccessResult(List<EmployeeListResponse.Employee> mappedEmployees,
                                         int pageNumber){
        PagingResponse<EmployeeListResponse> successResponse = new PagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new EmployeeListResponse(
                        mappedEmployees
                )
        );
        successResponse.setPaging(
                new Paging(
                        pageNumber,
                        ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE,
                        mappedEmployees.size()
                )
        );

        return successResponse;
    }

    public PagingResponse<FailedResponse>
    produceViewAllEmployeesFailedResult(String errorCode, String errorMessage) {
        PagingResponse<FailedResponse> failedResponse = new PagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }
}
