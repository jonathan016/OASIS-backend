package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import org.springframework.data.domain.Page;

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

    List< RequestModel > getUsernameRequestsList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException;

    String validateSortInformationGiven(String sort)
            throws
            BadRequestException;

    List< RequestModel > getOthersRequestListPaged(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException;

    List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    );

    List< EmployeeModel > getRequestModifiersDataFromRequest(
            final List< RequestModel > requests
    );

    List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    );

    long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    );

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

    List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    );

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

    void validateRequestedAssets(final List< RequestModel > requests)
            throws
            DataNotFoundException,
            UnauthorizedOperationException;

    boolean isNewRequestsValid(final List< RequestModel > requests)
            throws
            BadRequestException;

    boolean isUsernameAdminOrSupervisor(final String username, final String requesterUsername);

    void updateStatusToCancelled(
            RequestModel savedRequest, final String username, final String recordedRequesterUsername,
            final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException;

    void updateAssetDataAndStatusToAcceptedOrRejected(
            final String username, final RequestModel request, RequestModel savedRequest
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException;

    void updateStatusToDelivered(
            final String username, RequestModel savedRequest, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException;

    void updateAssetDataAndStatusToReturned(
            final String username, RequestModel savedRequest, final String newRequestStatus
    )
            throws
            BadRequestException,
            UnauthorizedOperationException;

    boolean isRequestCancellationValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException;

    boolean isRequestAcceptanceOrRejectionValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException;

    boolean isRequestDeliveryValid(
            final String username, final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException;

    boolean isRequestDeliveryOrReturnValid(
            final String username, final RequestModel savedRequest, final String newRequestStatus
    )
            throws
            BadRequestException,
            UnauthorizedOperationException;

    boolean isAssetDeliveryOrReturnValid(final String username, final boolean acceptedOrDeliveredToReturned)
            throws
            BadRequestException,
            UnauthorizedOperationException;

}
