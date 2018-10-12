package com.oasis.service.api;

import com.oasis.model.entity.EmployeeModel;

public interface LoginServiceApi {
    EmployeeModel checkLoginCredentials(String username, String password);
    String determineUserRole(String employeeId);
}
