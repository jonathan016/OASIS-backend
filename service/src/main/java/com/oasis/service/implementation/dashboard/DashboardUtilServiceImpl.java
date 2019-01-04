package com.oasis.service.implementation.dashboard;

import com.oasis.model.exception.BadRequestException;
import com.oasis.service.api.dashboard.DashboardRequestUpdateServiceApi;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardUtilServiceImpl
        implements DashboardUtilServiceApi {

    @Autowired
    private DashboardRequestUpdateServiceApi dashboardRequestUpdateServiceApi;
    @Autowired
    private RequestUtilServiceApi requestUtilServiceApi;



    @Override
    public long getRequestsCount(
            final String type, final String username, final String status
    )
            throws
            BadRequestException {

        final boolean emptyStatusGiven = ( status != null && status.isEmpty() );

        if (emptyStatusGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            switch (type) {
                case "Username":
                    return requestUtilServiceApi.countAllByUsernameEqualsAndStatusEquals(username, status);
                case "Others":
                    return dashboardRequestUpdateServiceApi.getOthersRequestList(username, status).size();
                default:
                    return -1;
            }
        }
    }

}