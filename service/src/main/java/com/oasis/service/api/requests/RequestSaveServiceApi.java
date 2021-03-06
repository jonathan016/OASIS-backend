package com.oasis.service.api.requests;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;

public interface RequestSaveServiceApi {

    void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UnauthorizedOperationException;

    List< AssetModel > getAssetRequestDetailsList(
            final List< String > skus
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< List< String > > getAssetRequestDetailsImages(
            final List< AssetModel > assets
    );

}
