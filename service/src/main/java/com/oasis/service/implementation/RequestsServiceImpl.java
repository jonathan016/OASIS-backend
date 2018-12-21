package com.oasis.service.implementation;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.BaseEntity;
import com.oasis.model.CollectionName;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.model.fieldname.AssetFieldName;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.service.api.RequestsServiceApi;
import com.oasis.tool.constant.PageSizeConstant;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.constant.StatusConstant;
import com.oasis.tool.helper.ImageHelper;
import com.oasis.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestsServiceImpl
        implements RequestsServiceApi {

    @Autowired
    private ImageHelper imageHelper;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private AssetsServiceApi assetsServiceApi;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeesServiceApi employeesServiceApi;

    /*-------------Requests List Methods-------------*/
    @Override
    public Map< String, List< ? > > getMyRequestsListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > myRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getUsernameRequestsList(username, query, status, page, sort);
        final List< EmployeeModel > employees = getEmployeesDataFromRequest(requests);
        final List< EmployeeModel > modifiers = getRequestModifiersDataFromRequest(requests);
        final List< AssetModel > assets = getAssetDataFromRequest(requests);

        myRequestsListData.put("requests", requests);
        myRequestsListData.put("employees", employees);
        myRequestsListData.put("modifiers", modifiers);
        myRequestsListData.put("assets", assets);

        return myRequestsListData;
    }

    @Override
    public Map< String, List< ? > > getOthersRequestListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > othersRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getOthersRequestListPaged(username, query, status, page, sort);
        final List< EmployeeModel > employees = getEmployeesDataFromRequest(requests);
        final List< AssetModel > assets = getAssetDataFromRequest(requests);

        othersRequestsListData.put("requests", requests);
        othersRequestsListData.put("employees", employees);
        othersRequestsListData.put("assets", assets);

        return othersRequestsListData;
    }

    @Override
    public long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            final String sort
    )
            throws
            BadRequestException {

        final boolean emptyQueryGiven = (query != null && query.isEmpty());
        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyQueryGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (type.equals("Username")) {
                final boolean viewAllRequestsRegardlessOfStatus = (status == null);
                final boolean viewAllRequests = (query == null);

                if (viewAllRequestsRegardlessOfStatus) {
                    if (viewAllRequests) {
                        return requestRepository.countAllByUsername(username);
                    } else {
                        long requestCount = 0;

                        List< AssetModel > assets = assetsServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            requestCount += requestRepository
                                    .countAllByUsernameEqualsAndSkuContainsIgnoreCase(username, asset.getSku());
                        }

                        return requestCount;
                    }
                } else {
                    if (viewAllRequests) {
                        return requestRepository.countAllByUsernameAndStatus(username, status);
                    } else {
                        long requestCount = 0;

                        List< AssetModel > assets = assetsServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            requestCount += requestRepository
                                    .countAllByUsernameEqualsAndSkuContainsIgnoreCase(username, asset.getSku());
                        }

                        return requestCount;
                    }
                }
            } else {
                if (type.equals("Others")) {
                    return getOthersRequestList(username, query, status, page, sort).size();
                }
            }

            return -1;
        }
    }

    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    public Page< AssetModel > getAssetRequestDetailsData(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (skus == null || skus.isEmpty()) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            for (final String sku : skus) {
                final boolean requestedAssetExists = assetsServiceApi.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

                if (!requestedAssetExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }
            }

            final long assetRequestDetails = assetsServiceApi.countAllByDeletedIsFalseAndSkuIn(skus);
            final boolean noRequests = (assetRequestDetails == 0);
            final long totalPages = (long) Math.ceil((double) getAssetRequestDetailsCount(skus, page) /
                                                     PageSizeConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE);
            final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

            if (noRequests || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final int zeroBasedIndexPage = page - 1;
                final Pageable pageable = PageRequest
                        .of(zeroBasedIndexPage, PageSizeConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE);

                final Page< AssetModel > requestedAssets = assetsServiceApi
                        .findAllByDeletedIsFalseAndSkuIn(skus, pageable);

                return requestedAssets;
            }
        }
    }

    @Override
    public List< AssetModel > getAssetRequestDetailsList(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        return getAssetRequestDetailsData(skus, page).getContent();
    }

    @Override
    public List< List< String > > getAssetRequestDetailsImages(
            final List< AssetModel > assets
    ) {

        List< List< String > > imageURLs = new ArrayList<>();

        for (final AssetModel asset : assets) {
            imageURLs.add(getAssetDetailImages(asset.getSku(), asset.getImageDirectory()));
        }

        return imageURLs;
    }

    private List< String > getAssetDetailImages(
            final String sku, final String imageDirectory
    ) {

        List< String > imageURLs = new ArrayList<>();

        if (imageDirectory.isEmpty()) {
            imageURLs.add("http://localhost:8085/oasis/api/assets/" + sku + "/image_not_found?extension=jpeg");
        } else {
            final File directory = new File(imageDirectory);
            final File[] images = directory.listFiles();

            if (Files.exists(directory.toPath()) && images != null) {
                for (int i = 0; i < images.length; i++) {
                    final String extension = imageHelper.getExtensionFromFileName(images[i].getName());

                    imageURLs.add("http://localhost:8085/oasis/api/assets/" + sku + "/" +
                                  sku.concat("-").concat(String.valueOf(i + 1)).concat("?extension=")
                                     .concat(extension));
                }
            } else {
                imageURLs.add("http://localhost:8085/oasis/api/assets/" + sku + "/image_not_found?extension=jpeg");
            }
        }

        return imageURLs;
    }

    @Override
    public long getAssetRequestDetailsCount(
            final List< String > skus, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (skus == null || skus.isEmpty()) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            for (final String sku : skus) {
                final boolean requestedAssetExists = assetsServiceApi.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

                if (!requestedAssetExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }
            }

            return assetsServiceApi.countAllByDeletedIsFalseAndSkuIn(skus);
        }
    }

    /*-------------Save Request Methods-------------*/
    @Override
    public void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UnauthorizedOperationException {

        final boolean employeeWithUsernameExists = employeesServiceApi
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

                    final boolean employeeWithUsernameIsAdministrator = employeesServiceApi
                            .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
                    final boolean employeeWithUsernameDoesNotHaveSupervision = employeesServiceApi
                            .existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(username);
                    final boolean employeeWithUsernameIsTopAdministrator =
                            employeeWithUsernameIsAdministrator && employeeWithUsernameDoesNotHaveSupervision;

                    if (employeeWithUsernameIsTopAdministrator) {
                        throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                    } else {
                        for (final RequestModel request : requests) {
                            final boolean enoughStockExists =
                                    assetsServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku()).getStock() -
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
                        if (!employeesServiceApi
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
                            final boolean expendableAsset = assetsServiceApi
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
            final AssetModel asset = assetsServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku());

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
                if (assetsServiceApi.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku()).getStock() -
                    savedRequest.getQuantity() < 0) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }

                // TODO Check concurrency
                Query query = new Query();
                query.addCriteria(Criteria.where(AssetFieldName.SKU).is(savedRequest.getSku()));
                Update update = new Update().inc(AssetFieldName.STOCK, 0 - savedRequest.getQuantity());
                AssetModel asset = mongoOperations
                        .findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), AssetModel.class,
                                       CollectionName.ASSET_COLLECTION_NAME
                        );

                assetsServiceApi.save(asset);
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
            // TODO Check concurrency
            Query query = new Query();
            query.addCriteria(Criteria.where(AssetFieldName.SKU).is(savedRequest.getSku()));
            Update update = new Update().inc(AssetFieldName.STOCK, savedRequest.getQuantity());
            AssetModel asset = mongoOperations
                    .findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), AssetModel.class,
                                   CollectionName.ASSET_COLLECTION_NAME
                    );

            assetsServiceApi.save(asset);

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
        final boolean expendableAsset = assetsServiceApi.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku())
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

        final boolean administratorWithUsernameExists = employeesServiceApi
                .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
        final boolean supervisorIsValid = employeesServiceApi
                .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(username,
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
            final boolean administratorWithUsernameExists = employeesServiceApi
                    .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);

            if (!administratorWithUsernameExists) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                return true;
            }
        }
    }

    @Override
    public List< RequestModel > findAllBySku(final String sku) {

        return requestRepository.findAllBySku(sku);
    }

    @Override
    public List< RequestModel > findAllByUsernameAndStatus(final String username, final String status) {

        return requestRepository.findAllByUsernameAndStatus(username, status);
    }

    @Override
    public void save(final RequestModel request) {

        requestRepository.save(request);
    }

    @Override
    public long countAllByUsername(final String username) {

        return requestRepository.countAllByUsername(username);
    }

    @Override
    public long countAllByUsernameAndStatus(final String username, final String status) {

        return requestRepository.countAllByUsernameAndStatus(username, status);
    }

    @Override
    public List< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(final String username) {

        return requestRepository.findAllByUsernameOrderByUpdatedDateDesc(username);
    }

    @Override
    public List< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            final String username, final String status
    ) {

        return requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateDesc(username, status);
    }

    @Override
    public Page< RequestModel > findAllByUsernameOrderByUpdatedDateDesc(
            final String username, final Pageable pageable
    ) {

        return requestRepository.findAllByUsernameOrderByUpdatedDateDesc(username, pageable);
    }

    @Override
    public Page< RequestModel > findAllByUsernameAndStatusOrderByUpdatedDateDesc(
            final String username, final String status, final Pageable pageable
    ) {

        return requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateDesc(username, status, pageable);
    }

    private List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > employees = new ArrayList<>();

        for (final RequestModel request : requests) {
            employees.add(employeesServiceApi.findByDeletedIsFalseAndUsername(request.getUsername()));
            employees.get(employees.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(employees.get(employees.size() - 1).getUsername(),
                                           employees.get(employees.size() - 1).getPhoto()
                    ));
        }

        return employees;
    }

    private List< EmployeeModel > getRequestModifiersDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > requestModifiers = new ArrayList<>();

        for (final RequestModel request : requests) {
            final String modifierUsername = request.getUpdatedBy();

            requestModifiers.add(employeesServiceApi.findByDeletedIsFalseAndUsername(modifierUsername));
            requestModifiers.get(requestModifiers.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(requestModifiers.get(requestModifiers.size() - 1).getUsername(),
                                           requestModifiers.get(requestModifiers.size() - 1).getPhoto()
                    ));
        }

        return requestModifiers;
    }

    private List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< AssetModel > assets = new ArrayList<>();

        for (final RequestModel request : requests) {
            assets.add(assetsServiceApi.findByDeletedIsFalseAndSkuEquals(request.getSku()));
            assets.get(assets.size() - 1).setStock(request.getQuantity());
        }

        return assets;
    }

    private String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    ) {

        final boolean validImageLocation = (photoLocation != null && photoLocation.isEmpty());

        if (validImageLocation) {
            final File photo = new File(photoLocation);

            if (photo.exists() && Files.exists(photo.toPath())) {
                return "http://localhost:8085/oasis/api/employees/" + username + "/" +
                       username.concat("?extension=").concat(imageHelper.getExtensionFromFileName(photo.getName()));
            }
        }

        return "http://localhost:8085/oasis/api/employees/" + username + "/image_not_found".concat("?extension=jpeg");
    }

    @SuppressWarnings("ConstantConditions")
    private List< RequestModel > getUsernameRequestsList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = (query != null && query.isEmpty());
        final boolean emptySortGiven = (sort != null && sort.isEmpty());
        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyQueryGiven || emptySortGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            sort = validateSortInformationGiven(sort);

            final boolean viewAllRequestsRegardlessOfStatus = (status == null);

            List< RequestModel > requests = new ArrayList<>();

            if (viewAllRequestsRegardlessOfStatus) {
                final long requestsCount = requestRepository.countAllByUsername(username);
                final boolean noRequests = (requestsCount == 0);
                final long totalPages = (long) Math
                        .ceil((double) getRequestsCount("Username", username, query, status, page, sort) /
                              PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
                final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                if (noRequests || pageIndexOutOfBounds) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    final boolean viewAllRequests = (query == null);

                    final int zeroBasedIndexPage = page - 1;
                    final Pageable pageable = PageRequest
                            .of(zeroBasedIndexPage, PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

                    if (viewAllRequests) {
                        if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameOrderByStatusAsc(username, pageable)
                                                                 .getContent());
                            } else {
                                requests.addAll(
                                        requestRepository.findAllByUsernameOrderByUpdatedDateAsc(username, pageable)
                                                         .getContent());

                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameOrderByStatusDesc(username, pageable)
                                                                 .getContent());
                            } else {
                                requests.addAll(
                                        requestRepository.findAllByUsernameOrderByUpdatedDateDesc(username, pageable)
                                                         .getContent());

                            }
                        }
                    } else {
                        List< AssetModel > assets = assetsServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);
                        for (final AssetModel asset : assets) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                    username, asset.getSku(), pageable).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                    username, asset.getSku(), pageable).getContent());
                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                    username, asset.getSku(), pageable).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                    username, asset.getSku(), pageable).getContent());
                                }
                            }
                        }
                    }
                }
            } else {
                final boolean statusIsRequested = status.equals(StatusConstant.STATUS_REQUESTED);
                final boolean statusIsAccepted = status.equals(StatusConstant.STATUS_ACCEPTED);
                final boolean statusIsRejected = status.equals(StatusConstant.STATUS_REJECTED);
                final boolean statusIsCancelled = status.equals(StatusConstant.STATUS_CANCELLED);
                final boolean statusIsDelivered = status.equals(StatusConstant.STATUS_DELIVERED);
                final boolean statusIsReturned = status.equals(StatusConstant.STATUS_RETURNED);

                final boolean incorrectStatusValue =
                        !statusIsRequested && !statusIsAccepted && !statusIsRejected && !statusIsCancelled &&
                        !statusIsDelivered && !statusIsReturned;

                if (incorrectStatusValue) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    final long requestsCount = requestRepository.countAllByUsernameAndStatus(username, status);
                    final boolean noRequests = (requestsCount == 0);
                    final long totalPages = (long) Math
                            .ceil((double) getRequestsCount("Username", username, query, status, page, sort) /
                                  PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
                    final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                    if (noRequests || pageIndexOutOfBounds) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        final boolean viewAllRequests = (query == null);

                        final int zeroBasedIndexPage = page - 1;
                        final Pageable pageable = PageRequest
                                .of(zeroBasedIndexPage, PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

                        if (viewAllRequests) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameAndStatusOrderByUpdatedDateAsc(username,
                                                                                                             status,
                                                                                                             pageable
                                                            ).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameAndStatusOrderByUpdatedDateAsc(username,
                                                                                                             status,
                                                                                                             pageable
                                                            ).getContent());

                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameAndStatusOrderByUpdatedDateDesc(username,
                                                                                                              status,
                                                                                                              pageable
                                                            ).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameAndStatusOrderByUpdatedDateDesc(username,
                                                                                                              status,
                                                                                                              pageable
                                                            ).getContent());

                                }
                            }
                        } else {
                            List< AssetModel > assets = assetsServiceApi
                                    .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);
                            for (final AssetModel asset : assets) {
                                if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                    if (sort.substring(2).equals("status")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                        username, status, asset.getSku(), pageable)
                                                                .getContent());
                                    } else {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                        username, status, asset.getSku(), pageable)
                                                                .getContent());
                                    }
                                } else {
                                    if (sort.substring(2).equals("status")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                        username, status, asset.getSku(), pageable)
                                                                .getContent());
                                    } else {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                        username, status, asset.getSku(), pageable)
                                                                .getContent());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());

            return requests;
        }
    }

    private List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException {

        final boolean emptyQueryGiven = (query != null && query.isEmpty());
        final boolean emptySortGiven = (sort != null && sort.isEmpty());
        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyQueryGiven || emptySortGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            sort = validateSortInformationGiven(sort);

            List< SupervisionModel > supervisions = employeesServiceApi
                    .findAllByDeletedIsFalseAndSupervisorUsername(username);

            List< String > supervisedEmployeesUsernames = new ArrayList<>();

            for (final SupervisionModel supervision : supervisions) {
                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
            }

            List< RequestModel > requests = new ArrayList<>();

            for (final String supervisedEmployeeUsername : supervisedEmployeesUsernames) {
                boolean administratorWithUsernameExists = employeesServiceApi
                        .existsAdminModelByDeletedIsFalseAndUsernameEquals(supervisedEmployeeUsername);
                boolean supervisorIsValid = employeesServiceApi
                        .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(supervisedEmployeeUsername);
                boolean usernameIsAdminOrSupervisor = administratorWithUsernameExists || supervisorIsValid;

                if (usernameIsAdminOrSupervisor) {
                    requests.addAll(getOthersRequestList(supervisedEmployeeUsername, query, status, page, sort));
                }

                final boolean viewAllRequestsRegardlessOfStatus = (status == null);
                final boolean viewAllRequests = (query == null);

                if (viewAllRequestsRegardlessOfStatus) {
                    if (viewAllRequests) {
                        if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository
                                                        .findAllByUsernameOrderByStatusAsc(supervisedEmployeeUsername));
                            } else {
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameOrderByUpdatedDateAsc(
                                            supervisedEmployeeUsername));
                                }
                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameOrderByStatusDesc(
                                        supervisedEmployeeUsername));
                            } else {
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameOrderByUpdatedDateDesc(
                                            supervisedEmployeeUsername));
                                }
                            }
                        }
                    } else {
                        List< AssetModel > assets = assetsServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                }
                            }
                        }
                    }
                } else {
                    if (viewAllRequests) {
                        if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameAndStatusContainsOrderByStatusAsc(
                                        supervisedEmployeeUsername, status));
                            } else {
                                requests.addAll(requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateAsc(
                                        supervisedEmployeeUsername, status));
                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameAndStatusContainsOrderByStatusDesc(
                                        supervisedEmployeeUsername, status));
                            } else {
                                requests.addAll(requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateDesc(
                                        supervisedEmployeeUsername, status));
                            }
                        }
                    } else {
                        List< AssetModel > assets = assetsServiceApi
                                .findAllByDeletedIsFalseAndNameContainsIgnoreCase(query);

                        for (final AssetModel asset : assets) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                    supervisedEmployeeUsername, status,
                                                                    asset.getSku()
                                                            ));
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                    supervisedEmployeeUsername, status,
                                                                    asset.getSku()
                                                            ));
                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                    supervisedEmployeeUsername, status,
                                                                    asset.getSku()
                                                            ));
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                    supervisedEmployeeUsername, status,
                                                                    asset.getSku()
                                                            ));
                                }
                            }
                        }
                    }
                }
            }

            requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());

            return requests;
        }
    }

    private String validateSortInformationGiven(String sort)
            throws
            BadRequestException {

        final boolean useDefaultSort = (sort == null);

        if (useDefaultSort) {
            sort = "D-updatedDate";
        } else {
            final boolean properSortFormatGiven = sort.matches(Regex.REGEX_REQUEST_SORT);

            if (!properSortFormatGiven) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            }
        }
        return sort;
    }

    private List< RequestModel > getOthersRequestListPaged(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException {

        final List< RequestModel > requests = getOthersRequestList(username, query, status, page, sort);
        final long totalPages = (long) Math.ceil((double) requests.size() / PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
        final boolean noRequests = requests.isEmpty();
        final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        PagedListHolder< RequestModel > pagedListHolder = new PagedListHolder<>(new ArrayList<>(requests));
        pagedListHolder.setPage(page - 1);
        pagedListHolder.setPageSize(PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

        return new ArrayList<>(pagedListHolder.getPageList());
    }

}
