package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.service.ImageHelper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.AssetsServiceApi;
import com.oasis.service.api.DashboardServiceApi;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.service.api.RequestsServiceApi;
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
    private RoleDeterminer roleDeterminer;
    @Autowired
    private AssetsServiceApi assetsServiceApi;
    @Autowired
    private EmployeesServiceApi employeesServiceApi;
    @Autowired
    private RequestsServiceApi requestsServiceApi;

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
            final long availableAssetCount = assetsServiceApi
                    .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);

            switch (roleDeterminer.determineRole(username)) {
                case ServiceConstant.ROLE_ADMINISTRATOR:
                    requestedRequestsCount += getRequestsCount("Others", username, ServiceConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += getRequestsCount("Others", username, ServiceConstant.STATUS_ACCEPTED, 1);
                    break;
                case ServiceConstant.ROLE_SUPERIOR:
                    requestedRequestsCount += getRequestsCount("Others", username, ServiceConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += getRequestsCount("Username", username, ServiceConstant.STATUS_ACCEPTED, 1);
                    break;
                case ServiceConstant.ROLE_EMPLOYEE:
                    requestedRequestsCount += getRequestsCount(
                            "Username", username, ServiceConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += getRequestsCount("Username", username, ServiceConstant.STATUS_ACCEPTED, 1);
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
                    return getOthersRequestListData(username, ServiceConstant.STATUS_REQUESTED, page);
                } else {
                    return getMyRequestsListData(username, ServiceConstant.STATUS_REQUESTED, page);
                }
            }
        }
    }

    @Override
    public Map< String, List< ? > > getMyRequestsListData(
            final String username, final String status, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > myRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getUsernameRequestsList(username, status, page);
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
            final String username, final String status, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > othersRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getOthersRequestListPaged(username, status, page);
        final List< EmployeeModel > employees = getEmployeesDataFromRequest(requests);
        final List< AssetModel > assets = getAssetDataFromRequest(requests);

        othersRequestsListData.put("requests", requests);
        othersRequestsListData.put("employees", employees);
        othersRequestsListData.put("assets", assets);

        return othersRequestsListData;
    }

    @Override
    public long getRequestsCount(
            final String type, final String username, final String status, final int page
    )
            throws
            BadRequestException {

        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            if (type.equals("Username")) {
                final boolean viewAllRequestsRegardlessOfStatus = (status == null);

                if (viewAllRequestsRegardlessOfStatus) {
                    return requestsServiceApi.countAllByUsername(username);
                } else {
                    return requestsServiceApi.countAllByUsernameAndStatus(username, status);
                }
            } else {
                if (type.equals("Others")) {
                    return getOthersRequestList(username, status).size();
                }
            }

            return -1;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private List< RequestModel > getUsernameRequestsList(
            final String username, final String status, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean viewAllRequestsRegardlessOfStatus = (status == null);

            List< RequestModel > requests = new ArrayList<>();

            if (viewAllRequestsRegardlessOfStatus) {
                final long requestsCount = requestsServiceApi.countAllByUsername(username);
                final boolean noRequests = (requestsCount == 0);
                final long totalPages = (long) Math.ceil((double) getRequestsCount("Username", username, status, page) /
                                                         ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
                final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                if (noRequests || pageIndexOutOfBounds) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    final int zeroBasedIndexPage = page - 1;
                    final Pageable pageable = PageRequest
                            .of(zeroBasedIndexPage, ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);

                    requests.addAll(
                            requestsServiceApi.findAllByUsernameOrderByUpdatedDateDesc(username, pageable).getContent());
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
                    final long requestsCount = requestsServiceApi.countAllByUsernameAndStatus(username, status);
                    final boolean noRequests = (requestsCount == 0);
                    final long totalPages = (long) Math
                            .ceil((double) getRequestsCount("Username", username, status, page) /
                                  ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
                    final boolean pageIndexOutOfBounds = ((page < 1) || (page > totalPages));

                    if (noRequests || pageIndexOutOfBounds) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        final int zeroBasedIndexPage = page - 1;
                        final Pageable pageable = PageRequest
                                .of(zeroBasedIndexPage, ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE);
                        requests.addAll(requestsServiceApi
                                                .findAllByUsernameAndStatusOrderByUpdatedDateDesc(username, status,
                                                                                                  pageable
                                                ).getContent());
                    }
                }
            }

            return requests;
        }
    }

    private List< RequestModel > getOthersRequestList(
            final String username, final String status
    )
            throws
            BadRequestException {

        final boolean emptyStatusGiven = (status != null && status.isEmpty());

        if (emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
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
                boolean usernameIsAdminOrSupervisor = (administratorWithUsernameExists || supervisorIsValid);

                if (usernameIsAdminOrSupervisor) {
                    requests.addAll(getOthersRequestList(supervisedEmployeeUsername, status));
                }

                final boolean viewAllRequestsRegardlessOfStatus = (status == null);

                if (viewAllRequestsRegardlessOfStatus) {
                    requests.addAll(
                            requestsServiceApi.findAllByUsernameOrderByUpdatedDateDesc(supervisedEmployeeUsername));
                } else {
                    requests.addAll(requestsServiceApi.findAllByUsernameAndStatusOrderByUpdatedDateDesc(
                            supervisedEmployeeUsername, status));
                }
            }

            requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());

            return requests;
        }
    }

    private List< RequestModel > getOthersRequestListPaged(
            final String username, final String status, final int page
    )
            throws
            DataNotFoundException,
            BadRequestException {

        final List< RequestModel > requests = getOthersRequestList(username, status);
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

}