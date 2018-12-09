package com.oasis.web_model.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AssetListResponse {

    private List< Asset > availableAssets;

    @Data
    @AllArgsConstructor
    public static class Asset {

        private String sku;
        private String name;
        private String brand;
        private String type;
        private String location;
        private long quantity;

    }

}
