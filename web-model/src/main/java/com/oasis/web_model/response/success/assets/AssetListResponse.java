package com.oasis.web_model.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetListResponse {

    private List< Asset > availableAssets;

    @Data
    @NoArgsConstructor
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
