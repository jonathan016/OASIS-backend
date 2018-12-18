package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.request_mapper.RequestsRequestMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.RequestsResponseMapper;
import com.oasis.service.ActiveComponentManager;
import com.oasis.service.api.RequestsServiceApi;
import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.web_model.request.requests.AssetRequestDetailsRequest;
import com.oasis.web_model.request.requests.SaveRequestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequestMapping(value = APIMappingValue.API_REQUEST)
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestsController {

    @Autowired
    private RequestsServiceApi requestsServiceApi;
    @Autowired
    private FailedResponseMapper failedResponseMapper;
    @Autowired
    private RequestsRequestMapper requestsRequestMapper;
    @Autowired
    private RequestsResponseMapper requestsResponseMapper;
    @Autowired
    private ActiveComponentManager activeComponentManager;

    @SuppressWarnings("unchecked")
    @GetMapping(value = APIMappingValue.API_MY_REQUESTS, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getMyRequestsListData(
            @PathVariable(value = "username")
            final String username,
            @RequestParam(value = "query", required = false)
            final String query,
            @RequestParam(value = "status", required = false)
            final String status,
            @RequestParam(value = "page")
            final int page,
            @RequestParam(value = "sort", required = false)
            final String sort
    ) {

        final Map< String, List< ? > > requestsListData;
        final List< RequestModel > requests;
        final List< EmployeeModel > employees;
        final List< EmployeeModel > modifiers;
        final List< AssetModel > assets;
        final long totalRecords;

        try {
            requestsListData = requestsServiceApi.getMyRequestsListData(username, query, status, page, sort);

            requests = (List< RequestModel >) requestsListData.get("requests");
            employees = (List< EmployeeModel >) requestsListData.get("employees");
            modifiers = (List< EmployeeModel >) requestsListData.get("modifiers");
            assets = (List< AssetModel >) requestsListData.get("assets");
            totalRecords = requestsServiceApi.getRequestsCount("Username", username, query, status, page, sort);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(requestsResponseMapper
                                            .produceViewMyFoundRequestSuccessResult(HttpStatus.OK.value(), requests,
                                                                                    employees, modifiers, assets,
                                                                                    activeComponentManager
                                                                                            .getRequestsListDataActiveComponents(
                                                                                                    "my", username,
                                                                                                    status
                                                                                            ), page, totalRecords
                                            ), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = APIMappingValue.API_OTHERS_REQUESTS, produces = APPLICATION_JSON_VALUE,
                consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getOthersRequestListData(
            @PathVariable(value = "username")
            final String username,
            @RequestParam(value = "query", required = false)
            final String query,
            @RequestParam(value = "status", required = false)
            final String status,
            @RequestParam(value = "page")
            final int page,
            @RequestParam(value = "sort", required = false)
            final String sort
    ) {

        final Map< String, List< ? > > othersRequestListData;
        final List< RequestModel > requests;
        final List< EmployeeModel > employees;
        final List< AssetModel > assets;
        final long totalRecords;

        try {
            othersRequestListData = requestsServiceApi.getOthersRequestListData(username, query, status, page, sort);

            requests = (List< RequestModel >) othersRequestListData.get("requests");
            employees = (List< EmployeeModel >) othersRequestListData.get("employees");
            assets = (List< AssetModel >) othersRequestListData.get("assets");
            totalRecords = requestsServiceApi.getRequestsCount("Others", username, query, status, page, sort);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(requestsResponseMapper
                                            .produceViewOthersFoundRequestSuccessResult(HttpStatus.OK.value(), requests,
                                                                                        employees, assets,
                                                                                        activeComponentManager
                                                                                                .getRequestsListDataActiveComponents(
                                                                                                        "others",
                                                                                                        username, status
                                                                                                ), page, totalRecords
                                            ), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_REQUESTED_ASSETS, produces = APPLICATION_JSON_VALUE,
                 consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity getAssetRequestDetailsData(
            @RequestBody
            final AssetRequestDetailsRequest request
    ) {

        final List< AssetModel > requestedAssets;
        final List< List< String > > requestedAssetsImages;
        final int page = request.getPage();
        final long totalRecords;

        try {
            requestedAssets = requestsServiceApi.getAssetRequestDetailsList(request.getSkus(), page);
            requestedAssetsImages = requestsServiceApi.getAssetRequestDetailsImages(requestedAssets);
            totalRecords = requestsServiceApi.getAssetRequestDetailsCount(request.getSkus(), page);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(requestsResponseMapper
                                            .produceViewAssetRequestDetailsSuccessResult(HttpStatus.OK.value(),
                                                                                         requestedAssets,
                                                                                         requestedAssetsImages, page,
                                                                                         totalRecords
                                            ), HttpStatus.OK);
    }

    @PostMapping(value = APIMappingValue.API_SAVE, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity saveRequest(
            @RequestBody
            final SaveRequestRequest request
    ) {

        final List< RequestModel > requests = requestsRequestMapper.getRequestsListFromRequest(request);

        try {
            requestsServiceApi.saveRequests(request.getUsername(), requests);
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
        } catch (UnauthorizedOperationException unauthorizedOperationException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorCode(),
                                                                                 unauthorizedOperationException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(requestsResponseMapper.produceRequestSaveSuccessResult(HttpStatus.CREATED.value()),
                                    HttpStatus.CREATED
        );

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
