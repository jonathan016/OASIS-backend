package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;

import java.util.Map;

public interface LoginServiceApi {

    Map< String, String > getLoginData(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UserNotAuthenticatedException;

    String getUsernameIfEmployeeWithCredentialExists(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException,
            BadRequestException;

    String getFirstNameFromUsername(
            final String username
    );

    String getRoleFromUsername(
            final String username
    )
            throws
            DataNotFoundException;

}
