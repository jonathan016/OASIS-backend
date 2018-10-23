package com.oasis.service.api;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;

import java.util.List;
import java.util.Map;

public interface DashboardServiceApi {

    List<RequestModel> getMyPendingHandoverRequests(
            final String employeeNik
    );

    List<RequestModel> getMyRequestedRequests(
            final String employeeNik
    );

    List<RequestModel> getOthersRequestedRequests(
            final String employeeNik
    );

    List<RequestModel> getOthersPendingHandoverRequests(
            final String employeeNik
    );

    AssetModel getAssetData(
            final String assetId
    );

    EmployeeModel getEmployeeData(
            final String employeeNik
    );

    SupervisionModel getEmployeeSupervisorData(
            final String employeeNik
    );

    String determineUserRole(
            final String employeeNik
    ) throws DataNotFoundException;

    List<String> getSupervisedEmployeeNikList(
            final String supervisorNik
    );

    List<RequestModel> getRequestsList(
            final String requestStatus, final List<String> supervisedEmployeeIdList
    );

    List<RequestModel> fillData(
            final String employeeNik, final String currentTab, final String role
    );

    void sortData(
            List<RequestModel> requestUpdates, final String sortInfo
    );

    List<DashboardRequestUpdateResponse.RequestUpdateModel> mapRequests(
            final List<RequestModel> requestUpdates
    );

    Map<String, Integer> getStatusSectionData(
            final String employeeNik
    ) throws DataNotFoundException;

    List<DashboardRequestUpdateResponse.RequestUpdateModel> getRequestUpdateSectionData(
            final String employeeNik, final String currentTab, final int pageNumber, final String sortInfo
    )
            throws DataNotFoundException;
}
