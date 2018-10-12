package com.oasis.webcontroller;

import com.oasis.MappingValue;
import com.oasis.model.entity.RequestModel;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.request.DashboardRequest;
import com.oasis.webmodel.response.BaseResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.success.DashboardSuccessResponse;
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
    @PostMapping(value = MappingValue.API_DASHBOARD_STATUS,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public BaseResponse<?> callDashboardService(@RequestBody DashboardRequest model) {
        Integer availableAssetCount = dashboardServiceImpl.getAvailableAssets().size();
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

        return produceSuccessResult(
                model.getEmployeeId(), model.getRole(),
                requestedRequests.size(), pendingHandoverRequests.size(), availableAssetCount
        );
    }

    private BaseResponse<DashboardSuccessResponse>
    produceSuccessResult(String employeeId,
                         String role,
                         Integer requestedRequestsCount,
                         Integer pendingHandoverRequestsCount,
                         Integer availableAssetCount) {
        BaseResponse<DashboardSuccessResponse> successResponse = new BaseResponse<>();

        successResponse.setCode("200");
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new DashboardSuccessResponse(
                        employeeId,
                        role,
                        requestedRequestsCount,
                        pendingHandoverRequestsCount,
                        availableAssetCount
                )
        );

        return successResponse;
    }
}
