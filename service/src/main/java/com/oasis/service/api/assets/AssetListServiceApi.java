package com.oasis.service.api.assets;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;

import java.util.List;

public interface AssetListServiceApi {

    List< AssetModel > getAvailableAssetsList(
            final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    long getAvailableAssetsCount(
            final String query, final String sort
    );

}
