package com.oasis.web_model.request.employees;

import lombok.Data;

@Data
public class UpdateEmployeeRequest {

    private String username;
    private Employee employee;

    @Data
    public static class Employee {
        private String username;
        private String name;
        private String dob;
        private String password;
        private String phone;
        private String jobTitle;
        private String division;
        private String location;
        private String supervisorUsername;
    }

}
