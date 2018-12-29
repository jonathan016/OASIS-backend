package com.oasis.service.api.requests;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.RequestModel;

import java.util.List;
import java.util.Map;

public interface RequestOthersListServiceApi {

    Map< String, List< ? > > getOthersRequestListData(
            final String username, final String query, final String status, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< RequestModel > getOthersRequestList(
            final String username, final String query, final String status, String sort
    )
            throws
            BadRequestException;

}
