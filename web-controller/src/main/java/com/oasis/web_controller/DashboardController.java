package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.response_mapper.DashboardResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.ServiceConstant;
import com.oasis.service.implementation.DashboardServiceImpl;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.web_model.response.success.dashboard.DashboardRequestUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping(value = APIMappingValue.API_DASHBOARD)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardController {

    @Autowired
    private DashboardResponseMapper dashboardResponseMapper;
    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @GetMapping(value = APIMappingValue.API_STATUS, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callDashboardStatusService(
            @PathVariable
            final String username
    ) {

        Map< String, Integer > statusData;

        try {
            statusData = dashboardServiceImpl.getStatusSectionData(username);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                dashboardResponseMapper.produceDashboardStatusSuccessResult(HttpStatus.OK.value(), statusData),
                HttpStatus.OK
        );
    }

    @GetMapping(value = APIMappingValue.API_REQUEST_UPDATE, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity callDashboardRequestUpdateService(
            @PathVariable
            final String username,
            @RequestParam
            final String tab,
            @RequestParam
            final int page,
            @RequestParam
            final String sort
    ) {

        List< DashboardRequestUpdateResponse.RequestUpdateModel > mappedData;

        try {
            mappedData = new ArrayList<>(dashboardServiceImpl.getRequestUpdateSectionData(username, tab, page, sort));
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                dashboardResponseMapper.produceDashboardRequestUpdateSuccessResult(HttpStatus.OK.value(), mappedData,
                                                                                   page,
                                                                                   ServiceConstant.DASHBOARD_REQUEST_UPDATE_PAGE_SIZE
                ), HttpStatus.OK);
    }

}
