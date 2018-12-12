package com.oasis.web_model.request.employees;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String username;
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;

}
