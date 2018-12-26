package com.oasis.web_model.response.success.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String username;
    private String name;
    private String photo;
    private String role;

}