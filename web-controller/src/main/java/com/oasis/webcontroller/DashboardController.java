package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.constant.ControllerConstant;
import com.oasis.model.BaseEntity;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse.RequestUpdateModel;
import com.oasis.webmodel.response.success.DashboardStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.oasis.constant.ErrorCodeAndMessage.INCORRECT_EMPLOYEE_ID;
import static com.oasis.constant.ErrorCodeAndMessage.REQUESTS_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class DashboardController {
    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;

    @GetMapping(value = APIMappingValue.API_DASHBOARD_STATUS,
            produces = APPLICATION_JSON_VALUE)
    public NoPagingResponse<?> callDashboardStatusService(@PathVariable String employeeId) {
        Integer availableAssetCount = dashboardServiceImpl.getAvailableAssetsCount();
        List<RequestModel> requestedRequests = new ArrayList<>();
        List<RequestModel> pendingHandoverRequests = new ArrayList<>();

        String role = dashboardServiceImpl.determineUserRole(employeeId);

        switch (role) {
            case "ADMINISTRATOR":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyAssignedRequestedRequests(
                                employeeId
                        )
                );
                pendingHandoverRequests.addAll(
                        dashboardServiceImpl.getMyAssignedPendingHandoverRequests(
                                employeeId
                        )
                );
                break;
            case "SUPERIOR":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyAssignedRequestedRequests(
                                employeeId
                        )
                );
                break;
            case "EMPLOYEE":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyRequestedRequests(
                                employeeId
                        )
                );
                break;
        }
        pendingHandoverRequests.addAll(
                dashboardServiceImpl.getMyPendingHandoverRequests(
                        employeeId
                )
        );

        return produceDashboardStatusSuccessResult(
                requestedRequests.size(),
                pendingHandoverRequests.size(),
                availableAssetCount
        );
    }

    @GetMapping(value = APIMappingValue.API_DASHBOARD_REQUEST_UPDATE,
            produces = APPLICATION_JSON_VALUE)
    public PagingResponse<?> callDashboardRequestUpdateService(@PathVariable String employeeId,
                                                               @RequestParam String currentTab,
                                                               @RequestParam Integer pageNumber,
                                                               @RequestParam String sortInfo) {
        List<RequestModel> requestUpdates = new ArrayList<>();

        if(dashboardServiceImpl.getEmployeeData(employeeId) == null){
            return produceDashboardRequestUpdateFailedResult(INCORRECT_EMPLOYEE_ID[0], INCORRECT_EMPLOYEE_ID[1]);
        }
        String role = dashboardServiceImpl.determineUserRole(employeeId);

        fillData(requestUpdates, employeeId, currentTab, role);

        if (requestUpdates.size() == 0){
            return produceDashboardRequestUpdateFailedResult(REQUESTS_NOT_FOUND[0], REQUESTS_NOT_FOUND[1]);
        }

        if ((int) Math.ceil(
                (float) requestUpdates.size() / ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) < pageNumber){
            return produceDashboardRequestUpdateFailedResult(REQUESTS_NOT_FOUND[0], REQUESTS_NOT_FOUND[1]);
        }

        sortData(requestUpdates, sortInfo);
        List<RequestUpdateModel> mappedRequests = mapRequests(requestUpdates);

        return produceDashboardRequestUpdateSuccessResult(
                mappedRequests,
                pageNumber
        );
    }

    private NoPagingResponse<DashboardStatusResponse>
    produceDashboardStatusSuccessResult(Integer requestedRequestsCount,
                                        Integer pendingHandoverRequestsCount,
                                        Integer availableAssetCount) {
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
    produceDashboardRequestUpdateSuccessResult(List<RequestUpdateModel> requests,
                                               Integer pageNumber) {
        PagingResponse<DashboardRequestUpdateResponse> successResponse = new PagingResponse<>();
        int startIndex = ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE * (pageNumber - 1);
        int endIndex = startIndex + ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE;

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
                        ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE,
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
                            dashboardServiceImpl.getMyAssignedRequestedRequests(
                                    employeeId
                            )
                    );
                } else if (currentTab.equals("Self")) {
                    //Should throw exception when no requested request exists
                    requestUpdates.addAll(
                            dashboardServiceImpl.getMyRequestedRequests(
                                    employeeId
                            )
                    );
                }
                break;
            case "EMPLOYEE":
                //Should throw exception when no requested request exists
                requestUpdates.addAll(
                        dashboardServiceImpl.getMyRequestedRequests(
                                employeeId
                        )
                );
                break;
        }
    }

    private void sortData(List<RequestModel> requestUpdates, String sortInfo) {
        if (sortInfo.substring(1).equals("createdDate")) {
            if (sortInfo.substring(0, 1).equals("A"))
                Collections.sort(requestUpdates, Comparator.comparing(BaseEntity::getCreatedDate));
            else if (sortInfo.substring(0, 1).equals("D"))
                Collections.sort(requestUpdates, Comparator.comparing(BaseEntity::getCreatedDate).reversed());
        } else if (sortInfo.substring(1).equals("updatedDate")) {
            if (sortInfo.substring(0, 1).equals("A"))
                Collections.sort(requestUpdates, Comparator.comparing(BaseEntity::getUpdatedDate));
            else if (sortInfo.substring(0, 1).equals("D"))
                Collections.sort(requestUpdates, Comparator.comparing(BaseEntity::getUpdatedDate).reversed());
        }
    }

    private List<DashboardRequestUpdateResponse.RequestUpdateModel> mapRequests(List<RequestModel> requestUpdates) {
        List<DashboardRequestUpdateResponse.RequestUpdateModel> mappedRequests = new ArrayList<>();

        for (RequestModel requestUpdate : requestUpdates) {
            RequestUpdateModel.Request request = new DashboardRequestUpdateResponse.RequestUpdateModel.Request(
                    requestUpdate.get_id(),
                    requestUpdate.getStatus(),
                    requestUpdate.getRequestNote()
            );
            RequestUpdateModel.Employee employee = new DashboardRequestUpdateResponse.RequestUpdateModel.Employee(
                    requestUpdate.getEmployeeId(),
                    dashboardServiceImpl.getEmployeeData(
                            requestUpdate.getEmployeeId()
                    ).getFullname()
            );
            RequestUpdateModel.Supervisor supervisor = new DashboardRequestUpdateResponse.RequestUpdateModel.Supervisor(
                    dashboardServiceImpl.getEmployeeSupervisorData(
                            requestUpdate.getEmployeeId()
                    ).getSupervisorId(),
                    dashboardServiceImpl.getEmployeeData(
                            dashboardServiceImpl.getEmployeeSupervisorData(
                                    requestUpdate.getEmployeeId()
                            ).getSupervisorId()
                    ).getFullname()
            );
            RequestUpdateModel.Asset asset = new DashboardRequestUpdateResponse.RequestUpdateModel.Asset(
                    requestUpdate.getAssetId(),
                    dashboardServiceImpl.getAssetData(
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
