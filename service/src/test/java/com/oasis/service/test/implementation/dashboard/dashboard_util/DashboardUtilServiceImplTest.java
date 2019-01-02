package com.oasis.service.test.implementation.dashboard.dashboard_util;

import com.oasis.model.constant.service_constant.StatusConstant;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.service.api.dashboard.DashboardRequestUpdateServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import com.oasis.service.implementation.dashboard.DashboardUtilServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardUtilServiceImplTest {

    private final String[] statuses = new String[]{ StatusConstant.STATUS_REQUESTED, StatusConstant.STATUS_ACCEPTED };

    @InjectMocks
    private DashboardUtilServiceImpl dashboardUtilService;
    @Mock
    private DashboardRequestUpdateServiceApi dashboardRequestUpdateServiceApi;
    @Mock
    private RequestUtilServiceApi requestUtilServiceApi;

    @Before
    public void setUp()
            throws
            Exception {

        when(requestUtilServiceApi.countAllByUsernameEqualsAndStatusEquals("username", statuses[ 0 ])).thenReturn(
                (long) 10);
        when(requestUtilServiceApi.countAllByUsernameEqualsAndStatusEquals("username", statuses[ 1 ])).thenReturn(
                (long) 20);

        List< RequestModel > othersRequestedRequestsList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            othersRequestedRequestsList.add(new RequestModel());
        }
        when(dashboardRequestUpdateServiceApi.getOthersRequestList("username", statuses[ 0 ])).thenReturn(
                othersRequestedRequestsList);

        List< RequestModel > othersAcceptedRequestsList = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            othersAcceptedRequestsList.add(new RequestModel());
        }
        when(dashboardRequestUpdateServiceApi.getOthersRequestList("username", statuses[ 1 ])).thenReturn(
                othersAcceptedRequestsList);
    }

    @Test
    public void getRequestsCount_TypeUsernameAndStatusRequested_Returns10()
            throws
            BadRequestException {

        final long requestsCount = dashboardUtilService.getRequestsCount("Username", "username", statuses[ 0 ]);

        assertEquals(10, requestsCount);

        verify(requestUtilServiceApi, times(1)).countAllByUsernameEqualsAndStatusEquals("username", statuses[ 0 ]);
    }

    @Test
    public void getRequestsCount_TypeUsernameAndStatusAccepted_Returns20()
            throws
            BadRequestException {

        final long requestsCount = dashboardUtilService.getRequestsCount("Username", "username", statuses[ 1 ]);

        assertEquals(20, requestsCount);

        verify(requestUtilServiceApi, times(1)).countAllByUsernameEqualsAndStatusEquals("username", statuses[ 1 ]);
    }

    @Test
    public void getRequestsCount_TypeOthersAndStatusRequested_Returns30()
            throws
            BadRequestException {

        final long requestsCount = dashboardUtilService.getRequestsCount("Others", "username", statuses[ 0 ]);

        assertEquals(30, requestsCount);

        verify(dashboardRequestUpdateServiceApi, times(1)).getOthersRequestList("username", statuses[ 0 ]);
    }

    @Test
    public void getRequestsCount_TypeOthersAndStatusAccepted_Returns40()
            throws
            BadRequestException {

        final long requestsCount = dashboardUtilService.getRequestsCount("Others", "username", statuses[ 1 ]);

        assertEquals(40, requestsCount);

        verify(dashboardRequestUpdateServiceApi, times(1)).getOthersRequestList("username", statuses[ 1 ]);
    }

    @After
    public void tearDown()
            throws
            Exception {


        verifyNoMoreInteractions(requestUtilServiceApi);
        verifyNoMoreInteractions(dashboardRequestUpdateServiceApi);
    }

}