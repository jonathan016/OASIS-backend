package com.oasis.web_model.response;

import lombok.Data;

@Data
@SuppressWarnings("Lombok")
public class PagingResponse< T >
        extends BaseResponse {

    private T value;
    private Paging paging;

}
