package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import com.oasis.exception.helper.BaseError;

public class DuplicateDataException extends BaseException {

    public DuplicateDataException(BaseError error) {
        super(error.getErrorCode(), error.getErrorMessage());
    }

}
