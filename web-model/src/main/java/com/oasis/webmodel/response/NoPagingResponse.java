package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class NoPagingResponse<T> extends BaseResponse {
    private T value;
}
