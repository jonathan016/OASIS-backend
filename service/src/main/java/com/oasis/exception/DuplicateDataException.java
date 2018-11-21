package com.oasis.exception;

import com.oasis.exception.helper.BaseException;

public class DuplicateDataException extends BaseException {

    public DuplicateDataException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
