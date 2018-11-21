package com.oasis.exception;

import com.oasis.exception.helper.BaseException;

public class BadRequestException extends BaseException {

    public BadRequestException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
