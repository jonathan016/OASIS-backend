package com.oasis.webmodel.request;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateEmployeeRequest {
    private String adminNik;
    private Employee employee;

    @Data
    public static class Employee {
        private String employeeNik;
        private String employeeFullname;
        private String employeeDob;
        private String employeePassword;
        private String employeePhone;
        private String employeeJobTitle;
        private String employeeDivision;
        private String employeeLocation;
        private String employeeSupervisorId;
    }
}
