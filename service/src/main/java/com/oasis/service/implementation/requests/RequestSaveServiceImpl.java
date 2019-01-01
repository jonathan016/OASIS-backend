package com.oasis.service.implementation.requests;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.model.constant.entity_constant.CollectionName;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.constant.entity_constant.field_name.AssetFieldName;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.assets.AssetDetailServiceApi;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestSaveServiceApi;
import com.oasis.model.constant.service_constant.StatusConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestSaveServiceImpl
        implements RequestSaveServiceApi {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AssetDetailServiceApi assetDetailServiceApi;
    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;

    @Autowired
    private MongoOperations mongoOperations;



    @Override
    public void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UnauthorizedOperationException {

        final boolean employeeWithUsernameExists = employeeUtilServiceApi
                .existsEmployeeModelByDeletedIsFalseAndUsername(username);

        if (!employeeWithUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            if (requests.isEmpty()) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            } else {
                final boolean createRequestOperation = isNewRequestsValid(requests);

                RequestModel savedRequest;
                if (createRequestOperation) {
                    validateRequestedAssets(requests);

                    final boolean employeeWithUsernameIsAdministrator = employeeUtilServiceApi
                            .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
                    final boolean employeeWithUsernameDoesNotHaveSupervision = employeeUtilServiceApi
                            .existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(username);
                    final boolean employeeWithUsernameIsTopAdministrator =
                            employeeWithUsernameIsAdministrator && employeeWithUsernameDoesNotHaveSupervision;

                    if (employeeWithUsernameIsTopAdministrator) {
                        throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                    } else {
                        for (final RequestModel request : requests) {
                            final boolean enoughStockExists =
                                    assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku()).getStock() -
                                    request.getQuantity() >= 0;

                            if (!enoughStockExists) {
                                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                            } else {
                                savedRequest = request;

                                savedRequest.setStatus(StatusConstant.STATUS_REQUESTED);
                                savedRequest.setTransactionNote(null);
                                savedRequest.setCreatedBy(username);
                                savedRequest.setCreatedDate(new Date());

                                savedRequest.setUpdatedBy(username);
                                savedRequest.setUpdatedDate(new Date());

                                requestRepository.save(savedRequest);
                            }
                        }
                    }
                } else {
                    RequestModel request = requests.get(0);

                    savedRequest = requestRepository.findBy_id(request.get_id());

                    if (savedRequest == null) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else if (request.getStatus() == null) {
                        throw new BadRequestException(INCORRECT_PARAMETER);
                    } else {
                        if (!employeeUtilServiceApi
                                .existsEmployeeModelByDeletedIsFalseAndUsername(request.getUsername())) {
                            throw new DataNotFoundException(DATA_NOT_FOUND);
                        }

                        final boolean newRequestStatusIsCancelled = request.getStatus()
                                                                           .equals(StatusConstant.STATUS_CANCELLED);
                        final boolean newRequestStatusIsAccepted = request.getStatus()
                                                                          .equals(StatusConstant.STATUS_ACCEPTED);
                        final boolean newRequestStatusIsRejected = request.getStatus()
                                                                          .equals(StatusConstant.STATUS_REJECTED);
                        final boolean newRequestStatusIsDelivered = request.getStatus()
                                                                           .equals(StatusConstant.STATUS_DELIVERED);

                        if (newRequestStatusIsCancelled) {
                            updateStatusToCancelled(savedRequest, username, request.getUsername(),
                                                    savedRequest.getStatus(), request.getStatus()
                            );
                        } else if (newRequestStatusIsAccepted || newRequestStatusIsRejected) {
                            updateAssetDataAndStatusToAcceptedOrRejected(username, request, savedRequest);
                        } else {
                            final boolean expendableAsset = assetUtilServiceApi
                                    .findByDeletedIsFalseAndSkuEquals(savedRequest.getSku()).isExpendable();

                            if (newRequestStatusIsDelivered) {
                                if (!expendableAsset) {
                                    updateStatusToDelivered(username, savedRequest, request.getStatus());
                                } else {
                                    updateAssetDataAndStatusToReturned(username, savedRequest, request.getStatus());
                                }
                            } else {
                                if (!expendableAsset) {
                                    updateAssetDataAndStatusToReturned(username, savedRequest, request.getStatus());
                                } else {
                                    throw new BadRequestException(INCORRECT_PARAMETER);
                                }
                            }
                        }
                    }
                    savedRequest.setTransactionNote(request.getTransactionNote());
                    savedRequest.setUpdatedBy(username);
                    savedRequest.setUpdatedDate(new Date());

                    requestRepository.save(savedRequest);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    public List< AssetModel > getAssetRequestDetailsList(
            final List< String > skus
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (skus == null || skus.isEmpty()) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            for (final String sku : skus) {
                final boolean requestedAssetExists = assetUtilServiceApi.existsAssetModelByDeletedIsFalseAndSkuEquals(
                        sku);

                if (!requestedAssetExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }
            }

            final long assetRequestDetails = assetUtilServiceApi.countAllByDeletedIsFalseAndSkuIn(skus);
            final boolean noRequests = ( assetRequestDetails == 0 );

            if (noRequests) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final List< AssetModel > requestedAssets = assetUtilServiceApi
                        .findAllByDeletedIsFalseAndStockGreaterThanZeroAndSkuIn(skus);

                if (skus.size() != requestedAssets.size()) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    return requestedAssets;
                }
            }
        }
    }

    @Override
    public List< List< String > > getAssetRequestDetailsImages(
            final List< AssetModel > assets
    ) {

        List< List< String > > imageURLs = new ArrayList<>();

        for (final AssetModel asset : assets) {
            imageURLs.add(assetDetailServiceApi.getAssetDetailImages(asset.getSku(), asset.getImageDirectory()));
        }

        return imageURLs;
    }

    private boolean isNewRequestsValid(final List< RequestModel > requests)
            throws
            BadRequestException {

        final boolean existingRequestFound = requests.stream().anyMatch(requestModel -> requestModel.get_id() != null);
        final boolean moreThanOneNewRequest = requests.size() > 1;

        if (moreThanOneNewRequest && existingRequestFound) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            return requests.size() != 1 || !existingRequestFound;
        }
    }

    private void validateRequestedAssets(final List< RequestModel > requests)
            throws
            DataNotFoundException,
            UnauthorizedOperationException {

        for (final RequestModel request : requests) {
            final AssetModel asset = assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku());

            if (asset == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            final boolean requestQuantityLargerThanStock = request.getQuantity() > asset.getStock();

            if (requestQuantityLargerThanStock) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            }
        }
    }

    private void updateStatusToCancelled(
            RequestModel savedRequest, final String username, final String recordedRequesterUsername,
            final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        if (isRequestCancellationValid(username, recordedRequesterUsername, currentRequestStatus, newRequestStatus)) {
            savedRequest.setStatus(StatusConstant.STATUS_CANCELLED);
        }
    }

    private void updateAssetDataAndStatusToAcceptedOrRejected(
            final String username, final RequestModel request, RequestModel savedRequest
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException {

        if (isRequestAcceptanceOrRejectionValid(
                username, savedRequest.getUsername(), savedRequest.getStatus(), request.getStatus())) {
            savedRequest.setStatus(request.getStatus());

            final boolean newRequestStatusIsAccepted = request.getStatus().equals(StatusConstant.STATUS_ACCEPTED);

            if (newRequestStatusIsAccepted) {
                if (assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku()).getStock() -
                    savedRequest.getQuantity() < 0) {
                    final List< RequestModel > requestsOfAssetWithSku = requestRepository
                            .findAllBySkuEqualsAndStatusEquals(request.getSku(), StatusConstant.STATUS_REQUESTED);
                    for (RequestModel requestOfAssetWithSku : requestsOfAssetWithSku) {
                        requestOfAssetWithSku.setStatus(StatusConstant.STATUS_REJECTED);
                        requestOfAssetWithSku.setTransactionNote("Asset stock is currently not available. Please try" +
                                                                 "again some other time.");

                        saveRequests(username, Collections.singletonList(requestOfAssetWithSku));
                    }

                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }

                Query query = new Query();
                query.addCriteria(Criteria.where(AssetFieldName.SKU).is(savedRequest.getSku()));
                Update update = new Update().inc(AssetFieldName.STOCK, 0 - savedRequest.getQuantity());
                AssetModel asset = mongoOperations
                        .findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), AssetModel.class,
                                       CollectionName.ASSET_COLLECTION_NAME
                        );

                assetUtilServiceApi.save(asset);
            }
        }
    }

    private void updateStatusToDelivered(
            final String username, RequestModel savedRequest, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final String currentRequestStatus = savedRequest.getStatus();

        if (isRequestDeliveryValid(username, currentRequestStatus, newRequestStatus)) {
            savedRequest.setStatus(StatusConstant.STATUS_DELIVERED);
        }
    }

    private void updateAssetDataAndStatusToReturned(
            final String username, RequestModel savedRequest, final String newRequestStatus
    )
            throws
            BadRequestException,
            UnauthorizedOperationException {

        if (isRequestDeliveryOrReturnValid(username, savedRequest, newRequestStatus)) {
            Query query = new Query();
            query.addCriteria(Criteria.where(AssetFieldName.SKU).is(savedRequest.getSku()));
            Update update = new Update().inc(AssetFieldName.STOCK, savedRequest.getQuantity());
            AssetModel asset = mongoOperations
                    .findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), AssetModel.class,
                                   CollectionName.ASSET_COLLECTION_NAME
                    );

            assetUtilServiceApi.save(asset);

            savedRequest.setStatus(StatusConstant.STATUS_RETURNED);
        }
    }

    private boolean isRequestCancellationValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean statusIsRequested = currentRequestStatus.equals(StatusConstant.STATUS_REQUESTED);
        final boolean newRequestStatusIsCancelled = newRequestStatus.equals(StatusConstant.STATUS_CANCELLED);
        final boolean requestedToCancelled = statusIsRequested && newRequestStatusIsCancelled;

        if (!requestedToCancelled) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean usernameIsRequester = recordedRequesterUsername.equals(username);

            if (!usernameIsRequester) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                return true;
            }
        }
    }

    private boolean isRequestAcceptanceOrRejectionValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean usernameIsAdminOrSupervisor = isUsernameAdminOrSupervisor(username, recordedRequesterUsername);

        final boolean statusIsRequested = currentRequestStatus.equals(StatusConstant.STATUS_REQUESTED);
        final boolean newRequestStatusIsAccepted = newRequestStatus.equals(StatusConstant.STATUS_ACCEPTED);
        final boolean newRequestStatusIsRejected = newRequestStatus.equals(StatusConstant.STATUS_REJECTED);
        final boolean requestedToAccepted = statusIsRequested && newRequestStatusIsAccepted;
        final boolean requestedToRejected = statusIsRequested && newRequestStatusIsRejected;
        final boolean requestedToAcceptedOrRejected = requestedToAccepted || requestedToRejected;

        if (!requestedToAcceptedOrRejected) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (!usernameIsAdminOrSupervisor) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                return true;
            }
        }
    }

    private boolean isRequestDeliveryValid(
            final String username, final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean statusIsAccepted = currentRequestStatus.equals(StatusConstant.STATUS_ACCEPTED);
        final boolean newRequestStatusIsDelivered = newRequestStatus.equals(StatusConstant.STATUS_DELIVERED);
        final boolean acceptedToDelivered = statusIsAccepted && newRequestStatusIsDelivered;

        return isAssetDeliveryOrReturnValid(username, acceptedToDelivered);
    }

    private boolean isRequestDeliveryOrReturnValid(
            final String username, final RequestModel savedRequest, final String newRequestStatus
    )
            throws
            BadRequestException,
            UnauthorizedOperationException {

        final boolean statusIsAccepted = savedRequest.getStatus().equals(StatusConstant.STATUS_ACCEPTED);
        final boolean expendableAsset = assetUtilServiceApi.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku())
                                                           .isExpendable();
        final boolean newRequestStatusIsDelivered = newRequestStatus.equals(StatusConstant.STATUS_DELIVERED);
        final boolean acceptedToDelivered = statusIsAccepted && newRequestStatusIsDelivered;
        final boolean expendableAssetDelivery = expendableAsset && acceptedToDelivered;

        final boolean statusIsDelivered = savedRequest.getStatus().equals(StatusConstant.STATUS_DELIVERED);
        final boolean newRequestStatusIsReturned = newRequestStatus.equals(StatusConstant.STATUS_RETURNED);
        final boolean deliveredToReturned = statusIsDelivered && newRequestStatusIsReturned;
        final boolean nonExpendableAssetReturn = !expendableAsset && deliveredToReturned;

        final boolean acceptedOrDeliveredToReturned = expendableAssetDelivery || nonExpendableAssetReturn;

        return isAssetDeliveryOrReturnValid(username, acceptedOrDeliveredToReturned);
    }

    private boolean isUsernameAdminOrSupervisor(final String username, final String requesterUsername) {

        final boolean administratorWithUsernameExists = employeeUtilServiceApi
                .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
        final boolean supervisorIsValid = employeeUtilServiceApi
                .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
                        username,
                        requesterUsername
                );

        return administratorWithUsernameExists || supervisorIsValid;
    }

    private boolean isAssetDeliveryOrReturnValid(final String username, final boolean acceptedOrDeliveredToReturned)
            throws
            BadRequestException,
            UnauthorizedOperationException {

        if (!acceptedOrDeliveredToReturned) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean administratorWithUsernameExists = employeeUtilServiceApi
                    .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);

            if (!administratorWithUsernameExists) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                return true;
            }
        }
    }

}
