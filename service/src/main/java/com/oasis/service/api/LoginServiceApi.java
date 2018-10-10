package com.oasis.service.api;

import com.oasis.model.entity.EmployeeModel;

public interface LoginServiceApi {
    public EmployeeModel checkLoginCredentials(String username, String password);
}
