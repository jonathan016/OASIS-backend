package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.DashboardServiceApi;
import com.oasis.web_model.response.success.dashboard.DashboardRequestUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.EMPTY_EMPLOYEE_NIK;
import static com.oasis.exception.helper.ErrorCodeAndMessage.REQUESTS_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.USER_NOT_FOUND;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class DashboardServiceImpl implements DashboardServiceApi {

    /*--------------Universal--------------*/
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

    @Override
    public List<RequestModel> getMyPendingHandoverRequests(final String username) {

        return requestRepository.findAllByUsernameAndStatus(username, ServiceConstant.ACCEPTED);
    }

    @Override
    public List<RequestModel> getMyRequestedRequests(final String username) {

        return requestRepository.findAllByUsernameAndStatus(username, ServiceConstant.REQUESTED);
    }

    @Override
    public List<RequestModel> getOthersRequestedRequests(final String username) {

        List<String> supervisedEmployeeUsernameList = getSupervisedEmployeeUsernameList(username);

        return getRequestsList(ServiceConstant.REQUESTED, supervisedEmployeeUsernameList);
    }

    @Override
    public List<RequestModel> getOthersPendingHandoverRequests(final String username) {

        List<String> supervisedEmployeeUsernameList = getSupervisedEmployeeUsernameList(username);

        return getRequestsList(ServiceConstant.ACCEPTED, supervisedEmployeeUsernameList);
    }

    @Override
    public String determineUserRole(final String username) throws DataNotFoundException {

        return roleDeterminer.determineRole(username);
    }

    @Override
    public List<String> getSupervisedEmployeeUsernameList(final String username) {

        List<SupervisionModel> supervisions =
                supervisionRepository.findAllByDeletedIsFalseAndSupervisorUsername(username);

        List<String> supervisedEmployeeUsernameList = new ArrayList<>();

        for (SupervisionModel supervision : supervisions) {
            supervisedEmployeeUsernameList.add(supervision.getEmployeeUsername());
        }

        return supervisedEmployeeUsernameList;
    }

    @Override
    public List<RequestModel> getRequestsList(
            final String requestStatus, final List<String> supervisedEmployeeUsernameList
    ) {

        List<RequestModel> assignedRequests = new ArrayList<>();

        for (String supervisedEmployeeUsername : supervisedEmployeeUsernameList) {
            assignedRequests.addAll(
                    requestRepository.findAllByUsernameAndStatus(supervisedEmployeeUsername,
                                                            requestStatus
                    ));

            boolean isAdminOrSuperior = supervisionRepository.existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(supervisedEmployeeUsername);

            if (isAdminOrSuperior) {
                if (requestStatus.equals(ServiceConstant.ACCEPTED)) {
                    assignedRequests.addAll(
                            getOthersPendingHandoverRequests(supervisedEmployeeUsername));
                } else if (requestStatus.equals(ServiceConstant.REQUESTED)) {
                    assignedRequests.addAll(getOthersRequestedRequests(supervisedEmployeeUsername));
                }
            }
        }

        return assignedRequests;
    }

    /*--------------Status Section--------------*/
    @Override
    public Map<String, Integer> getStatusSectionData(final String username) throws DataNotFoundException, BadRequestException {

        if (!username.matches("([A-Za-z0-9]+\\.?)*[A-Za-z0-9]+")){
            //TODO throw real exception
            throw new BadRequestException(EMPTY_EMPLOYEE_NIK);
        }

        int availableAssetCount = assetRepository.countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);

        List<RequestModel> requestedRequests = new ArrayList<>();
        List<RequestModel> pendingHandoverRequests = new ArrayList<>();

        switch (roleDeterminer.determineRole(username)) {
            case ServiceConstant.ROLE_ADMINISTRATOR:
                requestedRequests.addAll(getOthersRequestedRequests(username));
                pendingHandoverRequests.addAll(getOthersPendingHandoverRequests(username));
                break;
            case ServiceConstant.ROLE_SUPERIOR:
                requestedRequests.addAll(getOthersRequestedRequests(username));
                break;
            case ServiceConstant.ROLE_EMPLOYEE:
                requestedRequests.addAll(getMyRequestedRequests(username));
                break;
        }
        pendingHandoverRequests.addAll(getMyPendingHandoverRequests(username));

        Map<String, Integer> statusData = new HashMap<>();
        statusData.put("requestedRequestsCount", requestedRequests.size());
        statusData.put("pendingHandoverRequestsCount", pendingHandoverRequests.size());
        statusData.put("availableAssetsCount", availableAssetCount);

        return statusData;
    }

    /*--------------Request Update Section--------------*/
    @Override
    public AssetModel getAssetData(
            final String sku
    ) {

        return assetRepository.findByDeletedIsFalseAndSkuEquals(sku);
    }

    @Override
    public EmployeeModel getEmployeeData(
            final String username
    ) {

        return employeeRepository.findByDeletedIsFalseAndUsername(username);
    }

    @Override
    public SupervisionModel getEmployeeSupervisorData(
            final String username
    ) {

        return supervisionRepository.findByDeletedIsFalseAndEmployeeUsername(username);
    }

    @Override
    public List<RequestModel> fillData(
            final String username, final String tab, final String role
    ) {

        List<RequestModel> requestUpdates = new ArrayList<>();

        switch (role) {
            case ServiceConstant.ROLE_ADMINISTRATOR:
            case ServiceConstant.ROLE_SUPERIOR:
                if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                    requestUpdates.addAll(getOthersRequestedRequests(username));
                } else if (tab.equals(ServiceConstant.TAB_SELF)) {
                    requestUpdates.addAll(getMyRequestedRequests(username));
                    requestUpdates.addAll(getMyPendingHandoverRequests(username));
                }
                break;
            case ServiceConstant.ROLE_EMPLOYEE:
                requestUpdates.addAll(getMyPendingHandoverRequests(username));
                break;
        }

        return requestUpdates;
    }

    @Override
    public void sortData(
            List<RequestModel> requestUpdates, final String sortInfo
    ) {

        if (sortInfo.substring(1)
                .equals("createdDate")) {
            if (sortInfo.substring(0, 1)
                    .equals(ServiceConstant.ASCENDING)) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate));
            } else if (sortInfo.substring(0, 1)
                    .equals(ServiceConstant.DESCENDING)) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate)
                        .reversed());
            }
        } else if (sortInfo.substring(1)
                .equals("updatedDate")) {
            if (sortInfo.substring(0, 1)
                    .equals(ServiceConstant.ASCENDING)) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getUpdatedDate));
            } else if (sortInfo.substring(0, 1)
                    .equals(ServiceConstant.DESCENDING)) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getUpdatedDate)
                        .reversed());
            }
        }
    }

    @Override
    public List<DashboardRequestUpdateResponse.RequestUpdateModel> mapRequests(
            final List<RequestModel> requestUpdates
    ) {

        List<DashboardRequestUpdateResponse.RequestUpdateModel> mappedRequests = new ArrayList<>();

        for (RequestModel requestUpdate : requestUpdates) {
            DashboardRequestUpdateResponse.RequestUpdateModel.Request request =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Request(
                            requestUpdate.get_id(), requestUpdate.getStatus(),
                            requestUpdate.getRequestNote()
                    );
            DashboardRequestUpdateResponse.RequestUpdateModel.Employee employee =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Employee(
                            requestUpdate.getUsername(),
                            getEmployeeData(requestUpdate.getUsername()).getName()
                    );
            DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor supervisor =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor(
                            getEmployeeSupervisorData(
                                    requestUpdate.getUsername()).getSupervisorUsername(),
                            getEmployeeData(getEmployeeSupervisorData(
                                    requestUpdate.getUsername()).getSupervisorUsername()).getName()
                    );
            DashboardRequestUpdateResponse.RequestUpdateModel.Asset asset =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Asset(
                            requestUpdate.getSku(),
                            getAssetData(requestUpdate.getSku()).getName(),
                            requestUpdate.getQuantity()
                    );

            mappedRequests.add(
                    new DashboardRequestUpdateResponse.RequestUpdateModel(request, employee,
                            supervisor, asset
                    ));
        }

        return mappedRequests;
    }

    @Override
    public List<DashboardRequestUpdateResponse.RequestUpdateModel> getRequestUpdateSectionData(
            final String username, final String tab, final int page, final String sort
    )
            throws DataNotFoundException {

        if (getEmployeeData(username) == null) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        }
        String role = determineUserRole(username);

        List<RequestModel> requestUpdates =
                new ArrayList<>(fillData(username, tab, role));

        if (requestUpdates.size() == 0) {
            throw new DataNotFoundException(REQUESTS_NOT_FOUND);
        }

        if ((int) Math.ceil((float) requestUpdates.size() /
                ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) < page) {
            throw new DataNotFoundException(REQUESTS_NOT_FOUND);
        }

        sortData(requestUpdates, sort);

        return mapRequests(requestUpdates);
    }

}