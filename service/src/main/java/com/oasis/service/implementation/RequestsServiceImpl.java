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
import com.oasis.repository.*;
import com.oasis.service.ImageHelper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.RequestsServiceApi;
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
    private AdminRepository adminRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

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
    @SuppressWarnings("ConstantConditions")
    public List< RequestModel > getUsernameRequestsList(
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
                              ServiceConstant.REQUESTS_LIST_PAGE_SIZE);
                final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                if (noRequests || pageIndexOutOfBounds) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    final boolean viewAllRequests = (query == null);

                    final int zeroBasedIndexPage = page - 1;
                    final Pageable pageable = PageRequest
                            .of(zeroBasedIndexPage, ServiceConstant.REQUESTS_LIST_PAGE_SIZE);

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
                        List< AssetModel > assets = assetRepository
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
                final boolean statusIsRequested = status.equals(ServiceConstant.STATUS_REQUESTED);
                final boolean statusIsAccepted = status.equals(ServiceConstant.STATUS_ACCEPTED);
                final boolean statusIsRejected = status.equals(ServiceConstant.STATUS_REJECTED);
                final boolean statusIsCancelled = status.equals(ServiceConstant.STATUS_CANCELLED);
                final boolean statusIsDelivered = status.equals(ServiceConstant.STATUS_DELIVERED);
                final boolean statusIsReturned = status.equals(ServiceConstant.STATUS_RETURNED);

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
                                  ServiceConstant.REQUESTS_LIST_PAGE_SIZE);
                    final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                    if (noRequests || pageIndexOutOfBounds) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        final boolean viewAllRequests = (query == null);

                        final int zeroBasedIndexPage = page - 1;
                        final Pageable pageable = PageRequest
                                .of(zeroBasedIndexPage, ServiceConstant.REQUESTS_LIST_PAGE_SIZE);

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
                            List< AssetModel > assets = assetRepository
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

    @Override
    public List< RequestModel > getOthersRequestList(
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

            List< SupervisionModel > supervisions = supervisionRepository
                    .findAllByDeletedIsFalseAndSupervisorUsername(username);

            List< String > supervisedEmployeesUsernames = new ArrayList<>();

            for (final SupervisionModel supervision : supervisions) {
                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
            }

            List< RequestModel > requests = new ArrayList<>();

            for (final String supervisedEmployeeUsername : supervisedEmployeesUsernames) {
                boolean administratorWithUsernameExists = adminRepository
                        .existsAdminModelByDeletedIsFalseAndUsernameEquals(supervisedEmployeeUsername);
                boolean supervisorIsValid = supervisionRepository
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
                        List< AssetModel > assets = assetRepository
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
                        List< AssetModel > assets = assetRepository
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

    @Override
    public String validateSortInformationGiven(String sort)
            throws
            BadRequestException {

        final boolean useDefaultSort = (sort == null);

        if (useDefaultSort) {
            sort = "D-updatedDate";
        } else {
            final boolean properSortFormatGiven = sort.matches("^[AD]-(status|updatedDate)$");

            if (!properSortFormatGiven) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            }
        }
        return sort;
    }

    @Override
    public List< RequestModel > getOthersRequestListPaged(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException {

        final List< RequestModel > requests = getOthersRequestList(username, query, status, page, sort);
        final long totalPages = (long) Math.ceil((double) requests.size() / ServiceConstant.REQUESTS_LIST_PAGE_SIZE);
        final boolean noRequests = requests.isEmpty();
        final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        PagedListHolder< RequestModel > pagedListHolder = new PagedListHolder<>(new ArrayList<>(requests));
        pagedListHolder.setPage(page - 1);
        pagedListHolder.setPageSize(ServiceConstant.REQUESTS_LIST_PAGE_SIZE);

        return new ArrayList<>(pagedListHolder.getPageList());
    }

    @Override
    public List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > employees = new ArrayList<>();

        for (final RequestModel request : requests) {
            employees.add(employeeRepository.findByDeletedIsFalseAndUsername(request.getUsername()));
            employees.get(employees.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(employees.get(employees.size() - 1).getUsername(),
                                           employees.get(employees.size() - 1).getPhoto()
                    ));
        }

        return employees;
    }

    @Override
    public List< EmployeeModel > getRequestModifiersDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > requestModifiers = new ArrayList<>();

        for (final RequestModel request : requests) {
            final String modifierUsername = request.getUpdatedBy();

            requestModifiers.add(employeeRepository.findByDeletedIsFalseAndUsername(modifierUsername));
            requestModifiers.get(requestModifiers.size() - 1).setPhoto(
                    getEmployeeDetailPhoto(requestModifiers.get(requestModifiers.size() - 1).getUsername(),
                                           requestModifiers.get(requestModifiers.size() - 1).getPhoto()
                    ));
        }

        return requestModifiers;
    }

    @Override
    public List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< AssetModel > assets = new ArrayList<>();

        for (final RequestModel request : requests) {
            assets.add(assetRepository.findByDeletedIsFalseAndSkuEquals(request.getSku()));
            assets.get(assets.size() - 1).setStock(request.getQuantity());
        }

        return assets;
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

                        List< AssetModel > assets = assetRepository
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

                        List< AssetModel > assets = assetRepository
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
    public String getEmployeeDetailPhoto(
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
                final boolean requestedAssetExists = assetRepository.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

                if (!requestedAssetExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }
            }

            final long assetRequestDetails = assetRepository.countAllByDeletedIsFalseAndSkuIn(skus);
            final boolean noRequests = (assetRequestDetails == 0);
            final long totalPages = (long) Math.ceil((double) getAssetRequestDetailsCount(skus, page) /
                                                     ServiceConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE);
            final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

            if (noRequests || pageIndexOutOfBounds) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final int zeroBasedIndexPage = page - 1;
                final Pageable pageable = PageRequest
                        .of(zeroBasedIndexPage, ServiceConstant.ASSET_REQUEST_DETAILS_LIST_PAGE_SIZE);

                final Page< AssetModel > requestedAssets = assetRepository
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
    public List< String > getAssetDetailImages(
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
    public List< List< String > > getAssetRequestDetailsImages(
            final List< AssetModel > assets
    ) {

        List< List< String > > imageURLs = new ArrayList<>();

        for (final AssetModel asset : assets) {
            imageURLs.add(getAssetDetailImages(asset.getSku(), asset.getImageDirectory()));
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
                final boolean requestedAssetExists = assetRepository.existsAssetModelByDeletedIsFalseAndSkuEquals(sku);

                if (!requestedAssetExists) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                }
            }

            return assetRepository.countAllByDeletedIsFalseAndSkuIn(skus);
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

        final boolean employeeWithUsernameExists = employeeRepository
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

                    final boolean employeeWithUsernameIsAdministrator = adminRepository
                            .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
                    final boolean employeeWithUsernameDoesNotHaveSupervision = employeeRepository
                            .existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(username);
                    final boolean employeeWithUsernameIsTopAdministrator =
                            employeeWithUsernameIsAdministrator && employeeWithUsernameDoesNotHaveSupervision;

                    if (employeeWithUsernameIsTopAdministrator) {
                        throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                    } else {
                        for (final RequestModel request : requests) {
                            final boolean enoughStockExists =
                                    assetRepository.findByDeletedIsFalseAndSkuEquals(request.getSku()).getStock() -
                                    request.getQuantity() >= 0;

                            if (!enoughStockExists) {
                                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                            } else {
                                savedRequest = request;

                                savedRequest.setStatus(ServiceConstant.STATUS_REQUESTED);
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
                        if (!employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsername(request.getUsername())) {
                            throw new DataNotFoundException(DATA_NOT_FOUND);
                        }

                        final boolean newRequestStatusIsCancelled = request.getStatus()
                                                                           .equals(ServiceConstant.STATUS_CANCELLED);
                        final boolean newRequestStatusIsAccepted = request.getStatus()
                                                                          .equals(ServiceConstant.STATUS_ACCEPTED);
                        final boolean newRequestStatusIsRejected = request.getStatus()
                                                                          .equals(ServiceConstant.STATUS_REJECTED);
                        final boolean newRequestStatusIsDelivered = request.getStatus()
                                                                           .equals(ServiceConstant.STATUS_DELIVERED);

                        if (newRequestStatusIsCancelled) {
                            updateStatusToCancelled(savedRequest, username, request.getUsername(),
                                                    savedRequest.getStatus(), request.getStatus()
                            );
                        } else if (newRequestStatusIsAccepted || newRequestStatusIsRejected) {
                            updateAssetDataAndStatusToAcceptedOrRejected(username, request, savedRequest);
                        } else {
                            final boolean expendableAsset = assetRepository
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
    public void validateRequestedAssets(final List< RequestModel > requests)
            throws
            DataNotFoundException,
            UnauthorizedOperationException {

        for (final RequestModel request : requests) {
            final AssetModel asset = assetRepository.findByDeletedIsFalseAndSkuEquals(request.getSku());

            if (asset == null) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            final boolean requestQuantityLargerThanStock = request.getQuantity() > asset.getStock();

            if (requestQuantityLargerThanStock) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            }
        }
    }

    @Override
    public boolean isNewRequestsValid(final List< RequestModel > requests)
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

    @Override
    public boolean isUsernameAdminOrSupervisor(final String username, final String requesterUsername) {

        final boolean administratorWithUsernameExists = adminRepository
                .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
        final boolean supervisorIsValid = supervisionRepository
                .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(username,
                                                                                                requesterUsername
                );

        return administratorWithUsernameExists || supervisorIsValid;
    }

    @Override
    public void updateStatusToCancelled(
            RequestModel savedRequest, final String username, final String recordedRequesterUsername,
            final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        if (isRequestCancellationValid(username, recordedRequesterUsername, currentRequestStatus, newRequestStatus)) {
            savedRequest.setStatus(ServiceConstant.STATUS_CANCELLED);
        }
    }

    @Override
    public void updateAssetDataAndStatusToAcceptedOrRejected(
            final String username, final RequestModel request, RequestModel savedRequest
    )
            throws
            UnauthorizedOperationException,
            BadRequestException,
            DataNotFoundException {

        if (isRequestAcceptanceOrRejectionValid(
                username, savedRequest.getUsername(), savedRequest.getStatus(), request.getStatus())) {
            savedRequest.setStatus(request.getStatus());

            final boolean newRequestStatusIsAccepted = request.getStatus().equals(ServiceConstant.STATUS_ACCEPTED);

            if (newRequestStatusIsAccepted) {
                if (assetRepository.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku()).getStock() -
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

                assetRepository.save(asset);
            }
        }
    }

    @Override
    public void updateStatusToDelivered(
            final String username, RequestModel savedRequest, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final String currentRequestStatus = savedRequest.getStatus();

        if (isRequestDeliveryValid(username, currentRequestStatus, newRequestStatus)) {
            savedRequest.setStatus(ServiceConstant.STATUS_DELIVERED);
        }
    }

    @Override
    public void updateAssetDataAndStatusToReturned(
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

            assetRepository.save(asset);

            savedRequest.setStatus(ServiceConstant.STATUS_RETURNED);
        }
    }

    @Override
    public boolean isRequestCancellationValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean statusIsRequested = currentRequestStatus.equals(ServiceConstant.STATUS_REQUESTED);
        final boolean newRequestStatusIsCancelled = newRequestStatus.equals(ServiceConstant.STATUS_CANCELLED);
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

    @Override
    public boolean isRequestAcceptanceOrRejectionValid(
            final String username, final String recordedRequesterUsername, final String currentRequestStatus,
            final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean usernameIsAdminOrSupervisor = isUsernameAdminOrSupervisor(username, recordedRequesterUsername);

        final boolean statusIsRequested = currentRequestStatus.equals(ServiceConstant.STATUS_REQUESTED);
        final boolean newRequestStatusIsAccepted = newRequestStatus.equals(ServiceConstant.STATUS_ACCEPTED);
        final boolean newRequestStatusIsRejected = newRequestStatus.equals(ServiceConstant.STATUS_REJECTED);
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

    @Override
    public boolean isRequestDeliveryValid(
            final String username, final String currentRequestStatus, final String newRequestStatus
    )
            throws
            UnauthorizedOperationException,
            BadRequestException {

        final boolean statusIsAccepted = currentRequestStatus.equals(ServiceConstant.STATUS_ACCEPTED);
        final boolean newRequestStatusIsDelivered = newRequestStatus.equals(ServiceConstant.STATUS_DELIVERED);
        final boolean acceptedToDelivered = statusIsAccepted && newRequestStatusIsDelivered;

        return isAssetDeliveryOrReturnValid(username, acceptedToDelivered);
    }

    @Override
    public boolean isRequestDeliveryOrReturnValid(
            final String username, final RequestModel savedRequest, final String newRequestStatus
    )
            throws
            BadRequestException,
            UnauthorizedOperationException {

        final boolean statusIsAccepted = savedRequest.getStatus().equals(ServiceConstant.STATUS_ACCEPTED);
        final boolean expendableAsset = assetRepository.findByDeletedIsFalseAndSkuEquals(savedRequest.getSku())
                                                       .isExpendable();
        final boolean newRequestStatusIsDelivered = newRequestStatus.equals(ServiceConstant.STATUS_DELIVERED);
        final boolean acceptedToDelivered = statusIsAccepted && newRequestStatusIsDelivered;
        final boolean expendableAssetDelivery = expendableAsset && acceptedToDelivered;

        final boolean statusIsDelivered = savedRequest.getStatus().equals(ServiceConstant.STATUS_DELIVERED);
        final boolean newRequestStatusIsReturned = newRequestStatus.equals(ServiceConstant.STATUS_RETURNED);
        final boolean deliveredToReturned = statusIsDelivered && newRequestStatusIsReturned;
        final boolean nonExpendableAssetReturn = !expendableAsset && deliveredToReturned;

        final boolean acceptedOrDeliveredToReturned = expendableAssetDelivery || nonExpendableAssetReturn;

        return isAssetDeliveryOrReturnValid(username, acceptedOrDeliveredToReturned);
    }

    @Override
    public boolean isAssetDeliveryOrReturnValid(final String username, final boolean acceptedOrDeliveredToReturned)
            throws
            BadRequestException,
            UnauthorizedOperationException {

        if (!acceptedOrDeliveredToReturned) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean administratorWithUsernameExists = adminRepository
                    .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);

            if (!administratorWithUsernameExists) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                return true;
            }
        }
    }

}
