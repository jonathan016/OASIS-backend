package com.oasis.exception;

import com.oasis.exception.helper.BaseError;
import com.oasis.exception.helper.BaseException;

public class UnauthorizedOperationException
        extends BaseException {

    public UnauthorizedOperationException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
