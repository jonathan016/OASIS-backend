package com.oasis.webmodel.response;

import lombok.Getter;

@Getter
public class LoginResponseModelFAIL {
    private String errorCode;
    private String errorMessage;

    public LoginResponseModelFAIL(String errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
