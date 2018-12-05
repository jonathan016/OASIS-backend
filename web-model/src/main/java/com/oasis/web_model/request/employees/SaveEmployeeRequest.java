package com.oasis.web_model.request.employees;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SaveEmployeeRequest {

    private String username;
    private SaveEmployeeRequest.Employee employee;

    @Data
    @AllArgsConstructor
    public static class Employee {

        private String username;
        private String name;
        private String dob;
        private String phone;
        private String jobTitle;
        private String division;
        private String location;
        private String supervisorUsername;

    }

}
