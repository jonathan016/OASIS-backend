package com.oasis.response_mapper;

import com.oasis.service.ServiceConstant;
import com.oasis.web_model.constant.ResponseStatus;
import com.oasis.web_model.response.NoPagingResponse;
import com.oasis.web_model.response.Paging;
import com.oasis.web_model.response.PagingResponse;
import com.oasis.web_model.response.success.dashboard.DashboardRequestUpdateResponse;
import com.oasis.web_model.response.success.dashboard.DashboardStatusResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DashboardResponseMapper {

    public NoPagingResponse< DashboardStatusResponse > produceDashboardStatusSuccessResult(
            final int httpStatusCode, final Map< String, Integer > statusData
    ) {

        NoPagingResponse< DashboardStatusResponse > successResponse = new NoPagingResponse<>();

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new DashboardStatusResponse(statusData.get("requestedRequestsCount"),
                                                             statusData.get("pendingHandoverRequestsCount"),
                                                             statusData.get("availableAssetsCount")
        ));

        return successResponse;
    }

    public PagingResponse< DashboardRequestUpdateResponse > produceDashboardRequestUpdateSuccessResult(
            int httpStatusCode, List< DashboardRequestUpdateResponse.RequestUpdateModel > requests, int pageNumber,
            int pageSize
    ) {

        PagingResponse< DashboardRequestUpdateResponse > successResponse = new PagingResponse<>();

        int startIndex = pageSize * (pageNumber - 1);
        int endIndex = startIndex + pageSize;

        successResponse.setCode(httpStatusCode);
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        boolean indexBelowRecordSize = endIndex < requests.size();

        if (indexBelowRecordSize) {
            successResponse.setValue(new DashboardRequestUpdateResponse(requests.subList(startIndex, endIndex)));
        } else {
            successResponse.setValue(new DashboardRequestUpdateResponse(requests.subList(startIndex, requests.size())));
        }
        successResponse.setPaging(new Paging(pageNumber, pageSize, (int) Math.ceil(
                (double) requests.size() / ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE), requests.size()));

        return successResponse;
    }

}
