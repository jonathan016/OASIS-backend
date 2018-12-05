package com.oasis.request_mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.web_model.request.employees.SaveEmployeeRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.oasis.exception.helper.ErrorCodeAndMessage.NO_ASSET_SELECTED;

@Component
public class EmployeesRequestMapper {

    public String getAdminUsernameFromRawData(final String rawEmployeeData) {

        String adminUsername;

        try {
            adminUsername = new ObjectMapper().readTree(rawEmployeeData).path("username").asText();
        } catch (IOException e) {
            return "";
        }

        return adminUsername;
    }

    public boolean checkAddOperationFromRawData(final String rawEmployeeData) throws UnauthorizedOperationException {

        JsonNode employee;

        try {
            employee = new ObjectMapper().readTree(rawEmployeeData).path("employee");
        } catch (IOException e) {
            //TODO throw real exception cause
            throw new UnauthorizedOperationException(NO_ASSET_SELECTED);
        }

        return employee.path("username").isNull();
    }

    public EmployeeModel getEmployeeModelFromRawData(final String rawEmployeeData, final boolean isAddOperation) {

        SaveEmployeeRequest.Employee request;

        try {

            JsonNode employee = new ObjectMapper().readTree(rawEmployeeData).path("employee");

            if (isAddOperation){
                request = new SaveEmployeeRequest.Employee(
                        null,
                        employee.path("name").asText(),
                        employee.path("dob").asText(),
                        employee.path("phone").asText(),
                        employee.path("jobTitle").asText(),
                        employee.path("division").asText(),
                        employee.path("location").asText(),
                        employee.path("supervisorUsername").asText()
                );
            } else {
                request = new SaveEmployeeRequest.Employee(
                        employee.path("username").asText(),
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
        } catch (ParseException e) {
            //TODO log exception
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
//        return employeeDataFactory.getMapperFacade(SaveEmployeeRequest.Employee.class, EmployeeModel.class).map(request);
    }

    public String getSupervisorUsernameFromRawData(final String rawEmployeeData) {

        String supervisorUsername;

        try {
            supervisorUsername =
                    new ObjectMapper().readTree(rawEmployeeData).path("employee").path("supervisorUsername").asText();
        } catch (IOException e) {
            return "";
        }

        return supervisorUsername;
    }

}
