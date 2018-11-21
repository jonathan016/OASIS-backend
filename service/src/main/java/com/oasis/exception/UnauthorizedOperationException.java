package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import com.oasis.exception.helper.BaseError;

public class UnauthorizedOperationException extends BaseException {

    public UnauthorizedOperationException(BaseError error) {
        super(error.getErrorCode(), error.getErrorMessage());
    }

}
