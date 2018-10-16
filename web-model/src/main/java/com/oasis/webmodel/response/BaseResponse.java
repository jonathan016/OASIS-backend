package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class BaseResponse {
    protected Integer code;
    protected String success;
}
