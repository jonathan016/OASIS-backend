package com.oasis.web_model.response;

import lombok.Data;

import java.util.Map;

@Data
public class BaseResponse {

    protected int code;
    protected String success;
    protected Map< String, Boolean > components;

}
