package com.oasis.webmodel.response.failed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFailedResponse {
    private String errorCode;
    private String errorMessage;
}
