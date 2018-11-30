package com.oasis.response_mapper;

import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.success.login.LoginResponse;
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

}
