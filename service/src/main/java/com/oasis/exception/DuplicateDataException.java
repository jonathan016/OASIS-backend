package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDataException extends BaseException {

    public DuplicateDataException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
