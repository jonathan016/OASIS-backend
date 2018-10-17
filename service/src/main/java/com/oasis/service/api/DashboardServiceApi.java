package com.oasis.service.api;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;

import java.util.List;

public interface DashboardServiceApi {
    int getAvailableAssetsCount();

    List<RequestModel> getMyPendingHandoverRequests(String employeeId);

    List<RequestModel> getMyRequestedRequests(String employeeId);

    List<RequestModel> getMyAssignedRequestedRequests(String employeeId);

    List<RequestModel> getMyAssignedPendingHandoverRequests(String employeeId);

    AssetModel getAssetData(String assetId);

    EmployeeModel getEmployeeData(String employeeId);

    SupervisionModel getEmployeeSupervisorData(String employeeId);

    String determineUserRole(String employeeId);
}
