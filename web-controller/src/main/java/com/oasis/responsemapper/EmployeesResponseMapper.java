package com.oasis.responsemapper;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.ServiceConstant;
import com.oasis.webmodel.response.*;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.employees.EmployeeDetailResponse;
import com.oasis.webmodel.response.success.employees.EmployeeListResponse;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class EmployeesResponseMapper {

    public PagingResponse<EmployeeListResponse>
    produceViewFoundEmployeesSuccessResult(int httpStatusCode,
                                           List<EmployeeListResponse.Employee> mappedEmployees,
                                           int pageNumber) {
        PagingResponse<EmployeeListResponse> successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        if(mappedEmployees.size() - ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE * pageNumber > 0){
            successResponse.setValue(
                    new EmployeeListResponse(
                            mappedEmployees.subList(ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE * pageNumber - ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE * pageNumber)
                    )
            );
        } else {
            successResponse.setValue(
                    new EmployeeListResponse(
                            mappedEmployees.subList(ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE * pageNumber - ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE, ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE * pageNumber - ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE + mappedEmployees.size() % ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE)
                    )
            );
        }
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

    public BaseResponse
    produceEmployeeSaveSuccessResult(int httpStatusCode){
        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }
}
