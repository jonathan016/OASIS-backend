package com.oasis.service.api;

import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;

public interface DashboardServiceApi {
    List<AssetModel> getAvailableAssets();
    List<RequestModel> getMyPendingHandoverRequests(String employeeId);
    List<RequestModel> getMyRequestedRequests(String employeeId);
    List<RequestModel> getMyAssignedRequestedRequests(String employeeId);
    List<RequestModel> getMyAssignedPendingHandoverRequests(String employeeId);
}
