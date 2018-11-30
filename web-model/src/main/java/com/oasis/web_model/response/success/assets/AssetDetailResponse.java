package com.oasis.web_model.response.success.assets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
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
    private List<String> images;

}
