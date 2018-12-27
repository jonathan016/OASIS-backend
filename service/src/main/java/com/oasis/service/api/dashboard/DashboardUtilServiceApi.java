package com.oasis.service.api.dashboard;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;

public interface DashboardUtilServiceApi {

    long getRequestsCount(
            final String type, final String username, final String status, final int page
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
