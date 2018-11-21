package com.oasis.webmodel.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetDetailResponse {

    private String sku;
    private String name;
    private String location;
    private long stock;
    private String brand;
    private String type;
    private double price;
    private String expendable;
    private String[] images;

}
