package com.oasis.request_mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.exception.BadRequestException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.web_model.request.employees.SaveEmployeeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Component
public class EmployeesRequestMapper {

    private Logger logger = LoggerFactory.getLogger(EmployeesRequestMapper.class);

    public boolean isCreateEmployeeOperation(final String rawEmployeeData)
            throws
            BadRequestException {

        JsonNode employee;

        try {
            employee = new ObjectMapper().readTree(rawEmployeeData).path("employee");
        } catch (IOException ioException) {
            logger.error(
                    "Failed to read attribute 'employee' from passed JSON data as IOException occurred with message: " +
                    ioException.getMessage());
            throw new BadRequestException(INCORRECT_PARAMETER);
        }

        return employee.path("username").isNull();
    }

    public EmployeeModel getEmployeeModelFromRawData(
            final String rawEmployeeData, final boolean addEmployeeOperation
    ) {

        SaveEmployeeRequest.Employee request;

        try {

            JsonNode employee = new ObjectMapper().readTree(rawEmployeeData).path("employee");

            if (addEmployeeOperation) {
                request = new SaveEmployeeRequest.Employee(null, employee.path("name").asText(),
                                                           employee.path("dob").asText(),
                                                           employee.path("phone").asText(),
                                                           employee.path("jobTitle").asText(),
                                                           employee.path("division").asText(),
                                                           employee.path("location").asText(),
                                                           employee.path("supervisorUsername").asText()
                );
            } else {
                request = new SaveEmployeeRequest.Employee(employee.path("username").asText(),
                                                           employee.path("name").asText(),
                                                           employee.path("dob").asText(),
                                                           employee.path("phone").asText(),
                                                           employee.path("jobTitle").asText(),
                                                           employee.path("division").asText(),
                                                           employee.path("location").asText(),
                                                           employee.path("supervisorUsername").asText()
                );
            }

        } catch (IOException e) {
            return null;
        }

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername(request.getUsername());
        employee.setName(request.getName());
        try {
            employee.setDob(new SimpleDateFormat("dd/MM/yyyy").parse(request.getDob()));
        } catch (ParseException parseException) {
            logger.error("Failed to parse given DOB as ParseException occurred with message: " +
                         parseException.getMessage());
        }
        employee.setPhone(request.getPhone());
        employee.setJobTitle(request.getJobTitle());
        employee.setDivision(request.getDivision());
        employee.setLocation(request.getLocation());

        return employee;

        //        MapperFactory employeeDataFactory = new DefaultMapperFactory.Builder().build();
        //        employeeDataFactory.classMap(SaveEmployeeRequest.Employee.class, EmployeeModel.class)
        //                        .exclude("supervisorUsername")
        //                        .register();
        //
        //        return employeeDataFactory.getMapperFacade(SaveEmployeeRequest.Employee.class, EmployeeModel.class)
        // .map(request);
    }

    public String getSupervisorUsernameFromRawData(final String rawEmployeeData) {

        String supervisorUsername;

        try {
            supervisorUsername = new ObjectMapper().readTree(rawEmployeeData).path("employee")
                                                   .path("supervisorUsername").asText();
        } catch (IOException ioException) {
            logger.error(
                    "Failed to read attribute 'supervisorUsername' from passed JSON data as IOException occurred with" +
                    " message: " + ioException.getMessage());
            return "";
        }

        return supervisorUsername;
    }

}
