package com.oasis.service.implementation.requests;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.base.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestListServiceApi;
import com.oasis.service.api.requests.RequestMyListServiceApi;
import com.oasis.model.constant.service_constant.PageSizeConstant;
import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.model.constant.service_constant.StatusConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestMyListServiceImpl
        implements RequestMyListServiceApi {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Autowired
    private RequestListServiceApi requestListServiceApi;



    @Override
    public Map< String, List< ? > > getMyRequestsListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > myRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getUsernameRequestsList(username, query, status, page, sort);
        final List< EmployeeModel > employees = requestListServiceApi.getEmployeesDataFromRequest(requests);
        final List< EmployeeModel > modifiers = getRequestModifiersDataFromRequest(requests);
        final List< AssetModel > assets = requestListServiceApi.getAssetDataFromRequest(requests);

        myRequestsListData.put("requests", requests);
        myRequestsListData.put("employees", employees);
        myRequestsListData.put("modifiers", modifiers);
        myRequestsListData.put("assets", assets);

        return myRequestsListData;
    }

    private List< EmployeeModel > getRequestModifiersDataFromRequest(
            final List< RequestModel > requests
    ) {

        List< EmployeeModel > requestModifiers = new ArrayList<>();

        for (final RequestModel request : requests) {
            final String modifierUsername = request.getUpdatedBy();

            requestModifiers.add(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(modifierUsername));
            requestModifiers.get(requestModifiers.size() - 1).setPhoto(
                    requestListServiceApi.getEmployeeDetailPhoto(
                            requestModifiers.get(requestModifiers.size() - 1).getUsername(),
                            requestModifiers.get(requestModifiers.size() - 1).getPhoto()
                    ));
        }

        return requestModifiers;
    }

    @SuppressWarnings("ConstantConditions")
    private List< RequestModel > getUsernameRequestsList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        final boolean emptyQueryGiven = ( query != null && query.isEmpty() );
        final boolean emptySortGiven = ( sort != null && sort.isEmpty() );
        final boolean emptyStatusGiven = ( status != null && status.isEmpty() );

        if (emptyQueryGiven || emptySortGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            sort = requestListServiceApi.validateSortInformationGiven(sort);

            final boolean viewAllRequestsRegardlessOfStatus = ( status == null );

            List< RequestModel > requests = new ArrayList<>();

            if (viewAllRequestsRegardlessOfStatus) {
                final long requestsCount = requestRepository.countAllByUsernameEquals(username);
                final boolean noRequests = ( requestsCount == 0 );
                final long totalPages = (long) Math
                        .ceil((double) requestListServiceApi
                                .getRequestsCount("Username", username, query, status, page, sort) /
                              PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
                final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > totalPages ) );

                if (noRequests || pageIndexOutOfBounds) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    final boolean viewAllRequests = ( query == null );

                    final int zeroBasedIndexPage = page - 1;
                    final Pageable pageable = PageRequest
                            .of(zeroBasedIndexPage, PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

                    if (viewAllRequests) {
                        if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsOrderByStatusAsc(username, pageable)
                                                         .getContent());
                            } else {
                                requests.addAll(
                                        requestRepository
                                                .findAllByUsernameEqualsOrderByUpdatedDateAsc(username, pageable)
                                                .getContent());

                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsOrderByStatusDesc(username, pageable)
                                                         .getContent());
                            } else {
                                requests.addAll(
                                        requestRepository
                                                .findAllByUsernameEqualsOrderByUpdatedDateDesc(username, pageable)
                                                .getContent());

                            }
                        }
                    } else {
                        List< AssetModel > assets = assetUtilServiceApi
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
                    final long requestsCount = requestRepository.countAllByUsernameEqualsAndStatusEquals(
                            username, status);
                    final boolean noRequests = ( requestsCount == 0 );
                    final long totalPages = (long) Math
                            .ceil((double) requestListServiceApi
                                    .getRequestsCount("Username", username, query, status, page, sort) /
                                  PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
                    final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > totalPages ) );

                    if (noRequests || pageIndexOutOfBounds) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        final boolean viewAllRequests = ( query == null );

                        final int zeroBasedIndexPage = page - 1;
                        final Pageable pageable = PageRequest
                                .of(zeroBasedIndexPage, PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

                        if (viewAllRequests) {
                            if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc(
                                                                    username,
                                                                    status,
                                                                    pageable
                                                            ).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc(
                                                                    username,
                                                                    status,
                                                                    pageable
                                                            ).getContent());

                                }
                            } else {
                                if (sort.substring(2).equals("status")) {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(
                                                                    username,
                                                                    status,
                                                                    pageable
                                                            ).getContent());
                                } else {
                                    requests.addAll(requestRepository
                                                            .findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(
                                                                    username,
                                                                    status,
                                                                    pageable
                                                            ).getContent());

                                }
                            }
                        } else {
                            List< AssetModel > assets = assetUtilServiceApi
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

}
