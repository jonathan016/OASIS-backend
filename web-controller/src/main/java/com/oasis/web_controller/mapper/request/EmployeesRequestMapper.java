package com.oasis.web_controller.mapper.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.web_model.request.employees.SaveEmployeeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Component
public class EmployeesRequestMapper {

    private Logger logger = LoggerFactory.getLogger(EmployeesRequestMapper.class);

    public boolean isAddEmployeeOperation(final String rawEmployeeData)
            throws
            BadRequestException {

        final JsonNode employee;

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

        final SaveEmployeeRequest.Employee request;

        try {

            final JsonNode employee = new ObjectMapper().readTree(rawEmployeeData).path("employee");

            if (addEmployeeOperation) {
                request = new SaveEmployeeRequest.Employee(null, employee.path("name").textValue(),
                                                           employee.path("dob").textValue(),
                                                           employee.path("phone").textValue(),
                                                           employee.path("jobTitle").textValue(),
                                                           employee.path("division").textValue(),
                                                           employee.path("location").textValue(),
                                                           employee.path("supervisorUsername").textValue()
                );
            } else {
                request = new SaveEmployeeRequest.Employee(
                        employee.path("username").textValue(),
                        employee.path("name").textValue(),
                        employee.path("dob").textValue(),
                        employee.path("phone").textValue(),
                        employee.path("jobTitle").textValue(),
                        employee.path("division").textValue(),
                        employee.path("location").textValue(),
                        employee.path("supervisorUsername").textValue()
                );
            }

        } catch (IOException e) {
            return null;
        }

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername(request.getUsername());
        employee.setName(request.getName());
        try {
            employee.setDob(new SimpleDateFormat("yyyy-MM-dd").parse(request.getDob()));
        } catch (ParseException | NullPointerException exception) {
            logger.error("Failed to parse given DOB as ParseException or NullPointerException occurred with message: " +
                         exception.getMessage());
        }
        employee.setPhone(request.getPhone());
        employee.setJobTitle(request.getJobTitle());
        employee.setDivision(request.getDivision());
        employee.setLocation(request.getLocation());

        return employee;
    }

    public String getSupervisorUsernameFromRawData(final String rawEmployeeData) {

        final String supervisorUsername;

        try {
            supervisorUsername = new ObjectMapper().readTree(rawEmployeeData).path("employee")
                                                   .path("supervisorUsername").textValue();
        } catch (IOException ioException) {
            logger.error(
                    "Failed to read attribute 'supervisorUsername' from passed JSON data as IOException occurred with" +
                    " message: " + ioException.getMessage());
            return null;
        }

        return supervisorUsername;
    }

}
