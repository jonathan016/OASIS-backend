package com.oasis.response_mapper;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.employees.EmployeeDetailResponse;
import com.oasis.web_model.response.success.employees.EmployeeListResponse;
import com.oasis.web_model.response.success.employees.EmployeeSaveAddResponse;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeesResponseMapper {

    public PagingResponse< EmployeeListResponse > produceViewFoundEmployeesSuccessResult(
            final int httpStatusCode, final List< EmployeeModel > employees, final List< EmployeeModel > supervisors,
            final List< String > photos, final int pageNumber, final long totalRecords
    ) {

        PagingResponse< EmployeeListResponse > successResponse = new PagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
        employeeDataFactory.classMap(EmployeeModel.class, EmployeeListResponse.Employee.class);
        MapperFactory employeeSupervisorDataFactory = new DefaultMapperFactory.Builder().build();
        employeeSupervisorDataFactory.classMap(EmployeeModel.class, EmployeeListResponse.Employee.Supervisor.class);

        List< EmployeeListResponse.Employee > mappedEmployees = new ArrayList<>();

        for (int i = 0; i < employees.size(); i++) {
            mappedEmployees.add(
                    employeeDataFactory.getMapperFacade(EmployeeModel.class, EmployeeListResponse.Employee.class)
                                       .map(employees.get(i)));
            mappedEmployees.get(mappedEmployees.size() - 1)
                           .setPhoto(photos.get(i));
            if (supervisors.get(i) != null) {
                mappedEmployees.get(mappedEmployees.size() - 1)
                               .setSupervisor(employeeSupervisorDataFactory.getMapperFacade(EmployeeModel.class,
                                                                                            EmployeeListResponse.Employee.Supervisor.class
                               )
                                                                           .map(supervisors.get(i)));
            } else {
                // For top administrator, who does not have any supervisor at all
                mappedEmployees.get(mappedEmployees.size() - 1)
                               .setSupervisor(null);
            }
        }

        successResponse.setValue(new EmployeeListResponse(mappedEmployees));

        successResponse.setPaging(new Paging(pageNumber, ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE,
                                             (int) Math.ceil((double) totalRecords /
                                                             ServiceConstant.EMPLOYEES_LIST_PAGE_SIZE),
                                             totalRecords
        ));

        return successResponse;
    }

    public NoPagingResponse< EmployeeDetailResponse > produceEmployeeDetailSuccessResponse(
            final int httpStatusCode, final EmployeeModel employee, final String photo, final EmployeeModel supervisor
    ) {

        NoPagingResponse< EmployeeDetailResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        if (supervisor != null) {
            successResponse.setValue(new EmployeeDetailResponse(employee.getUsername(), employee.getName(),
                                                                new SimpleDateFormat("dd-MM-yyyy").format(
                                                                        employee.getDob()), photo, employee.getPhone(),
                                                                employee.getJobTitle(), employee.getDivision(),
                                                                employee.getLocation(),
                                                                new EmployeeDetailResponse.Supervisor(
                                                                        supervisor.getUsername(), supervisor.getName())
            ));
        } else {
            successResponse.setValue(new EmployeeDetailResponse(employee.getUsername(), employee.getName(),
                                                                new SimpleDateFormat("dd-MM-yyyy").format(
                                                                        employee.getDob()), photo, employee.getPhone(),
                                                                employee.getJobTitle(), employee.getDivision(),
                                                                employee.getLocation(), null
            ));
        }

        return successResponse;
    }

    public BaseResponse produceEmployeeSaveUpdateSuccessResult(final int httpStatusCode) {

        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        return successResponse;
    }

    public NoPagingResponse produceEmployeeSaveAddSuccessResult(
            final int httpStatusCode, final String username
    ) {

        NoPagingResponse< EmployeeSaveAddResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        successResponse.setValue(new EmployeeSaveAddResponse(username));

        return successResponse;
    }

}
