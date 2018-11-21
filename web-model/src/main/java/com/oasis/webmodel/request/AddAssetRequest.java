package com.oasis.webmodel.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AddAssetRequest {
    private String employeeNik;
    private Asset asset;

    @Data
    @AllArgsConstructor
    public static class Asset {
        private String assetName;
        private String assetLocation;
        private String assetBrand;
        private String assetType;
        private long assetQty;
        private double assetPrice;
    }
}
