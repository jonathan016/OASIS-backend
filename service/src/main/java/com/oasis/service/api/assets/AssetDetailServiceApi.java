package com.oasis.service.api.assets;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;

import java.util.List;

public interface AssetDetailServiceApi {

    AssetModel getAssetDetailData(
            String sku
    )
            throws
            DataNotFoundException;

    List< String > getAssetDetailImages(
            String sku, String imageDirectory
    );

    byte[] getAssetDetailInPdf(
            String sku
    );

}
