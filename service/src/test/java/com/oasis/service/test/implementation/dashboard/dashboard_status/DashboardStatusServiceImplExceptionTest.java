package com.oasis.service.test.implementation.dashboard.dashboard_status;

import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.service.api.assets.AssetUtilServiceApi;
import com.oasis.service.implementation.dashboard.DashboardStatusServiceImpl;
import com.oasis.service.tool.helper.RoleDeterminer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardStatusServiceImplExceptionTest {

    private final String invalidUsername = "E.A.R.lEON";
    private final String unregisteredUsername = "unregistered";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private DashboardStatusServiceImpl dashboardStatusService;
    @Mock
    private AssetUtilServiceApi assetUtilServiceApi;
    @Mock
    private RoleDeterminer roleDeterminer;

    @Before
    public void setUp()
            throws
            Exception {

        when(roleDeterminer.determineRole(unregisteredUsername)).thenThrow(DataNotFoundException.class);
        when(assetUtilServiceApi
                     .countAllByDeletedIsFalseAndStockGreaterThan(ServiceConstant.ZERO)).thenReturn((long) 0);
    }

    @Test
    public void getStatusSectionData_InvalidUsernameFormatGiven_ThrowsBadRequestException()
            throws
            BadRequestException,
            DataNotFoundException {

        thrown.expect(BadRequestException.class);
        dashboardStatusService.getStatusSectionData(invalidUsername);
    }

    @Test
    public void getStatusSectionData_UnregisteredUsernameGiven_ThrowsDataNotFoundException()
            throws
            BadRequestException,
            DataNotFoundException {

        thrown.expect(DataNotFoundException.class);
        dashboardStatusService.getStatusSectionData(unregisteredUsername);
    }

}
