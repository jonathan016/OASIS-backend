package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RequestsServiceApi {

    Map< String, List< ? > > getMyRequestsListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    Map< String, List< ? > > getOthersRequestListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

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

    void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UnauthorizedOperationException;

    List< RequestModel > findAllBySku(final String sku);

    List< RequestModel > findAllByUsernameAndStatus(final String username, final String status);

    void save(final RequestModel request);

    long countAllByUsername(final String username);

    long countAllByUsernameAndStatus(final String username, final String status);

    List< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(final String username);

    List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(final String username, final String status);

    Page< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            final String username, final Pageable pageable
    );

    Page< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            final String username, final String status, final Pageable pageable
    );

}
