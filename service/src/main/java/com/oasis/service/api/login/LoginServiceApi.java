package com.oasis.service.api.login;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;

import java.util.Map;

public interface LoginServiceApi {

    Map< String, String > getLoginData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UserNotAuthenticatedException;

}
