package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class DataNotFoundException extends BaseException {

    public DataNotFoundException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}