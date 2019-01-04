package com.oasis.service.api.dashboard;

import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;

import java.util.Map;

public interface DashboardStatusServiceApi {

    Map< String, Long > getStatusSectionData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException;

}
