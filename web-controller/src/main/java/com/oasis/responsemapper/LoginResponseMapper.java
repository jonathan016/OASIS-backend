package com.oasis.responsemapper;

import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

    public NoPagingResponse<LoginResponse> produceSuccessResponse(
            final String employeeNik, final String role
    ) {
        NoPagingResponse<LoginResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new LoginResponse(employeeNik, role));

        return successResponse;
    }

    public NoPagingResponse<FailedResponse> produceFailedResponse(
            final String errorCode, final String errorMessage
    ) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(new FailedResponse(errorCode, errorMessage));

        return failedResponse;
    }
}
