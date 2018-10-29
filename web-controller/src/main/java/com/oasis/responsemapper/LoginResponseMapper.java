package com.oasis.responsemapper;

import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

    public NoPagingResponse<LoginResponse> produceLoginSuccessResponse(
            final int httpStatusCode, final String employeeNik, final String role
    ) {
        NoPagingResponse<LoginResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new LoginResponse(employeeNik, role));

        return successResponse;
    }

    public NoPagingResponse<FailedResponse> produceLoginFailedResponse(
            final int httpStatusCode, final String errorCode, final String errorMessage
    ) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(httpStatusCode);
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(new FailedResponse(errorCode, errorMessage));

        return failedResponse;
    }
}
