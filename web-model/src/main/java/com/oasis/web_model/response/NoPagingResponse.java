package com.oasis.web_model.response;

import lombok.Data;

@Data
@SuppressWarnings("Lombok")
public class NoPagingResponse<T> extends BaseResponse {

    private T value;

}
