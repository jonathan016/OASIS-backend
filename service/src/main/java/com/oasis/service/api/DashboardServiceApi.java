package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AssetModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;

import java.util.List;
import java.util.Map;

public interface DashboardServiceApi {

    Map< String, Long > getStatusSectionData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException;

    Map< String, List< ? > > getRequestUpdateSectionData(
            final String username, final String tab, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

    Map< String, List< ? > > getMyRequestsListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    Map< String, List< ? > > getOthersRequestListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< RequestModel > getUsernameRequestsList(
            final String username, final String query, final String status, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException;

    String validateSortInformationGiven(String sort)
            throws
            BadRequestException;

    List< RequestModel > getOthersRequestListPaged(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            DataNotFoundException,
            BadRequestException;

    List< EmployeeModel > getEmployeesDataFromRequest(
            final List< RequestModel > requests
    );

    List< EmployeeModel > getRequestModifiersDataFromRequest(
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
            final String username, final String photoLocation
    );

}
