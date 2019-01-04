package com.oasis.model.exception;

import com.oasis.model.base.BaseError;
import com.oasis.model.base.BaseException;

public class BadRequestException
        extends BaseException {

    public BadRequestException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
