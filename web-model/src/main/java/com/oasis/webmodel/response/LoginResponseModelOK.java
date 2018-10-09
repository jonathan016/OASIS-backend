package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class LoginResponseModelOK {
    private String employeeId;

    public LoginResponseModelOK(String employeeId){
        this.employeeId = employeeId;
    }
}