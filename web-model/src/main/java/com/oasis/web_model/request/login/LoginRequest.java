package com.oasis.web_model.request.login;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;

}