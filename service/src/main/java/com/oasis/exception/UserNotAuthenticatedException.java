package com.oasis.exception;

import com.oasis.exception.helper.BaseError;
import com.oasis.exception.helper.BaseException;

public class UserNotAuthenticatedException
        extends BaseException {

    public UserNotAuthenticatedException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}