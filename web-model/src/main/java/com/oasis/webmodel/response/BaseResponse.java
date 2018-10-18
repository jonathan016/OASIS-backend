package com.oasis.webmodel.response;

import lombok.Data;

@Data
public class BaseResponse {
    protected int code;
    protected String success;
}
