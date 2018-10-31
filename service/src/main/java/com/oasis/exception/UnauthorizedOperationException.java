package com.oasis.exception;

import com.oasis.exception.helper.BaseException;

public class UnauthorizedOperationException extends BaseException {

    public UnauthorizedOperationException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
