package com.oasis.service.api.dashboard;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.entity.RequestModel;

import java.util.List;
import java.util.Map;

public interface DashboardRequestUpdateServiceApi {

    Map< String, List< ? > > getRequestUpdateSectionData(
            final String username, final String tab, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< RequestModel > getOthersRequestList(
            final String username, final String status
    )
            throws
            BadRequestException;

}
