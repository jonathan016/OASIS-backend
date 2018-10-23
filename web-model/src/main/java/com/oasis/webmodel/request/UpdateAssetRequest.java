package com.oasis.webmodel.request;

import lombok.Data;

@Data
public class UpdateAssetRequest {
    private String employeeNik;
    private UpdateAssetRequest.Asset asset;

    @Data
    public class Asset {
        private String assetSku;
        private String assetName;
        private String assetLocation;
        private String assetBrand;
        private String assetType;
        private long assetQty;
        private double assetPrice;
    }
}
