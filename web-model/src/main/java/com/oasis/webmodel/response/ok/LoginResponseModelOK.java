package com.oasis.webmodel.response.ok;

import lombok.Getter;

@Getter
public class LoginResponseModelOK {
    private String employeeId;

    public LoginResponseModelOK(String employeeId){
        this.employeeId = employeeId;
    }
}