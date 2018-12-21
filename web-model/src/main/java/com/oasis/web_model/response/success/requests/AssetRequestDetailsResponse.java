package com.oasis.web_model.response.success.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetRequestDetailsResponse {

    List< AssetRequestDetailsObject > requestedAssets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetRequestDetailsObject {

        private String sku;
        private String name;
        private long quantity;
        private List< String > images;

    }

}
