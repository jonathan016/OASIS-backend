package com.oasis.web_controller.mapper.response;

import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.success.endpoint.EndpointResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EndpointResponseMapper {

    public NoPagingResponse< EndpointResponse > produceLoginSuccessResponse(
            final int httpStatusCode, final String username, final String name, final String photo, final String role
    ) {

        NoPagingResponse< EndpointResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new EndpointResponse(username, name, photo, role));

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
