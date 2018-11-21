package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import com.oasis.model.BaseError;

public class UserNotAuthenticatedException extends BaseException {

    public UserNotAuthenticatedException(BaseError error) {
        super(error.getErrorCode(), error.getErrorMessage());
    }

}