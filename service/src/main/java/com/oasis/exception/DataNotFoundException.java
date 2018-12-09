package com.oasis.exception;

import com.oasis.exception.helper.BaseError;
import com.oasis.exception.helper.BaseException;

public class DataNotFoundException
        extends BaseException {

    public DataNotFoundException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}