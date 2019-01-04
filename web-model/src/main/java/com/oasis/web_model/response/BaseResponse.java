package com.oasis.web_model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {

    protected int code;
    protected String success;
    protected Map< String, Boolean > components;

}
