package com.oasis.service.test.implementation.dashboard.dashboard_util;

import com.oasis.model.exception.BadRequestException;
import com.oasis.service.implementation.dashboard.DashboardUtilServiceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DashboardUtilServiceImplExceptionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private DashboardUtilServiceImpl dashboardUtilService;

    @Test
    public void getRequestsCount_EmptyStatusGiven_ThrowsBadRequestException()
            throws
            BadRequestException {

        thrown.expect(BadRequestException.class);
        dashboardUtilService.getRequestsCount("Others", "username", "");
    }

}
