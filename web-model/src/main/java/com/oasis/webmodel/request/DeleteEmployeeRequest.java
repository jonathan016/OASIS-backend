package com.oasis.webmodel.request;

import lombok.Data;

@Data
public class DeleteEmployeeRequest {
    private String adminNik;
    private String employeeNik;
}
