package com.oasis.service.api.requests;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;

import java.util.List;
import java.util.Map;

public interface RequestMyListServiceApi {

    Map< String, List< ? > > getMyRequestsListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
