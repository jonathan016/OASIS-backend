package com.oasis.exception.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@SuppressWarnings("Lombok")
@AllArgsConstructor
public class BaseException
        extends Exception {

    private String errorCode;
    private String errorMessage;

}
