package com.oasis.model.exception;

import com.oasis.model.base.BaseError;
import com.oasis.model.base.BaseException;

public class DataNotFoundException
        extends BaseException {

    public DataNotFoundException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}