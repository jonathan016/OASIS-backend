package com.oasis.webmodel.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetDetailResponse {
    private String assetSku;
    private String assetName;
    private String assetBrand;
    private String assetType;
    private String assetLocation;
    private long assetQuantity;
    private double assetPrice;
    private String[] assetImages;
}
