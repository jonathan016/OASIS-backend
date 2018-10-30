package com.oasis.responsemapper;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.employees.EmployeeDetailResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class EmployeesResponseMapper {

    public PagingResponse<EmployeeListResponse>
    produceViewAllEmployeesSuccessResult(int httpStatusCode,
                                         List<EmployeeListResponse.Employee> mappedEmployees,
                                         int pageNumber) {
        PagingResponse<EmployeeListResponse> successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
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

    public NoPagingResponse<FailedResponse>
    produceEmployeesFailedResult(int httpStatusCode, String errorCode, String errorMessage) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(httpStatusCode);
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }

    public NoPagingResponse<EmployeeDetailResponse>
    produceEmployeeDetailSuccessResponse(int httpStatusCode, EmployeeModel employee, EmployeeModel supervisor) {
        NoPagingResponse<EmployeeDetailResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        if (supervisor != null) {
            successResponse.setValue(
                    new EmployeeDetailResponse(
                            employee.getNik(),
                            employee.getUsername(),
                            employee.getFullname(),
                            new SimpleDateFormat("dd-MM-yyyy").format(employee.getDob()),
                            employee.getPhone(),
                            employee.getJobTitle(),
                            employee.getDivision(),
                            employee.getLocation(),
                            new EmployeeDetailResponse.Supervisor(
                                    supervisor.getNik(),
                                    supervisor.getFullname()
                            )
                    )
            );
        } else {
            successResponse.setValue(
                    new EmployeeDetailResponse(
                            employee.getNik(),
                            employee.getUsername(),
                            employee.getFullname(),
                            new SimpleDateFormat("dd-MM-yyyy").format(employee.getDob()),
                            employee.getPhone(),
                            employee.getJobTitle(),
                            employee.getDivision(),
                            employee.getLocation(),
                            null
                    )
            );
        }

        return successResponse;
    }
}
