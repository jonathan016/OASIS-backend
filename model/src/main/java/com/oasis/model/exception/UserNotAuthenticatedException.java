package com.oasis.model.exception;

import com.oasis.model.base.BaseError;
import com.oasis.model.base.BaseException;

public class UserNotAuthenticatedException
        extends BaseException {

    public UserNotAuthenticatedException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}