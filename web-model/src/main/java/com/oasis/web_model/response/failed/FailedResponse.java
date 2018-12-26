package com.oasis.web_model.response.failed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedResponse {

    private String errorCode;
    private String errorMessage;

}
