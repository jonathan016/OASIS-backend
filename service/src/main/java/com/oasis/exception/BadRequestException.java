package com.oasis.exception;

import com.oasis.exception.helper.BaseError;
import com.oasis.exception.helper.BaseException;

public class BadRequestException
        extends BaseException {

    public BadRequestException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
