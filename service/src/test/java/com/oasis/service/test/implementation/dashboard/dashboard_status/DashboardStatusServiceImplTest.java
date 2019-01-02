package com.oasis.service.test.implementation.dashboard.dashboard_status;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.model.constant.service_constant.StatusConstant;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.service.implementation.dashboard.DashboardStatusServiceImpl;
import com.oasis.service.tool.helper.RoleDeterminer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class DashboardStatusServiceImplTest {

    private final String[] usernames = new String[]{ "admin", "superior", "employee" };
    private final String[] roles = new String[]{ RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR,
                                                 RoleConstant.ROLE_EMPLOYEE };

    @InjectMocks
    private DashboardStatusServiceImpl dashboardStatusService;
    @Mock
    private AssetUtilServiceApi assetUtilServiceApi;
    @Mock
    private DashboardUtilServiceApi dashboardUtilServiceApi;

    @Mock
    private RoleDeterminer roleDeterminer;

    @Before
    public void setUp()
            throws
            Exception {

        when(assetUtilServiceApi
                     .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO)).thenReturn((long) 10);

        when(roleDeterminer.determineRole(usernames[ 0 ])).thenReturn(roles[ 0 ]);
        when(roleDeterminer.determineRole(usernames[ 1 ])).thenReturn(roles[ 1 ]);
        when(roleDeterminer.determineRole(usernames[ 2 ])).thenReturn(roles[ 2 ]);

        when(dashboardUtilServiceApi.getRequestsCount(
                "Others", usernames[ 0 ], StatusConstant.STATUS_REQUESTED)).thenReturn((long) 5);
        when(dashboardUtilServiceApi.getRequestsCount(
                "Others", usernames[ 0 ], StatusConstant.STATUS_ACCEPTED)).thenReturn((long) 5);
        when(dashboardUtilServiceApi.getRequestsCount(
                "Others", usernames[ 1 ], StatusConstant.STATUS_REQUESTED)).thenReturn((long) 3);
        when(dashboardUtilServiceApi.getRequestsCount(
                "Username", usernames[ 1 ], StatusConstant.STATUS_ACCEPTED)).thenReturn((long) 3);
        when(dashboardUtilServiceApi.getRequestsCount(
                "Username", usernames[ 2 ], StatusConstant.STATUS_REQUESTED)).thenReturn((long) 1);
        when(dashboardUtilServiceApi.getRequestsCount(
                "Username", usernames[ 2 ], StatusConstant.STATUS_ACCEPTED)).thenReturn((long) 1);
    }

    @Test
    public void getStatusSectionData_RoleAdministrator()
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, Long > statuses = new HashMap<>();

        statuses.put("requestedRequestsCount", (long) 5);
        statuses.put("acceptedRequestsCount", (long) 5);
        statuses.put("availableAssetsCount", (long) 10);

        final Map< String, Long > producedStatuses = dashboardStatusService.getStatusSectionData(usernames[ 0 ]);

        assertEquals(statuses, producedStatuses);

        verify(assetUtilServiceApi, times(1))
                .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        verify(roleDeterminer, times(1)).determineRole(usernames[ 0 ]);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Others", usernames[ 0 ], StatusConstant.STATUS_REQUESTED);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Others", usernames[ 0 ], StatusConstant.STATUS_ACCEPTED);
    }

    @Test
    public void getStatusSectionData_RoleSuperior()
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, Long > statuses = new HashMap<>();

        statuses.put("requestedRequestsCount", (long) 3);
        statuses.put("acceptedRequestsCount", (long) 3);
        statuses.put("availableAssetsCount", (long) 10);

        final Map< String, Long > producedStatuses = dashboardStatusService.getStatusSectionData(usernames[ 1 ]);

        assertEquals(statuses, producedStatuses);

        verify(assetUtilServiceApi, times(1))
                .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        verify(roleDeterminer, times(1)).determineRole(usernames[ 1 ]);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Others", usernames[ 1 ], StatusConstant.STATUS_REQUESTED);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Username", usernames[ 1 ], StatusConstant.STATUS_ACCEPTED);
    }

    @Test
    public void getStatusSectionData_RoleEmployee()
            throws
            BadRequestException,
            DataNotFoundException {

        Map< String, Long > statuses = new HashMap<>();

        statuses.put("requestedRequestsCount", (long) 1);
        statuses.put("acceptedRequestsCount", (long) 1);
        statuses.put("availableAssetsCount", (long) 10);

        final Map< String, Long > producedStatuses = dashboardStatusService.getStatusSectionData(usernames[ 2 ]);

        assertEquals(statuses, producedStatuses);

        verify(assetUtilServiceApi, times(1))
                .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO);
        verify(roleDeterminer, times(1)).determineRole(usernames[ 2 ]);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Username", usernames[ 2 ], StatusConstant.STATUS_REQUESTED);
        verify(dashboardUtilServiceApi, times(1)).getRequestsCount(
                "Username", usernames[ 2 ], StatusConstant.STATUS_ACCEPTED);
    }

    @After
    public void tearDown()
            throws
            Exception {

        verifyNoMoreInteractions(assetUtilServiceApi);
        verifyNoMoreInteractions(roleDeterminer);
        verifyNoMoreInteractions(dashboardUtilServiceApi);
    }

}