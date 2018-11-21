package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import com.oasis.exception.helper.BaseError;

public class DataNotFoundException extends BaseException {

    public DataNotFoundException(BaseError error) {
        super(error.getErrorCode(), error.getErrorMessage());
    }

}