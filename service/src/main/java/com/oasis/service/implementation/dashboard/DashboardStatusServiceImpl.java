package com.oasis.service.implementation.dashboard;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.dashboard.DashboardStatusServiceApi;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.tool.constant.RoleConstant;
import com.oasis.tool.constant.ServiceConstant;
import com.oasis.tool.constant.StatusConstant;
import com.oasis.tool.helper.RoleDeterminer;
import com.oasis.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.oasis.exception.helper.ErrorCodeAndMessage.INCORRECT_PARAMETER;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DashboardStatusServiceImpl
        implements DashboardStatusServiceApi {

    @Autowired
    private AssetUtilServiceApi assetUtilServiceApi;
    @Autowired
    private DashboardUtilServiceApi dashboardUtilServiceApi;

    @Autowired
    private RoleDeterminer roleDeterminer;



    @Override
    public Map< String, Long > getStatusSectionData(final String username)
            throws
            DataNotFoundException,
            BadRequestException {

        if (!username.matches(Regex.REGEX_USERNAME)) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            long requestedRequestsCount = 0;
            long acceptedRequestsCount = 0;
            final long availableAssetCount = assetUtilServiceApi
                    .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);

            switch (roleDeterminer.determineRole(username)) {
                case RoleConstant.ROLE_ADMINISTRATOR:
                    requestedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Others", username, StatusConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Others", username, StatusConstant.STATUS_ACCEPTED, 1);
                    break;
                case RoleConstant.ROLE_SUPERIOR:
                    requestedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Others", username, StatusConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Username", username, StatusConstant.STATUS_ACCEPTED, 1);
                    break;
                case RoleConstant.ROLE_EMPLOYEE:
                    requestedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Username", username, StatusConstant.STATUS_REQUESTED, 1);
                    acceptedRequestsCount += dashboardUtilServiceApi.getRequestsCount(
                            "Username", username, StatusConstant.STATUS_ACCEPTED, 1);
                    break;
            }

            Map< String, Long > statuses = new HashMap<>();
            statuses.put("requestedRequestsCount", requestedRequestsCount);
            statuses.put("acceptedRequestsCount", acceptedRequestsCount);
            statuses.put("availableAssetsCount", availableAssetCount);

            return statuses;
        }
    }

}
