package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.constant.ControllerConstant;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.request.DashboardRequestUpdateRequest;
import com.oasis.webmodel.request.DashboardStatusRequest;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.responsemodel.RequestUpdateSectionResponseModel;
import com.oasis.webmodel.response.success.DashboardRequestUpdateSuccessResponse;
import com.oasis.webmodel.response.success.DashboardStatusSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class DashboardController {
    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;

    @CrossOrigin(origins = "http://localhost")
    @PostMapping(value = APIMappingValue.API_DASHBOARD_STATUS,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public PagingResponse<?> callDashboardStatusService(@RequestBody DashboardStatusRequest model) {
        Integer availableAssetCount = dashboardServiceImpl.getAvailableAssetsCount();
        List<RequestModel> requestedRequests = new ArrayList<>();
        List<RequestModel> pendingHandoverRequests = new ArrayList<>();

        switch (model.getRole()) {
            case "ADMINISTRATOR":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyAssignedRequestedRequests(
                                model.getEmployeeId()
                        )
                );
                pendingHandoverRequests.addAll(
                        dashboardServiceImpl.getMyAssignedPendingHandoverRequests(
                                model.getEmployeeId()
                        )
                );
                break;
            case "SUPERIOR":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyAssignedRequestedRequests(
                                model.getEmployeeId()
                        )
                );
                break;
            case "EMPLOYEE":
                requestedRequests.addAll(
                        dashboardServiceImpl.getMyRequestedRequests(
                                model.getEmployeeId()
                        )
                );
                break;
        }
        pendingHandoverRequests.addAll(
                dashboardServiceImpl.getMyPendingHandoverRequests(model.getEmployeeId())
        );

        return produceDashboardStatusSuccessResult(
                model.getEmployeeId(), model.getRole(),
                requestedRequests.size(), pendingHandoverRequests.size(), availableAssetCount
        );
    }

    @CrossOrigin(origins = "http://localhost")
    @PostMapping(value = APIMappingValue.API_DASHBOARD_REQUEST_UPDATE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public PagingResponse<?> callDashboardRequestUpdateService(@RequestBody DashboardRequestUpdateRequest model) {
        List<RequestModel> requestUpdates = new ArrayList<>();

        switch (model.getRole()) {
            case "ADMINISTRATOR":
            case "SUPERIOR":
                if (model.getCurrentTab().equals("Others")) {
                    //Should throw exception when no requested request exists
                    requestUpdates.addAll(
                            dashboardServiceImpl.getMyAssignedRequestedRequests(
                                    model.getEmployeeId()
                            )
                    );
                } else if (model.getCurrentTab().equals("Self")) {
                    //Should throw exception when no requested request exists
                    requestUpdates.addAll(
                            dashboardServiceImpl.getMyRequestedRequests(
                                    model.getEmployeeId()
                            )
                    );
                }
                break;
            case "EMPLOYEE":
                //Should throw exception when no requested request exists
                requestUpdates.addAll(
                        dashboardServiceImpl.getMyRequestedRequests(
                                model.getEmployeeId()
                        )
                );
                break;
        }

        List<RequestUpdateSectionResponseModel> mappedRequests = new ArrayList<>();
        for (RequestModel request : requestUpdates) {
            mappedRequests.add(
                    new RequestUpdateSectionResponseModel(
                            request.getAssetId(),
                            dashboardServiceImpl.getAssetData(
                                    request.getAssetId()
                            ).getName(),
                            request.getAssetQuantity(),
                            request.getRequestNote(),
                            request.getEmployeeId(),
                            dashboardServiceImpl.getEmployeeData(
                                    request.getEmployeeId()
                            ).getFullname(),
                            request.getStatus(),
                            dashboardServiceImpl.getEmployeeSupervisorData(
                                    request.getEmployeeId()
                            ).getSupervisorId(),
                            dashboardServiceImpl.getEmployeeData(
                                    dashboardServiceImpl.getEmployeeSupervisorData(
                                            request.getEmployeeId()
                                    ).getSupervisorId()
                            ).getFullname()
                    )
            );
        }

        return produceDashboardRequestUpdateSuccessResult(
                model.getEmployeeId(),
                model.getRole(),
                mappedRequests,
                model.getStartIndex()
        );
    }

    private PagingResponse<DashboardStatusSuccessResponse>
    produceDashboardStatusSuccessResult(String employeeId,
                                        String role,
                                        Integer requestedRequestsCount,
                                        Integer pendingHandoverRequestsCount,
                                        Integer availableAssetCount) {
        PagingResponse<DashboardStatusSuccessResponse> successResponse = new PagingResponse<>();

        successResponse.setCode("200");
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new DashboardStatusSuccessResponse(
                        employeeId,
                        role,
                        requestedRequestsCount,
                        pendingHandoverRequestsCount,
                        availableAssetCount
                )
        );

        return successResponse;
    }

    private PagingResponse<DashboardRequestUpdateSuccessResponse>
    produceDashboardRequestUpdateSuccessResult(String employeeId,
                                               String role,
                                               List<RequestUpdateSectionResponseModel> requests,
                                               Integer startIndex) {
        PagingResponse<DashboardRequestUpdateSuccessResponse> successResponse = new PagingResponse<>();

        successResponse.setCode("200");
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        if (startIndex + ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE < requests.size()) {
            successResponse.setValue(
                    new DashboardRequestUpdateSuccessResponse(
                            employeeId,
                            role,
                            requests.subList(
                                    startIndex,
                                    startIndex + ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE
                            )
                    )
            );
            successResponse.setPaging(
                    new Paging(
                            ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE,
                            (startIndex / ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) + 1,
                            requests.size()
                    )
            );
        } else {
            successResponse.setValue(
                    new DashboardRequestUpdateSuccessResponse(
                            employeeId,
                            role,
                            requests.subList(
                                    startIndex,
                                    requests.size()
                            )
                    )
            );
            successResponse.setPaging(
                    new Paging(
                            ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE,
                            (startIndex / ControllerConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE) + 1,
                            requests.size()
                    )
            );
        }

        return successResponse;
    }
}
