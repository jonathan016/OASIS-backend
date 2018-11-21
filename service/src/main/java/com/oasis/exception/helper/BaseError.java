package com.oasis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseError {

    private String errorCode;
    private String errorMessage;

}
