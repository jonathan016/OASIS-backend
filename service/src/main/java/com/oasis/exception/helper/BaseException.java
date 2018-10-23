package com.oasis.exception.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseException extends Exception {

    private String errorCode;
    private String errorMessage;
}
