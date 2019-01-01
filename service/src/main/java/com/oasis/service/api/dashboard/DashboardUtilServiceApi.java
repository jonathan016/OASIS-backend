package com.oasis.service.api.dashboard;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;

public interface DashboardUtilServiceApi {

    long getRequestsCount(
            final String type, final String username, final String status
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
