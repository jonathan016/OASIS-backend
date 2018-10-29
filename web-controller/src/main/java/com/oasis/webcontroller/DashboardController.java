package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.responsemapper.DashboardResponseMapper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.webmodel.response.success.DashboardRequestUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity callDashboardStatusService(
            @PathVariable
                    String employeeNik
    ) {
        Map<String, Integer> statusData;

        try {
            statusData = dashboardServiceImpl.getStatusSectionData(employeeNik);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(dashboardResponseMapper.produceDashboardStatusFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dashboardResponseMapper.produceDashboardStatusSuccessResult(HttpStatus.OK.value(), statusData), HttpStatus.OK);
    }

    @GetMapping(value = APIMappingValue.API_DASHBOARD_REQUEST_UPDATE,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callDashboardRequestUpdateService(
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
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(dashboardResponseMapper.produceDashboardRequestUpdateFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dashboardResponseMapper.produceDashboardRequestUpdateSuccessResult(HttpStatus.OK.value(),
                mappedData,
                pageNumber,
                ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE
        ), HttpStatus.OK);
    }
}
