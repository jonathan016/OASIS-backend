package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.*;
import com.oasis.service.ImageHelper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.DashboardServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardServiceImpl
        implements DashboardServiceApi {

    /*--------------Universal--------------*/
    @Autowired
    private ImageHelper imageHelper;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;

    /*--------------Status Section--------------*/
    @Override
    public Map< String, Long > getStatusSectionData(final String username)
            throws
            DataNotFoundException,
            BadRequestException {

        if (!username.matches("([a-z0-9]+\\.?)*[a-z0-9]+")) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            long requestedRequestsCount = 0;
            long acceptedRequestsCount = 0;
            final long availableAssetCount =
                    assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);

            switch (roleDeterminer.determineRole(username)) {
                case ServiceConstant.ROLE_ADMINISTRATOR:
                    requestedRequestsCount += getRequestsCount(
                            "Others", username, null, ServiceConstant.STATUS_REQUESTED, 1, null);
                    acceptedRequestsCount += getRequestsCount(
                            "Others", username, null, ServiceConstant.STATUS_ACCEPTED, 1, null);
                    break;
                case ServiceConstant.ROLE_SUPERIOR:
                    requestedRequestsCount += getRequestsCount(
                            "Others", username, null, ServiceConstant.STATUS_REQUESTED, 1, null);
                    acceptedRequestsCount += getRequestsCount(
                            "Username", username, null, ServiceConstant.STATUS_ACCEPTED, 1, null);
                    break;
                case ServiceConstant.ROLE_EMPLOYEE:
                    requestedRequestsCount += getRequestsCount(
                            "Username", username, null, ServiceConstant.STATUS_REQUESTED, 1, null);
                    acceptedRequestsCount += getRequestsCount(
                            "Username", username, null, ServiceConstant.STATUS_ACCEPTED, 1, null);
                    break;
            }

            Map< String, Long > statuses = new HashMap<>();
            statuses.put("requestedRequestsCount", requestedRequestsCount);
            statuses.put("acceptedRequestsCount", acceptedRequestsCount);
            statuses.put("availableAssetsCount", availableAssetCount);

            return statuses;
        }
    }

    @Override
    public Map< String, List< ? > > getRequestUpdateSectionData(
            final String username, final String tab, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        if (!username.matches("([a-z0-9]+\\.?)*[a-z0-9]+")) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (!tab.equals(ServiceConstant.TAB_OTHERS) && !tab.equals(ServiceConstant.TAB_MY)) {
                throw new BadRequestException(INCORRECT_PARAMETER);
            } else {
                if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                    return getOthersRequestListData(username, null, ServiceConstant.STATUS_REQUESTED, page, null);
                } else {
                    return getMyRequestsListData(username, null, ServiceConstant.STATUS_REQUESTED, page, null);
                }
            }
        }
    }

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

            Set< RequestModel > requests = new LinkedHashSet<>();

            if (viewAllRequestsRegardlessOfStatus) {
                final long requestsCount = requestRepository.countAllByUsername(username);
                final boolean noRequests = (requestsCount == 0);
                final long totalPages = (long) Math
                        .ceil((double) getRequestsCount("Username", username, query, status, page, sort) /
                              ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
                final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                if (noRequests || pageIndexOutOfBounds) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    final boolean viewAllRequests = (query == null);

                    final int zeroBasedIndexPage = page - 1;
                    final Pageable pageable = PageRequest
                            .of(zeroBasedIndexPage, ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

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
                                .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                        query, query, query, query, query);
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
                                  ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
                    final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                    if (noRequests || pageIndexOutOfBounds) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        final boolean viewAllRequests = (query == null);

                        final int zeroBasedIndexPage = page - 1;
                        final Pageable pageable = PageRequest
                                .of(zeroBasedIndexPage, ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

                        if (viewAllRequests) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameAndStatusContainsOrderByUpdatedDateAsc(
                                                                    username, status, pageable).getContent());
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
                                                            .findAllByUsernameAndStatusContainsOrderByUpdatedDateDesc(
                                                                    username, status, pageable).getContent());
                                } else {
                                    return requestRepository
                                            .findAllByUsernameAndStatusOrderByUpdatedDateDesc(username, status,
                                                                                              pageable
                                            ).getContent();

                                }
                            }
                        } else {
                            List< AssetModel > assets = assetRepository
                                    .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                            query, query, query, query, query);
                            for (final AssetModel asset : assets) {
                                if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                    if (sort.substring(2).equals("status")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                        username, query, asset.getSku(), pageable)
                                                                .getContent());
                                    } else {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                        username, query, asset.getSku(), pageable)
                                                                .getContent());
                                    }
                                } else {
                                    if (sort.substring(2).equals("status")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                        username, query, username, asset.getSku(),
                                                                        pageable
                                                                ).getContent());
                                    } else {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                        username, query, username, asset.getSku(),
                                                                        pageable
                                                                ).getContent());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return new ArrayList<>(requests);
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

            Set< RequestModel > requests = new LinkedHashSet<>();

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
                                .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                        query, query, query, query, query);
                        for (final AssetModel asset : assets) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                } else {
                                    if (sort.substring(2).equals("updatedDate")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                        supervisedEmployeeUsername, asset.getSku()));
                                    }
                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                    supervisedEmployeeUsername, asset.getSku()));
                                } else {
                                    if (sort.substring(2).equals("updatedDate")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                        supervisedEmployeeUsername, asset.getSku()));
                                    }
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
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateAsc(
                                            supervisedEmployeeUsername, status));
                                }
                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameAndStatusContainsOrderByStatusDesc(
                                        supervisedEmployeeUsername, status));
                            } else {
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameAndStatusOrderByUpdatedDateDesc(
                                            supervisedEmployeeUsername, status));
                                }
                            }
                        }
                    } else {
                        List< AssetModel > assets = assetRepository
                                .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                        query, query, query, query, query);
                        for (final AssetModel asset : assets) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByStatusAsc(
                                                                    supervisedEmployeeUsername, query, asset.getSku()));
                                } else {
                                    if (sort.substring(2).equals("updatedDate")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCaseOrderByUpdatedDateAsc(
                                                                        supervisedEmployeeUsername, query,
                                                                        asset.getSku()
                                                                ));
                                    }
                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByStatusDesc(
                                                                    supervisedEmployeeUsername, query,
                                                                    supervisedEmployeeUsername, asset.getSku()
                                                            ));
                                } else {
                                    if (sort.substring(2).equals("updatedDate")) {
                                        requests.addAll(requestRepository
                                                                .findAllByUsernameEqualsAndStatusEqualsOrUsernameEqualsAndSkuContainsIgnoreCaseOrderByUpdatedDateDesc(
                                                                        supervisedEmployeeUsername, query,
                                                                        supervisedEmployeeUsername, asset.getSku()
                                                                ));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return new ArrayList<>(requests);
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
        final long totalPages = (long) Math
                .ceil((double) requests.size() / ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
        final boolean noRequests = requests.isEmpty();
        final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        PagedListHolder< RequestModel > pagedListHolder = new PagedListHolder<>(new ArrayList<>(requests));
        pagedListHolder.setPage(page - 1);
        pagedListHolder.setPageSize(ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

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
                                .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                        query, query, query, query, query);

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
                                .findAllByDeletedIsFalseAndSkuContainsOrDeletedIsFalseAndNameContainsIgnoreCaseOrDeletedIsFalseAndBrandContainsIgnoreCaseOrDeletedIsFalseAndTypeContainsIgnoreCaseOrDeletedIsFalseAndLocationContainsIgnoreCase(
                                        query, query, query, query, query);

                        for (final AssetModel asset : assets) {
                            requestCount += requestRepository
                                    .countAllByUsernameEqualsAndStatusEqualsOrSkuContainsIgnoreCase(username, status,
                                                                                                    asset.getSku()
                                    );
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

}