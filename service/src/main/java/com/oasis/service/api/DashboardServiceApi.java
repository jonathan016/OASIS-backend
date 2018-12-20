package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;

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

    long getRequestsCount(
            final String type, final String username, final String query, final String status, final int page,
            String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
