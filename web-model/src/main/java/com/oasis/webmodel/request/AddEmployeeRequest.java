package com.oasis.webmodel.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddEmployeeRequest {
    private String employeeNik;
    private Employee employee;

    @Data
    @AllArgsConstructor
    public static class Employee {
        private String fullname;
        private String dob;
        private String phone;
        private String jobTitle;
        private String division;
        private String location;
        private String supervisorId;
    }
}
