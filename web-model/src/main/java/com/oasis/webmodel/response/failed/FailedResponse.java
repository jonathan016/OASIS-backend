package com.oasis.webmodel.response.failed;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailedResponse {
    private String errorCode;
    private String errorMessage;
}
