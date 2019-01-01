package com.oasis.web_model.response.success.endpoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointResponse {

    private String username;
    private String name;
    private String photo;
    private String role;

}