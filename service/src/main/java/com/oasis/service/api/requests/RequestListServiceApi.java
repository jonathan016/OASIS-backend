package com.oasis.service.api.requests;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;

public interface RequestListServiceApi {

    long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    );

    List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    );

    String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    );

    String validateSortInformationGiven(String sort)
            throws
            BadRequestException;

}
