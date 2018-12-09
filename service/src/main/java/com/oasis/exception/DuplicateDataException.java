package com.oasis.exception;

import com.oasis.exception.helper.BaseError;
import com.oasis.exception.helper.BaseException;

public class DuplicateDataException
        extends BaseException {

    public DuplicateDataException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
