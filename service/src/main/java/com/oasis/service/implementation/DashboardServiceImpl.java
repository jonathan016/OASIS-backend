package com.oasis.service.implementation;

import com.oasis.model.BaseEntity;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AssetRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.RequestRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.RoleDeterminer;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.DashboardServiceApi;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import com.oasis.webmodel.response.success.DashboardStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.oasis.service.ErrorCodeAndMessage.INCORRECT_EMPLOYEE_ID;
import static com.oasis.service.ErrorCodeAndMessage.REQUESTS_NOT_FOUND;

@Service
public class DashboardServiceImpl implements DashboardServiceApi {
    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private SupervisionRepository supervisionRepository;

    @Override
    public List<RequestModel> getMyPendingHandoverRequests(String employeeId) {
        return requestRepository.findAllByEmployeeIdAndStatus(employeeId, ServiceConstant.PENDING_HANDOVER);
    }

    @Override
    public List<RequestModel> getMyRequestedRequests(String employeeId) {
        return requestRepository.findAllByEmployeeIdAndStatus(employeeId, ServiceConstant.REQUESTED);
    }

    @Override
    public List<RequestModel> getMyAssignedRequestedRequests(String supervisorId) {
        List<String> supervisedEmployeeIdList = getSupervisedEmployeeIdList(supervisorId);

        return getRequestsList(ServiceConstant.REQUESTED, supervisedEmployeeIdList);
    }

    @Override
    public List<RequestModel> getMyAssignedPendingHandoverRequests(String supervisorId) {
        List<String> supervisedEmployeeIdList = getSupervisedEmployeeIdList(supervisorId);

        return getRequestsList(ServiceConstant.PENDING_HANDOVER, supervisedEmployeeIdList);
    }

    @Override
    public AssetModel getAssetData(String assetId) {
        return assetRepository.findBy_id(assetId);
    }

    @Override
    public EmployeeModel getEmployeeData(String employeeId) {
        return employeeRepository.findBy_id(employeeId);
    }

    @Override
    public SupervisionModel getEmployeeSupervisorData(String employeeId) {
        return supervisionRepository.findByEmployeeId(employeeId);
    }

    @Override
    public String determineUserRole(String employeeId) {
        RoleDeterminer roleDeterminer = new RoleDeterminer();
        return roleDeterminer.determineRole(employeeRepository, supervisionRepository, employeeId);
    }

    @Override
    public NoPagingResponse<?> getStatusSectionData(String employeeId) {
        int availableAssetCount = assetRepository.findAllByStockGreaterThan(ServiceConstant.ZERO).size();
        List<RequestModel> requestedRequests = new ArrayList<>();
        List<RequestModel> pendingHandoverRequests = new ArrayList<>();

        RoleDeterminer roleDeterminer = new RoleDeterminer();
        String role = roleDeterminer.determineRole(employeeRepository, supervisionRepository, employeeId);

        switch (role) {
            case "ADMINISTRATOR":
                requestedRequests.addAll(
                        getMyAssignedRequestedRequests(
                                employeeId
                        )
                );
                pendingHandoverRequests.addAll(
                        getMyAssignedPendingHandoverRequests(
                                employeeId
                        )
                );
                break;
            case "SUPERIOR":
                requestedRequests.addAll(
                        getMyAssignedRequestedRequests(
                                employeeId
                        )
                );
                break;
            case "EMPLOYEE":
                requestedRequests.addAll(
                        getMyRequestedRequests(
                                employeeId
                        )
                );
                break;
        }
        pendingHandoverRequests.addAll(
                getMyPendingHandoverRequests(
                        employeeId
                )
        );

        return produceDashboardStatusSuccessResult(
                requestedRequests.size(),
                pendingHandoverRequests.size(),
                availableAssetCount
        );
    }

