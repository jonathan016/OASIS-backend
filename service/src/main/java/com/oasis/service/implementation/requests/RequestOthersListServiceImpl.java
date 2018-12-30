package com.oasis.service.implementation.requests;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.RequestRepository;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestListServiceApi;
import com.oasis.service.api.requests.RequestOthersListServiceApi;
import com.oasis.tool.constant.PageSizeConstant;
import com.oasis.tool.constant.ServiceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestOthersListServiceImpl
        implements RequestOthersListServiceApi {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Autowired
    private RequestListServiceApi requestListServiceApi;



    @Override
    public Map< String, List< ? > > getOthersRequestListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, List< ? > > othersRequestsListData = new HashMap<>();

        final List< RequestModel > requests = getOthersRequestListPaged(username, query, status, page, sort);
        final List< EmployeeModel > employees = requestListServiceApi.getEmployeesDataFromRequest(requests);
        final List< AssetModel > assets = requestListServiceApi.getAssetDataFromRequest(requests);

        othersRequestsListData.put("requests", requests);
        othersRequestsListData.put("employees", employees);
        othersRequestsListData.put("assets", assets);

        return othersRequestsListData;
    }

    @Override
    public List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, String sort
    )
            throws
            BadRequestException {

        final boolean emptyQueryGiven = ( query != null && query.isEmpty() );
        final boolean emptySortGiven = ( sort != null && sort.isEmpty() );
        final boolean emptyStatusGiven = ( status != null && status.isEmpty() );

        if (emptyQueryGiven || emptySortGiven || emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            sort = requestListServiceApi.validateSortInformationGiven(sort);

            List< SupervisionModel > supervisions = employeeUtilServiceApi
                    .findAllByDeletedIsFalseAndSupervisorUsername(username);

            List< String > supervisedEmployeesUsernames = new ArrayList<>();

            for (final SupervisionModel supervision : supervisions) {
                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
            }

            List< RequestModel > requests = new ArrayList<>();

            for (final String supervisedEmployeeUsername : supervisedEmployeesUsernames) {
                boolean administratorWithUsernameExists = employeeUtilServiceApi
                        .existsAdminModelByDeletedIsFalseAndUsernameEquals(supervisedEmployeeUsername);
                boolean supervisorIsValid = employeeUtilServiceApi
                        .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(supervisedEmployeeUsername);
                boolean usernameIsAdminOrSupervisor = administratorWithUsernameExists || supervisorIsValid;

                if (usernameIsAdminOrSupervisor) {
                    requests.addAll(getOthersRequestList(supervisedEmployeeUsername, query, status, sort));
                }

                final boolean viewAllRequestsRegardlessOfStatus = ( status == null );
                final boolean viewAllRequests = ( query == null );

                if (viewAllRequestsRegardlessOfStatus) {
                    if (viewAllRequests) {
                        if (sort.substring(0, 1).equals(ServiceConstant.ASCENDING)) {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository
                                                        .findAllByUsernameEqualsOrderByStatusAsc(
                                                                supervisedEmployeeUsername));
                            } else {
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameEqualsOrderByUpdatedDateAsc(
                                            supervisedEmployeeUsername));
                                }
                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(requestRepository.findAllByUsernameEqualsOrderByStatusDesc(
                                        supervisedEmployeeUsername));
                            } else {
                                if (sort.substring(2).equals("updatedDate")) {
                                    requests.addAll(requestRepository.findAllByUsernameEqualsOrderByUpdatedDateDesc(
                                            supervisedEmployeeUsername));
                                }
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
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsAndStatusContainsOrderByStatusAsc(
                                                supervisedEmployeeUsername, status));
                            } else {
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateAsc(
                                                supervisedEmployeeUsername, status));
                            }
                        } else {
                            if (sort.substring(2).equals("status")) {
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsAndStatusContainsOrderByStatusDesc(
                                                supervisedEmployeeUsername, status));
                            } else {
                                requests.addAll(
                                        requestRepository.findAllByUsernameEqualsAndStatusEqualsOrderByUpdatedDateDesc(
                                                supervisedEmployeeUsername, status));
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

            if (sort.equals("D-updatedDate")) {
                requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());
            } else if (sort.equals("A-updatedDate")) {
                requests.sort(Comparator.comparing(BaseEntity::getUpdatedDate));
            }

            return requests;
        }
    }

    private List< RequestModel > getOthersRequestListPaged(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException {

        final List< RequestModel > requests = getOthersRequestList(username, query, status, sort);
        final long totalPages = (long) Math.ceil((double) requests.size() / PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);
        final boolean noRequests = requests.isEmpty();
        final boolean pageIndexOutOfBounds = ( ( page < 1 ) || ( page > totalPages ) );

        if (noRequests || pageIndexOutOfBounds) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        PagedListHolder< RequestModel > pagedListHolder = new PagedListHolder<>(new ArrayList<>(requests));
        pagedListHolder.setPage(page - 1);
        pagedListHolder.setPageSize(PageSizeConstant.REQUESTS_LIST_PAGE_SIZE);

        return new ArrayList<>(pagedListHolder.getPageList());
    }

}
