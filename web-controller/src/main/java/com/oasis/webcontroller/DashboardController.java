package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.responsemapper.DashboardResponseMapper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class DashboardController {

    @Autowired
    private DashboardResponseMapper dashboardResponseMapper;
    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;

    @GetMapping(value = APIMappingValue.API_DASHBOARD_STATUS,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_OCTET_STREAM_VALUE)
    public NoPagingResponse<?> callDashboardStatusService(
            @PathVariable
                    String employeeNik
    ) {
        Map<String, Integer> statusData;

        try {
             statusData = dashboardServiceImpl.getStatusSectionData(employeeNik);
        } catch (DataNotFoundException e){
            return dashboardResponseMapper.produceDashboardStatusFailedResult(e.getErrorCode(), e.getErrorMessage());
        }

        return dashboardResponseMapper.produceDashboardStatusSuccessResult(statusData);
    }

    @GetMapping(value = APIMappingValue.API_DASHBOARD_REQUEST_UPDATE,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callDashboardRequestUpdateService(
            @PathVariable
                    String employeeNik,
            @RequestParam
                    String currentTab,
            @RequestParam
                    int pageNumber,
            @RequestParam
                    String sortInfo
    ) {
        List<DashboardRequestUpdateResponse.RequestUpdateModel> mappedData;

        try {
            mappedData = new ArrayList<>(dashboardServiceImpl.getRequestUpdateSectionData(
                    employeeNik,
                    currentTab,
                    pageNumber,
                    sortInfo
            ));
        } catch (DataNotFoundException e) {
            return dashboardResponseMapper.produceDashboardRequestUpdateFailedResult(e.getErrorCode(),
                    e.getErrorMessage()
            );
        }

        return dashboardResponseMapper.produceDashboardRequestUpdateSuccessResult(mappedData,
                pageNumber,
                ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE
        );
    }
}