    @Override
    public PagingResponse<?> getRequestUpdateSectionData(String employeeId, String currentTab, int pageNumber, String sortInfo) {
        List<RequestModel> requestUpdates = new ArrayList<>();

        if(getEmployeeData(employeeId) == null){
            return produceDashboardRequestUpdateFailedResult(
                    INCORRECT_EMPLOYEE_ID.getErrorCode(),
                    INCORRECT_EMPLOYEE_ID.getErrorMessage()
            );
        }
        String role = determineUserRole(employeeId);

        fillData(requestUpdates, employeeId, currentTab, role);

        if (requestUpdates.size() == 0){
            return produceDashboardRequestUpdateFailedResult(
                    REQUESTS_NOT_FOUND.getErrorCode(),
                    REQUESTS_NOT_FOUND.getErrorMessage()
            );
        }

        if ((int) Math.ceil(
                (float) requestUpdates.size() / ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) < pageNumber){
            return produceDashboardRequestUpdateFailedResult(
                    REQUESTS_NOT_FOUND.getErrorCode(),
                    REQUESTS_NOT_FOUND.getErrorMessage()
            );
        }

        sortData(requestUpdates, sortInfo);
        List<DashboardRequestUpdateResponse.RequestUpdateModel> mappedRequests = mapRequests(requestUpdates);

        return produceDashboardRequestUpdateSuccessResult(
                mappedRequests,
                pageNumber
        );
    }

    @Override
    public List<String> getSupervisedEmployeeIdList(String supervisorId) {
        List<SupervisionModel> supervisions = supervisionRepository.findAllBySupervisorId(supervisorId);

        List<String> supervisedEmployeeIdList = new ArrayList<>();
        for (SupervisionModel supervision : supervisions) {
            supervisedEmployeeIdList.add(supervision.getEmployeeId());
        }

        return supervisedEmployeeIdList;
    }

    @Override
    public List<RequestModel> getRequestsList(String requestStatus, List<String> supervisedEmployeeIdList) {
        List<RequestModel> assignedRequests = new ArrayList<>();
        for (String supervisedEmployeeId : supervisedEmployeeIdList) {
            EmployeeModel employee = employeeRepository.findBy_id(supervisedEmployeeId);
            if (employee.getSupervisingCount() > 0) {
                if (requestStatus.equals(ServiceConstant.PENDING_HANDOVER)) {
                    assignedRequests.addAll(getMyAssignedPendingHandoverRequests(supervisedEmployeeId));
                } else {
                    assignedRequests.addAll(getMyAssignedRequestedRequests(supervisedEmployeeId));
                }
            } else {
                assignedRequests.addAll(
                        requestRepository.findAllByEmployeeIdAndStatus(
                                supervisedEmployeeId,
                                requestStatus
                        )
                );
            }
        }

        return assignedRequests;
    }

    private NoPagingResponse<DashboardStatusResponse>
    produceDashboardStatusSuccessResult(int requestedRequestsCount,
                                        int pendingHandoverRequestsCount,
                                        int availableAssetCount) {
        NoPagingResponse<DashboardStatusResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new DashboardStatusResponse(
                        requestedRequestsCount,
                        pendingHandoverRequestsCount,
                        availableAssetCount
                )
        );

