package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.response_mapper.DashboardResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.ActiveComponentManager;
import com.oasis.service.ServiceConstant;
import com.oasis.service.api.DashboardServiceApi;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping(value = APIMappingValue.API_DASHBOARD)
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardController {

    @Autowired
    private DashboardServiceApi dashboardServiceApi;
    @Autowired
    private FailedResponseMapper failedResponseMapper;
    @Autowired
    private ActiveComponentManager activeComponentManager;
    @Autowired
    private DashboardResponseMapper dashboardResponseMapper;

    @GetMapping(value = APIMappingValue.API_STATUS, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getStatusSectionData(
            @PathVariable
            final String username
    ) {

        final Map< String, Long > statuses;
        final long requestedRequestsCount;
        final long acceptedRequestsCount;
        final long availableAssetsCount;

        try {
            statuses = dashboardServiceApi.getStatusSectionData(username);
            requestedRequestsCount = statuses.get("requestedRequestsCount");
            acceptedRequestsCount = statuses.get("acceptedRequestsCount");
            availableAssetsCount = statuses.get("availableAssetsCount");
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
                dashboardResponseMapper
                        .produceDashboardStatusSuccessResult(HttpStatus.OK.value(), requestedRequestsCount,
                                                             acceptedRequestsCount, availableAssetsCount
                        ), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = APIMappingValue.API_REQUEST_UPDATE, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getRequestUpdateSectionData(
            @PathVariable
            final String username,
            @RequestParam(value = "tab")
            final String tab,
            @RequestParam(value = "page")
            final int page
    ) {

        final Map< String, List< ? > > requestsListData;
        final List< RequestModel > requests;
        final List< EmployeeModel > employees;
        final List< EmployeeModel > modifiers = new ArrayList<>();
        final List< AssetModel > assets;
        final long totalRecords;

        try {
            requestsListData = dashboardServiceApi.getRequestUpdateSectionData(username, tab, page);

            requests = (List< RequestModel >) requestsListData.get("requests");
            employees = (List< EmployeeModel >) requestsListData.get("employees");
            assets = (List< AssetModel >) requestsListData.get("assets");

            if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                totalRecords = dashboardServiceApi
                        .getRequestsCount("Others", username, null, ServiceConstant.STATUS_REQUESTED, page, null);
            } else {
                modifiers.addAll((List< EmployeeModel >) requestsListData.get("modifiers"));
                totalRecords = dashboardServiceApi
                        .getRequestsCount("Username", username, null, ServiceConstant.STATUS_REQUESTED, page, null);
            }
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

        if (tab.equals(ServiceConstant.TAB_OTHERS)) {
            return new ResponseEntity<>(dashboardResponseMapper
                                                .produceViewOthersFoundRequestSuccessResult(HttpStatus.OK.value(),
                                                                                            requests, employees, assets,
                                                                                            activeComponentManager
                                                                                                    .getRequestsListDataActiveComponents(
                                                                                                            tab,
                                                                                                            username,
                                                                                                            ServiceConstant.STATUS_REQUESTED
                                                                                                    ), page,
                                                                                            totalRecords
                                                ), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dashboardResponseMapper
                                                .produceViewMyFoundRequestSuccessResult(HttpStatus.OK.value(), requests,
                                                                                        employees, modifiers, assets,
                                                                                        activeComponentManager
                                                                                                .getRequestsListDataActiveComponents(
                                                                                                        tab, username,
                                                                                                        ServiceConstant.STATUS_REQUESTED
                                                                                                ), page, totalRecords
                                                ), HttpStatus.OK);
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT, method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.HEAD,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,
            RequestMethod.TRACE
    })
    public ResponseEntity returnIncorrectMappingCalls(
            final MissingServletRequestParameterException exception
    ) {

        final String message;

        if (exception.getParameterName() != null) {
            message = exception.getMessage();
        } else {
            message = "Incorrect mapping/method!";
        }

        return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                             HttpStatus.BAD_REQUEST.name(), message
        ), HttpStatus.BAD_REQUEST);
    }

}
