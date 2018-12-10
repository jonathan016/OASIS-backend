package com.oasis.service.api;

import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;

public interface LoginServiceApi {

    String checkLoginCredentials(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException;

}
