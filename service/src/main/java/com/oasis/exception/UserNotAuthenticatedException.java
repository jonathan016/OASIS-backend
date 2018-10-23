package com.oasis.exception;

import com.oasis.exception.helper.BaseException;

public class UserNotAuthenticatedException extends BaseException {

    public UserNotAuthenticatedException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}