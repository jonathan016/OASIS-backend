package com.oasis.exception;

import com.oasis.exception.helper.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException {

    public BadRequestException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
