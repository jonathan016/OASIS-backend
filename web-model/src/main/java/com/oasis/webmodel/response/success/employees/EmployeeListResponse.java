package com.oasis.webmodel.response.success.employees;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EmployeeListResponse {
    private List<Employee> employeeList;

    @Data
    @AllArgsConstructor
    public static class Employee {
        private String employeeNik;
        private String employeeName;
        private String employeeJobTitle;
        private String employeeLocation;
        private Supervisor supervisor;

        @Data
        @AllArgsConstructor
        public static class Supervisor {
            private String supervisorNik;
            private String supervisorName;
        }
    }
}
