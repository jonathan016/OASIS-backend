package com.oasis.service.api;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.PagingResponse;

import java.util.List;

public interface DashboardServiceApi {
    List<RequestModel> getMyPendingHandoverRequests(String employeeId);

    List<RequestModel> getMyRequestedRequests(String employeeId);

    List<RequestModel> getMyAssignedRequestedRequests(String employeeId);

    List<RequestModel> getMyAssignedPendingHandoverRequests(String employeeId);

    AssetModel getAssetData(String assetId);

    EmployeeModel getEmployeeData(String employeeId);

    SupervisionModel getEmployeeSupervisorData(String employeeId);

    String determineUserRole(String employeeId);

    List<String> getSupervisedEmployeeIdList(String supervisorId);

    List<RequestModel> getRequestsList(String requestStatus, List<String> supervisedEmployeeIdList);

    NoPagingResponse<?> getStatusSectionData(String employeeId);

    PagingResponse<?> getRequestUpdateSectionData(String employeeId, String currentTab, int pageNumber, String sortInfo);
}
