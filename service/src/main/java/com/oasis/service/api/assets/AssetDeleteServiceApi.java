package com.oasis.service.api.assets;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;

import java.util.List;

public interface AssetDeleteServiceApi {

    void deleteAssets(
            final List< String > skus, final String username
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
