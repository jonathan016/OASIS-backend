package com.oasis.response_mapper;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.*;
import com.oasis.web_model.response.success.employees.EmployeeDetailResponse;
import com.oasis.web_model.response.success.employees.EmployeeListResponse;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class EmployeesResponseMapper {

    public PagingResponse<EmployeeListResponse>
    produceViewFoundEmployeesSuccessResult(final int httpStatusCode,
                                           final List<EmployeeListResponse.Employee> mappedEmployees,
                                           final int pageNumber) {

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
                        (int) Math.ceil((double) mappedEmployees.size() / ServiceConstant.EMPLOYEES_FIND_EMPLOYEE_PAGE_SIZE),
                        mappedEmployees.size()
                )
        );

        return successResponse;
    }

    public NoPagingResponse<EmployeeDetailResponse>
    produceEmployeeDetailSuccessResponse(final int httpStatusCode, final EmployeeModel employee,
                                         final EmployeeModel supervisor) {

        NoPagingResponse<EmployeeDetailResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        if (supervisor != null) {
            successResponse.setValue(
                    new EmployeeDetailResponse(
                            employee.getUsername(),
                            employee.getUsername(),
                            employee.getName(),
                            new SimpleDateFormat("dd-MM-yyyy").format(employee.getDob()),
                            employee.getPhone(),
                            employee.getJobTitle(),
                            employee.getDivision(),
                            employee.getLocation(),
                            new EmployeeDetailResponse.Supervisor(
                                    supervisor.getUsername(),
                                    supervisor.getName()
                            )
                    )
            );
        } else {
            successResponse.setValue(
                    new EmployeeDetailResponse(
                            employee.getUsername(),
                            employee.getUsername(),
                            employee.getName(),
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
    produceEmployeeSaveSuccessResult(final int httpStatusCode){

        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

}
