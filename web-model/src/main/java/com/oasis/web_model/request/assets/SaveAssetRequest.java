package com.oasis.web_model.request.assets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAssetRequest {

    private SaveAssetRequest.Asset asset;

    @Data
    @NoArgsConstructor
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
