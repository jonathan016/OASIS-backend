package com.oasis.webmodel.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteAssetRequest {
    private String employeeNik;
    private List<String> selectedAssets;
}
