package com.oasis.webmodel.response.success.employees;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class EmployeeDetailResponse {
    private String employeeNik;
    private String employeeUsername;
    private String employeeFullname;
    private Date employeeDob;
    private String employeePhone;
    private String employeeJobTitle;
    private String employeeDivision;
    private String employeeLocation;
    private Supervisor supervisor;

    @Data
    @AllArgsConstructor
    public static class Supervisor {
        private String supervisorId;
        private String supervisorName;
    }
}
