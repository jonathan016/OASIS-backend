package com.oasis.webmodel.request;

import lombok.Getter;

@Getter
public class LoginRequestModel {
    private String employeeUsername;
    private String employeePassword;
}