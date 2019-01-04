package com.oasis.web_model.request.employees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveEmployeeRequest {

    private SaveEmployeeRequest.Employee employee;

    @Data
    @NoArgsConstructor
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
