package com.oasis.response_mapper;

import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.success.login.LoginResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoginResponseMapper {

    public NoPagingResponse< LoginResponse > produceLoginSuccessResponse(
            final int httpStatusCode, final String username, final String name, final String photo, final String role
    ) {

        NoPagingResponse< LoginResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new LoginResponse(username, name, photo, role));

        return successResponse;
    }

    public BaseResponse produceSideBarActiveComponentResponse(
            final int httpStatusCode, final Map< String, Boolean > activeComponents
    ) {

        BaseResponse successResponse = new BaseResponse();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setComponents(activeComponents);

        return successResponse;
    }

}
