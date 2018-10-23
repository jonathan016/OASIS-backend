package com.oasis.webmodel.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AssetListResponse {
    private List<Asset> assetList;

    @Data
    @AllArgsConstructor
    public static class Asset {
        private String assetId;
        private String assetName;
        private String assetBrand;
        private String assetType;
        private String assetLocation;
        private Integer assetQty;
    }
}
