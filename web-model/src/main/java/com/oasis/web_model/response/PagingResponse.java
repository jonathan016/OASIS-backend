package com.oasis.web_model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("Lombok")
public class PagingResponse < T >
        extends BaseResponse {

    private T value;
    private Paging paging;

}
