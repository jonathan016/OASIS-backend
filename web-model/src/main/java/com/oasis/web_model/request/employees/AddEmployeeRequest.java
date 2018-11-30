package com.oasis.web_model.request.employees;

import lombok.Data;

@Data
public class AddEmployeeRequest {

    private String username;
    private Employee employee;

    @Data
    public static class Employee {
        private String name;
        private String dob;
        private String phone;
        private String jobTitle;
        private String division;
        private String location;
        private String supervisorUsername;
    }

}
