package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;

public interface RequestsServiceApi {

    List< RequestModel > getUsernameRequestsList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< EmployeeModel > getEmployeeDataFromRequest(
            final List< RequestModel > requests
    );

    List< AssetModel > getAssetDataFromRequest(
            final List< RequestModel > requests
    );

    long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    String getEmployeeDetailPhoto(
            final String username, final String photoDirectory
    );

    void saveRequests(
            final String username, final List< RequestModel > requests
    )
            throws
            DataNotFoundException,
            BadRequestException;

    List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
