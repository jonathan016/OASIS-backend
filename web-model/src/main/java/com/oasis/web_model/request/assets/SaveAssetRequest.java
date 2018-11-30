package com.oasis.web_model.request.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SaveAssetRequest {

    private String username;
    private SaveAssetRequest.Asset asset;

    @Data
    @AllArgsConstructor
    public static class Asset {

        private String sku;
        private String name;
        private String location;
        private String brand;
        private String type;
        private long quantity;
        private double price;
        private boolean expendable;

    }

}
