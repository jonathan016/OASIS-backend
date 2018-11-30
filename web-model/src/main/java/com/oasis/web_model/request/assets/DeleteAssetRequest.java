package com.oasis.web_model.request.assets;

import lombok.Data;

import java.util.List;

@Data
public class DeleteAssetRequest {

    private String username;
    private List<String> skus;

}
