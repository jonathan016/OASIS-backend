package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.PagingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class DashboardController {
    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;

    @GetMapping(value = APIMappingValue.API_DASHBOARD_STATUS,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public NoPagingResponse<?> callDashboardStatusService(@PathVariable String employeeId) {
        return dashboardServiceImpl.getStatusSectionData(employeeId);
    }

    @GetMapping(value = APIMappingValue.API_DASHBOARD_REQUEST_UPDATE,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_OCTET_STREAM_VALUE)
    public PagingResponse<?> callDashboardRequestUpdateService(@PathVariable String employeeId,
                                                               @RequestParam String currentTab,
                                                               @RequestParam int pageNumber,
                                                               @RequestParam String sortInfo) {
        return dashboardServiceImpl.getRequestUpdateSectionData(employeeId, currentTab, pageNumber, sortInfo);
    }
}
