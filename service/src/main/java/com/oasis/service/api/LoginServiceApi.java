package com.oasis.service.api;

import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;

public interface LoginServiceApi {

    EmployeeModel checkLoginCredentials(final String username, final String password)
            throws DataNotFoundException, UserNotAuthenticatedException;

    String determineUserRole(final String username) throws DataNotFoundException;

}
