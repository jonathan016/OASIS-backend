package com.oasis.web_model.response.success.employees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class EmployeeListResponse {
    private List<Employee> employeeList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Employee {
        private String nik;
        private String name;
        private String jobTitle;
        private String location;
        private Supervisor supervisor = null;

        @Data
        @AllArgsConstructor
        public static class Supervisor {
            private String nik;
            private String name;
        }
    }
}
