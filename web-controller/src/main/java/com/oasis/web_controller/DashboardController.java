package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.response_mapper.DashboardResponseMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.service.api.dashboard.DashboardRequestUpdateServiceApi;
import com.oasis.service.api.dashboard.DashboardStatusServiceApi;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.constant.StatusConstant;
import com.oasis.tool.helper.ActiveComponentManager;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private DashboardResponseMapper dashboardResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Autowired
    private DashboardRequestUpdateServiceApi dashboardRequestUpdateServiceApi;
    @Autowired
    private DashboardStatusServiceApi dashboardStatusServiceApi;
    @Autowired
    private DashboardUtilServiceApi dashboardUtilServiceApi;

    @Autowired
    private ActiveComponentManager activeComponentManager;



    @GetMapping(value = APIMappingValue.API_STATUS,
                produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getStatusSectionData(
            @AuthenticationPrincipal
            final User user
    ) {

        final Map< String, Long > statuses;
        final long requestedRequestsCount;
        final long acceptedRequestsCount;
        final long availableAssetsCount;

        try {
            statuses = dashboardStatusServiceApi.getStatusSectionData(user.getUsername());
            requestedRequestsCount = statuses.get("requestedRequestsCount");
            acceptedRequestsCount = statuses.get("acceptedRequestsCount");
            availableAssetsCount = statuses.get("availableAssetsCount");
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                dashboardResponseMapper
                        .produceDashboardStatusSuccessResult(HttpStatus.OK.value(), requestedRequestsCount,
                                                             acceptedRequestsCount, availableAssetsCount
                        ), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = APIMappingValue.API_REQUEST_UPDATE,
                produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getRequestUpdateSectionData(
            @RequestParam(value = "tab")
            final String tab,
            @RequestParam(value = "page")
            final int page,
            @AuthenticationPrincipal
            final User user
    ) {

        final Map< String, List< ? > > requestsListData;
        final List< RequestModel > requests;
        final List< EmployeeModel > employees;
        final List< EmployeeModel > modifiers = new ArrayList<>();
        final List< AssetModel > assets;
        final long totalRecords;

        try {
            requestsListData = dashboardRequestUpdateServiceApi.getRequestUpdateSectionData(
                    user.getUsername(), tab, page);

            requests = (List< RequestModel >) requestsListData.get("requests");
            employees = (List< EmployeeModel >) requestsListData.get("employees");
            assets = (List< AssetModel >) requestsListData.get("assets");

            if (tab.equals(ServiceConstant.TAB_OTHERS)) {
                totalRecords = dashboardUtilServiceApi
                        .getRequestsCount("Others", user.getUsername(), StatusConstant.STATUS_REQUESTED);
            } else {
                modifiers.addAll((List< EmployeeModel >) requestsListData.get("modifiers"));
                totalRecords = dashboardUtilServiceApi
                        .getRequestsCount("Username", user.getUsername(), StatusConstant.STATUS_REQUESTED);
            }
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(),
                    activeComponentManager
                            .getDashboardActiveComponents(
                                    user.getUsername(),
                                    new ArrayList<>(
                                            user.getAuthorities())
                                            .get(0)
                                            .getAuthority()
                            )
            ), HttpStatus.NOT_FOUND);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(),
                    activeComponentManager
                            .getDashboardActiveComponents(
                                    user.getUsername(),
                                    new ArrayList<>(
                                            user.getAuthorities())
                                            .get(0)
                                            .getAuthority()
                            )
            ), HttpStatus.BAD_REQUEST);
        }

        if (tab.equals(ServiceConstant.TAB_OTHERS)) {
            return new ResponseEntity<>(dashboardResponseMapper
                                                .produceViewOthersFoundRequestSuccessResult(HttpStatus.OK.value(),
                                                                                            requests, employees, assets,
                                                                                            activeComponentManager
                                                                                                    .getDashboardActiveComponents(
                                                                                                            user.getUsername(),
                                                                                                            new ArrayList<>(
                                                                                                                    user.getAuthorities())
                                                                                                                    .get(0)
                                                                                                                    .getAuthority()
                                                                                                    ), page,
                                                                                            totalRecords
                                                ), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dashboardResponseMapper
                                                .produceViewMyFoundRequestSuccessResult(HttpStatus.OK.value(), requests,
                                                                                        employees, modifiers, assets,
                                                                                        activeComponentManager
                                                                                                .getDashboardActiveComponents(
                                                                                                        user.getUsername(),
                                                                                                        new ArrayList<>(
                                                                                                                user.getAuthorities())
                                                                                                                .get(0)
                                                                                                                .getAuthority()
                                                                                                ), page, totalRecords
                                                ), HttpStatus.OK);
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT,
                    method = {
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
                                                                             HttpStatus.BAD_REQUEST.name(), message,
                                                                             null
        ), HttpStatus.BAD_REQUEST);
    }

}
