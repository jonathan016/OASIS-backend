package com.oasis.webmodel.request.assets;

import lombok.Data;

import java.util.List;

@Data
public class DeleteAssetRequest {

    private String nik;
    private List<String> assetSkus;

}
