package com.oasis.web_model.response.failed;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailedResponse {

    private String errorCode;
    private String errorMessage;

}
