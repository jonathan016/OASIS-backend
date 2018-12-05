package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.RequestsResponseMapper;
import com.oasis.service.implementation.RequestsServiceImpl;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost")
@RequestMapping(value = APIMappingValue.API_REQUEST)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RequestsController {

    @Autowired
    private RequestsServiceImpl requestsServiceImpl;
    @Autowired
    private RequestsResponseMapper requestsResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @GetMapping(value = APIMappingValue.API_LIST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getRequestsList(
            @RequestParam(value = "username") final String username,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "sort") final String sort
    ) {

        List<RequestModel> requests;
        List<EmployeeModel> employees;
        List<AssetModel> assets;
        long totalRecords;

        if (query != null && query.isEmpty()) query = "defaultQuery";

        try {
            requests = requestsServiceImpl.getRequestsList(username, query, page, sort);
            employees = requestsServiceImpl.getEmployeeDataFromRequest(requests);
            assets = requestsServiceImpl.getAssetDataFromRequest(requests);
            totalRecords = requestsServiceImpl.getRequestsCount(username, query);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(
                    failedResponseMapper.produceFailedResult(
                            HttpStatus.BAD_REQUEST.value(),
                            badRequestException.getErrorCode(),
                            badRequestException.getErrorMessage()
                    ),
                    HttpStatus.BAD_REQUEST
            );
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(
                    failedResponseMapper.produceFailedResult(
                            HttpStatus.NOT_FOUND.value(),
                            dataNotFoundException.getErrorCode(),
                            dataNotFoundException.getErrorMessage()
                    ),
                    HttpStatus.NOT_FOUND
            );
        }

        return new ResponseEntity<>(
                requestsResponseMapper.produceViewFoundAssetSuccessResult(
                        HttpStatus.OK.value(), requests, employees, assets, null, page, totalRecords
                ),
                HttpStatus.OK
        );

    }

}
