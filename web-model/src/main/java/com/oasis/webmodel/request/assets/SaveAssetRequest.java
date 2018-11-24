package com.oasis.webmodel.request.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SaveAssetRequest {

    private String nik;
    private SaveAssetRequest.Asset asset;
    private boolean hasNewImage;

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
