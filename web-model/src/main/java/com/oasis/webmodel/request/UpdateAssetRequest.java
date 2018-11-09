package com.oasis.webmodel.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UpdateAssetRequest {
    private String employeeNik;
    private UpdateAssetRequest.Asset asset;

    @Data
    @AllArgsConstructor
    public static class Asset {
        private String assetSku;
        private String assetName;
        private String assetLocation;
        private String assetBrand;
        private String assetType;
        private long assetQty;
        private double assetPrice;
    }
}
