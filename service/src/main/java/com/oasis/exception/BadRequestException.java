package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import com.oasis.exception.helper.BaseError;

public class BadRequestException extends BaseException {

    public BadRequestException(BaseError error) {
        super(error.getErrorCode(), error.getErrorMessage());
    }

}
