package com.oasis.service.api;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import com.oasis.webmodel.response.success.DashboardStatusResponse;

import java.util.List;

public interface DashboardServiceApi {

    List<RequestModel>
    getMyPendingHandoverRequests(
            String employeeId
    );

    List<RequestModel>
    getMyRequestedRequests(
            String employeeId
    );

    List<RequestModel>
    getMyAssignedRequestedRequests(
            String employeeId
    );

    List<RequestModel>
    getMyAssignedPendingHandoverRequests(
            String employeeId
    );

    AssetModel
    getAssetData(
            String assetId
    );

    EmployeeModel
    getEmployeeData(
            String employeeId
    );

    SupervisionModel
    getEmployeeSupervisorData(
            String employeeId
    );

    String
    determineUserRole(
            String employeeId
    );

    List<String>
    getSupervisedEmployeeIdList(
            String supervisorId
    );

    List<RequestModel>
    getRequestsList(
            String requestStatus,
            List<String> supervisedEmployeeIdList
    );

    NoPagingResponse<DashboardStatusResponse>
    produceDashboardStatusSuccessResult(
            int requestedRequestsCount,
            int pendingHandoverRequestsCount,
            int availableAssetCount
    );

    PagingResponse<DashboardRequestUpdateResponse>
    produceDashboardRequestUpdateSuccessResult(
            List<DashboardRequestUpdateResponse.RequestUpdateModel> requests,
            int pageNumber
    );

    PagingResponse<FailedResponse>
    produceDashboardRequestUpdateFailedResult(
            String errorCode,
            String errorMessage
    );

    void
    fillData(
            List<RequestModel> requestUpdates,
            String employeeId,
            String currentTab,
            String role
    );

    void
    sortData(
            List<RequestModel> requestUpdates,
            String sortInfo
    );

    List<DashboardRequestUpdateResponse.RequestUpdateModel>
    mapRequests(
            List<RequestModel> requestUpdates
    );

    NoPagingResponse<?>
    getStatusSectionData(
            String employeeId
    );

    PagingResponse<?>
    getRequestUpdateSectionData(
            String employeeId,
            String currentTab,
            int pageNumber,
            String sortInfo
    );
}