        return successResponse;
    }

    private PagingResponse<DashboardRequestUpdateResponse>
    produceDashboardRequestUpdateSuccessResult(List<DashboardRequestUpdateResponse.RequestUpdateModel> requests,
                                               int pageNumber) {
        PagingResponse<DashboardRequestUpdateResponse> successResponse = new PagingResponse<>();
        int startIndex = ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE * (pageNumber - 1);
        int endIndex = startIndex + ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE;

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        boolean indexBelowRecordSize = endIndex < requests.size();
        if (indexBelowRecordSize) {
            successResponse.setValue(
                    new DashboardRequestUpdateResponse(
                            requests.subList(
                                    startIndex,
                                    endIndex
                            )
                    )
            );
        } else {
            successResponse.setValue(
                    new DashboardRequestUpdateResponse(
                            requests.subList(
                                    startIndex,
                                    requests.size()
                            )
                    )
            );
        }
        successResponse.setPaging(
                new Paging(
                        pageNumber,
                        ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE,
                        requests.size()
                )
        );

        return successResponse;
    }

    private PagingResponse<FailedResponse>
    produceDashboardRequestUpdateFailedResult(String errorCode, String errorMessage) {
        PagingResponse<FailedResponse> failedResponse = new PagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );
        failedResponse.setPaging(null);

        return failedResponse;
    }

    private void fillData(List<RequestModel> requestUpdates, String employeeId, String currentTab, String role) {
        switch (role) {
            case "ADMINISTRATOR":
            case "SUPERIOR":
                if (currentTab.equals("Others")) {
                    //Should throw exception when no requested request exists
                    requestUpdates.addAll(
                            getMyAssignedRequestedRequests(
                                    employeeId
                            )
                    );
                } else if (currentTab.equals("Self")) {
                    //Should throw exception when no requested request exists
                    requestUpdates.addAll(
                            getMyRequestedRequests(
                                    employeeId
                            )
                    );
                }
                break;
            case "EMPLOYEE":
                //Should throw exception when no requested request exists
                requestUpdates.addAll(
                        getMyPendingHandoverRequests(
                                employeeId
                        )
                );
                break;
        }
    }

    private void sortData(List<RequestModel> requestUpdates, String sortInfo) {
        if (sortInfo.substring(1).equals("createdDate")) {
            if (sortInfo.substring(0, 1).equals("A"))
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate));
            else if (sortInfo.substring(0, 1).equals("D"))
                requestUpdates.sort(Comparator.comparing(BaseEntity::getCreatedDate).reversed());
        } else if (sortInfo.substring(1).equals("updatedDate")) {
            if (sortInfo.substring(0, 1).equals("A"))
                requestUpdates.sort(Comparator.comparing(BaseEntity::getUpdatedDate));
            else if (sortInfo.substring(0, 1).equals("D"))
                requestUpdates.sort(Comparator.comparing(BaseEntity::getUpdatedDate).reversed());
        }
    }

    private List<DashboardRequestUpdateResponse.RequestUpdateModel> mapRequests(List<RequestModel> requestUpdates) {
        List<DashboardRequestUpdateResponse.RequestUpdateModel> mappedRequests = new ArrayList<>();

        for (RequestModel requestUpdate : requestUpdates) {
            DashboardRequestUpdateResponse.RequestUpdateModel.Request request = new DashboardRequestUpdateResponse.RequestUpdateModel.Request(
                    requestUpdate.get_id(),
                    requestUpdate.getStatus(),
                    requestUpdate.getRequestNote()
            );
            DashboardRequestUpdateResponse.RequestUpdateModel.Employee employee = new DashboardRequestUpdateResponse.RequestUpdateModel.Employee(
                    requestUpdate.getEmployeeId(),
                    getEmployeeData(
                            requestUpdate.getEmployeeId()
                    ).getFullname()
            );
            DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor supervisor = new DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor(
                    getEmployeeSupervisorData(
                            requestUpdate.getEmployeeId()
                    ).getSupervisorId(),
                    getEmployeeData(
                            getEmployeeSupervisorData(
                                    requestUpdate.getEmployeeId()
                            ).getSupervisorId()
                    ).getFullname()
            );
            DashboardRequestUpdateResponse.RequestUpdateModel.Asset asset = new DashboardRequestUpdateResponse.RequestUpdateModel.Asset(
                    requestUpdate.getAssetId(),
                    getAssetData(
                            requestUpdate.getAssetId()
                    ).getName(),
                    requestUpdate.getAssetQuantity()
            );

            mappedRequests.add(
                    new DashboardRequestUpdateResponse.RequestUpdateModel(
                            request,
                            employee,
                            supervisor,
                            asset
                    )
            );
        }

        return mappedRequests;
    }
}
