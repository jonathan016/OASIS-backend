package com.oasis.web_controller.mapper.response;

import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.BaseResponse;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.success.entry_point.EntryPointResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EntryPointResponseMapper {

    public NoPagingResponse< EntryPointResponse > produceLoginSuccessResponse(
            final int httpStatusCode, final String username, final String name, final String photo, final String role
    ) {

        NoPagingResponse< EntryPointResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new EntryPointResponse(username, name, photo, role));

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
