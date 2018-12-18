package com.oasis.web_model.request.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetRequestDetailsRequest {

    List< String > skus;
    int page;

}
