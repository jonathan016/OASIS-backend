package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class PagingResponse<T> extends BaseResponse {

    private T value;
    private Paging paging;
}
