package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
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
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_EMPLOYEE_NIK;
import static com.oasis.exception.helper.ErrorCodeAndMessage.REQUESTS_NOT_FOUND;

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
    public List<RequestModel> getMyPendingHandoverRequests(final String employeeNik) {
        return requestRepository.findAllByEmployeeNikAndStatus(employeeNik,
                ServiceConstant.PENDING_HANDOVER
        );
    }

    @Override
    public List<RequestModel> getMyRequestedRequests(final String employeeNik) {
        return requestRepository.findAllByEmployeeNikAndStatus(employeeNik,
                ServiceConstant.REQUESTED
        );
    }

    @Override
    public List<RequestModel> getOthersRequestedRequests(final String supervisorNik) {
        List<String> supervisedEmployeeNikList = getSupervisedEmployeeNikList(supervisorNik);

        return getRequestsList(ServiceConstant.REQUESTED, supervisedEmployeeNikList);
    }

    @Override
    public List<RequestModel> getOthersPendingHandoverRequests(final String supervisorNik) {
        List<String> supervisedEmployeeNikList = getSupervisedEmployeeNikList(supervisorNik);

        return getRequestsList(ServiceConstant.PENDING_HANDOVER, supervisedEmployeeNikList);
    }

    @Override
    public String determineUserRole(final String employeeNik) throws DataNotFoundException {
        return roleDeterminer.determineRole(employeeNik);
    }

    @Override
    public List<String> getSupervisedEmployeeNikList(final String supervisorNik) {
        List<SupervisionModel> supervisions =
                supervisionRepository.findAllBySupervisorNik(supervisorNik);

        List<String> supervisedEmployeeNikList = new ArrayList<>();

        for (SupervisionModel supervision : supervisions) {
            supervisedEmployeeNikList.add(supervision.getEmployeeNik());
        }

        return supervisedEmployeeNikList;
    }

    @Override
    public List<RequestModel> getRequestsList(
            final String requestStatus, final List<String> supervisedEmployeeNikList
    ) {
        List<RequestModel> assignedRequests = new ArrayList<>();

        for (String supervisedEmployeeNik : supervisedEmployeeNikList) {
            assignedRequests.addAll(
                    requestRepository.findAllByEmployeeNikAndStatus(supervisedEmployeeNik,
                            requestStatus
                    ));

            int employeeSupervisingCount = employeeRepository.findByNik(supervisedEmployeeNik)
                    .getSupervisingCount();
            boolean isAdminOrSuperior = employeeSupervisingCount > 0;

            if (isAdminOrSuperior) {
                if (requestStatus.equals(ServiceConstant.PENDING_HANDOVER)) {
                    assignedRequests.addAll(
                            getOthersPendingHandoverRequests(supervisedEmployeeNik));
                } else if (requestStatus.equals(ServiceConstant.REQUESTED)) {
                    assignedRequests.addAll(getOthersRequestedRequests(supervisedEmployeeNik));
                }
            }
        }

        return assignedRequests;
    }

    /*--------------Status Section--------------*/
    @Override
    public Map<String, Integer> getStatusSectionData(final String employeeNik) throws DataNotFoundException {
        int availableAssetCount = assetRepository.findAllByStockGreaterThan(ServiceConstant.ZERO)
                .size();

        List<RequestModel> requestedRequests = new ArrayList<>();
        List<RequestModel> pendingHandoverRequests = new ArrayList<>();

        switch (roleDeterminer.determineRole(employeeNik)) {
            case ServiceConstant.ROLE_ADMINISTRATOR:
                requestedRequests.addAll(getOthersRequestedRequests(employeeNik));
                pendingHandoverRequests.addAll(getOthersPendingHandoverRequests(employeeNik));
                break;
            case ServiceConstant.ROLE_SUPERIOR:
                requestedRequests.addAll(getOthersRequestedRequests(employeeNik));
                break;
            case ServiceConstant.ROLE_EMPLOYEE:
                requestedRequests.addAll(getMyRequestedRequests(employeeNik));
                break;
        }
        pendingHandoverRequests.addAll(getMyPendingHandoverRequests(employeeNik));

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
        return assetRepository.findBySku(sku);
    }

    @Override
    public EmployeeModel getEmployeeData(
            final String employeeNik
    ) {
        return employeeRepository.findByNik(employeeNik);
    }

    @Override
    public SupervisionModel getEmployeeSupervisorData(
            final String employeeNik
    ) {
        return supervisionRepository.findByEmployeeNik(employeeNik);
    }

    @Override
    public List<RequestModel> fillData(
            final String employeeNik, final String currentTab, final String role
    ) {
        List<RequestModel> requestUpdates = new ArrayList<>();

        switch (role) {
            case ServiceConstant.ROLE_ADMINISTRATOR:
            case ServiceConstant.ROLE_SUPERIOR:
                if (currentTab.equals(ServiceConstant.TAB_OTHERS)) {
                    requestUpdates.addAll(getOthersRequestedRequests(employeeNik));
                } else if (currentTab.equals(ServiceConstant.TAB_SELF)) {
                    requestUpdates.addAll(getMyRequestedRequests(employeeNik));
                    requestUpdates.addAll(getMyPendingHandoverRequests(employeeNik));
                }
                break;
            case ServiceConstant.ROLE_EMPLOYEE:
                requestUpdates.addAll(getMyPendingHandoverRequests(employeeNik));
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
                    .equals("A")) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate));
            } else if (sortInfo.substring(0, 1)
                    .equals("D")) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate)
                        .reversed());
            }
        } else if (sortInfo.substring(1)
                .equals("updatedDate")) {
            if (sortInfo.substring(0, 1)
                    .equals("A")) {
                requestUpdates.sort(Comparator.comparing(BaseEntity::getUpdatedDate));
            } else if (sortInfo.substring(0, 1)
                    .equals("D")) {
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
                            requestUpdate.getNik(),
                            getEmployeeData(requestUpdate.getNik()).getName()
                    );
            DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor supervisor =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor(
                            getEmployeeSupervisorData(
                                    requestUpdate.getNik()).getSupervisorNik(),
                            getEmployeeData(getEmployeeSupervisorData(
                                    requestUpdate.getNik()).getSupervisorNik()).getName()
                    );
            DashboardRequestUpdateResponse.RequestUpdateModel.Asset asset =
                    new DashboardRequestUpdateResponse.RequestUpdateModel.Asset(
                            requestUpdate.getSku(),
                            getAssetData(requestUpdate.getSku()).getName(),
                            requestUpdate.getAssetQuantity()
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
            final String employeeNik, final String currentTab, final int pageNumber, final String sortInfo
    )
            throws DataNotFoundException {
        if (getEmployeeData(employeeNik) == null) {
            throw new DataNotFoundException(INCORRECT_EMPLOYEE_NIK);
        }
        String role = determineUserRole(employeeNik);

        List<RequestModel> requestUpdates =
                new ArrayList<>(fillData(employeeNik, currentTab, role));

        if (requestUpdates.size() == 0) {
            throw new DataNotFoundException(REQUESTS_NOT_FOUND);
        }

        if ((int) Math.ceil((float) requestUpdates.size() /
                ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) < pageNumber) {
            throw new DataNotFoundException(REQUESTS_NOT_FOUND);
        }

        sortData(requestUpdates, sortInfo);

        return mapRequests(requestUpdates);
    }
}