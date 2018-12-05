package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;

public interface RequestsServiceApi {

    List<RequestModel> getRequestsList(
            final String username,
            final String query,
            final int page,
            final String sort
    ) throws BadRequestException, DataNotFoundException;

    List<EmployeeModel> getEmployeeDataFromRequest(
            final List<RequestModel> requests
    );

    List<AssetModel> getAssetDataFromRequest(
            final List<RequestModel> requests
    );

    long getRequestsCount(
            final String username,
            final String query
    ) throws BadRequestException;

    String getEmployeeDetailPhoto(
            final String username,
            final String photoDirectory
    );

}
