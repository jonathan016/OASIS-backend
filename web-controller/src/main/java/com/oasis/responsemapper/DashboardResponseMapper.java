package com.oasis.responsemapper;

import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.Paging;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import com.oasis.webmodel.response.success.DashboardStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DashboardResponseMapper {

    /*--------------Status Section--------------*/
    public NoPagingResponse<DashboardStatusResponse> produceDashboardStatusSuccessResult(
            final Map<String, Integer> statusData
    ) {
        NoPagingResponse<DashboardStatusResponse> successResponse = new NoPagingResponse<>();

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(new DashboardStatusResponse(
                statusData.get("requestedRequestsCount"),
                statusData.get("pendingHandoverRequestsCount"),
                statusData.get("availableAssetsCount")
        ));

        return successResponse;
    }

    public NoPagingResponse<FailedResponse> produceDashboardStatusFailedResult(
            final String errorCode, final String errorMessage
    ) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(HttpStatus.OK.value());
        failedResponse.setSuccess(ResponseStatus.SUCCESS);
        failedResponse.setValue(new FailedResponse(
                errorCode,
                errorMessage
        ));

        return failedResponse;
    }

    /*--------------Request Update Section--------------*/
    public PagingResponse<DashboardRequestUpdateResponse> produceDashboardRequestUpdateSuccessResult(
            List<DashboardRequestUpdateResponse.RequestUpdateModel> requests, int pageNumber,
            int pageSize
    ) {
        PagingResponse<DashboardRequestUpdateResponse> successResponse = new PagingResponse<>();

        int startIndex = pageSize * (pageNumber - 1);
        int endIndex = startIndex + pageSize;

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);

        boolean indexBelowRecordSize = endIndex < requests.size();

        if (indexBelowRecordSize) {
            successResponse.setValue(
                    new DashboardRequestUpdateResponse(requests.subList(startIndex, endIndex)));
        } else {
            successResponse.setValue(new DashboardRequestUpdateResponse(
                    requests.subList(startIndex, requests.size())));
        }
        successResponse.setPaging(new Paging(pageNumber, pageSize, requests.size()));

        return successResponse;
    }

    public PagingResponse<FailedResponse> produceDashboardRequestUpdateFailedResult(
            String errorCode, String errorMessage
    ) {
        PagingResponse<FailedResponse> failedResponse = new PagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(new FailedResponse(errorCode, errorMessage));
        failedResponse.setPaging(null);

        return failedResponse;
    }
}
