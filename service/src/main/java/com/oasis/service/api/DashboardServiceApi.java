package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.web_model.response.success.dashboard.DashboardRequestUpdateResponse;

import java.util.List;
import java.util.Map;

public interface DashboardServiceApi {

    List<RequestModel> getMyPendingHandoverRequests(
            final String username
    );

    List<RequestModel> getMyRequestedRequests(
            final String username
    );

    List<RequestModel> getOthersRequestedRequests(
            final String username
    );

    List<RequestModel> getOthersPendingHandoverRequests(
            final String username
    );

    AssetModel getAssetData(
            final String sku
    );

    EmployeeModel getEmployeeData(
            final String username
    );

    SupervisionModel getEmployeeSupervisorData(
            final String username
    );

    String determineUserRole(
            final String username
    ) throws DataNotFoundException;

    List<String> getSupervisedEmployeeUsernameList(
            final String username
    );

    List<RequestModel> getRequestsList(
            final String requestStatus, final List<String> supervisedEmployeeUsernameList
    );

    List<RequestModel> fillData(
            final String username, final String tab, final String role
    );

    void sortData(
            List<RequestModel> requestUpdates, final String sort
    );

    List<DashboardRequestUpdateResponse.RequestUpdateModel> mapRequests(
            final List<RequestModel> requestUpdates
    );

    Map<String, Integer> getStatusSectionData(
            final String username
    ) throws DataNotFoundException, BadRequestException;

    List<DashboardRequestUpdateResponse.RequestUpdateModel> getRequestUpdateSectionData(
            final String username, final String tab, final int page, final String sort
    )
            throws DataNotFoundException;

}
