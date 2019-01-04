package com.oasis.model.exception;

import com.oasis.model.base.BaseError;
import com.oasis.model.base.BaseException;

public class DuplicateDataException
        extends BaseException {

    public DuplicateDataException(BaseError error) {

        super(error.getErrorCode(), error.getErrorMessage());
    }

}
