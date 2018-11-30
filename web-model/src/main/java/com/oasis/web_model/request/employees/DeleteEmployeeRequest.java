package com.oasis.web_model.request.employees;

import lombok.Data;

@Data
public class DeleteEmployeeRequest {

    private String adminUsername;
    private String employeeUsername;

}
