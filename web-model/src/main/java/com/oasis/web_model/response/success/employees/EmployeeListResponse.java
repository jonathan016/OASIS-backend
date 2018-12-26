package com.oasis.web_model.response.success.employees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {

    private List< EmployeeListResponse.Employee > employees;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Employee {

        private String username;
        private String name;
        private String photo;
        private String jobTitle;
        private String location;
        private EmployeeListResponse.Employee.Supervisor supervisor;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Supervisor {

            private String username;
            private String name;

        }

    }

}
