package com.oasis.response_mapper;

import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.failed.FailedResponse;
import org.springframework.stereotype.Component;

@Component
public class FailedResponseMapper {

    public NoPagingResponse< FailedResponse > produceFailedResult(
            final int httpStatusCode, final String errorCode, final String errorMessage
    ) {

        NoPagingResponse< FailedResponse > failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(httpStatusCode);
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(new FailedResponse(errorCode, errorMessage));

        return failedResponse;
    }

}
