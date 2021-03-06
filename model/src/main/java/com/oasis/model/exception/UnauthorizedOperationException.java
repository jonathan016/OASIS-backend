package com.oasis.model.exception;

import com.oasis.model.base.BaseError;
import com.oasis.model.base.BaseException;

public class UnauthorizedOperationException
        extends BaseException {

    public UnauthorizedOperationException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
