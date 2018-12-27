package com.oasis.service.api.dashboard;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;

import java.util.Map;

public interface DashboardStatusServiceApi {

    Map< String, Long > getStatusSectionData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException;

}
