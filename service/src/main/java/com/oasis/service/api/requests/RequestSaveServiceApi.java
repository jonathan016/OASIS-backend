package com.oasis.service.api.requests;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RequestSaveServiceApi {

    void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UnauthorizedOperationException;

    Page< AssetModel > getAssetRequestDetailsData(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< AssetModel > getAssetRequestDetailsList(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< List< String > > getAssetRequestDetailsImages(
            final List< AssetModel > assets
    );

    long getAssetRequestDetailsCount(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
