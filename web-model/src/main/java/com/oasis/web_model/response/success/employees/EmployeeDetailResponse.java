package com.oasis.web_model.response.success.employees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailResponse {

    private String username;
    private String name;
    private String dob;
    private String photo;
    private String phone;
    private String jobTitle;
    private String division;
    private String location;
    private Supervisor supervisor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Supervisor {

        private String username;
        private String name;

    }

}
