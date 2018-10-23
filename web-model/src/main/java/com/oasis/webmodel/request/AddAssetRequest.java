package com.oasis.webmodel.request;

import lombok.Data;

@Data
public class AddAssetRequest {
    private String employeeNik;
    private Asset asset;

    @Data
    public class Asset {
        private String assetName;
        private String assetLocation;
        private String assetBrand;
        private String assetType;
        private long assetQty;
        private double assetPrice;
    }
}
