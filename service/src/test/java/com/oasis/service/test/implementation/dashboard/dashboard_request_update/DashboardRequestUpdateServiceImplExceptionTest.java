package com.oasis.service.test.implementation.dashboard.dashboard_request_update;

import com.oasis.model.constant.service_constant.ServiceConstant;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.service.api.dashboard.DashboardUtilServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import com.oasis.service.implementation.dashboard.DashboardRequestUpdateServiceImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class DashboardRequestUpdateServiceImplExceptionTest {

    private final String invalidUsername = "E.A.R.lEON";
    private final String invalidTab = "invalidTab";
    private final String[] undeletedEmployeeUsernames = new String[]{ "d.grayson", "t.drake", "c.marks", "c.kent1",
                                                                      "jackson", "mgann", "w.west", "j.pierce" };
    private final String undeletedSupervisorUsername = "b.wayne";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private DashboardUtilServiceApi dashboardUtilServiceApi;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Mock
    private RequestUtilServiceApi requestUtilServiceApi;
    @InjectMocks
    private DashboardRequestUpdateServiceImpl dashboardRequestUpdateService;

    @Test
    public void getRequestUpdateSectionData_OthersInvalidUsernameFormatGiven_ThrowsBadRequestException()
            throws
            BadRequestException,
            DataNotFoundException {

        thrown.expect(BadRequestException.class);
        dashboardRequestUpdateService.getRequestUpdateSectionData(invalidUsername, ServiceConstant.TAB_OTHERS, 1);
    }

    @Test
    public void getRequestUpdateSectionData_InvalidTabGiven_ThrowsBadRequestException()
            throws
            BadRequestException,
            DataNotFoundException {

        thrown.expect(BadRequestException.class);
        dashboardRequestUpdateService.getRequestUpdateSectionData(undeletedSupervisorUsername, invalidTab, 1);
    }

    @Test
    public void getRequestUpdateSectionData_OthersZeroPageGiven_ThrowsDataNotFoundException()
            throws
            BadRequestException,
            DataNotFoundException {

        mockSupervisionData();

        thrown.expect(DataNotFoundException.class);
        dashboardRequestUpdateService.getRequestUpdateSectionData(
                undeletedSupervisorUsername, ServiceConstant.TAB_OTHERS, 0);
    }

    @Test
    public void getRequestUpdateSectionData_MyInvalidUsernameFormatGiven_ThrowsBadRequestException()
            throws
            BadRequestException,
            DataNotFoundException {

        thrown.expect(BadRequestException.class);
        dashboardRequestUpdateService.getRequestUpdateSectionData(invalidUsername, ServiceConstant.TAB_MY, 1);
    }

    @Test
    public void getRequestUpdateSectionData_MyZeroPageGiven_ThrowsDataNotFoundException()
            throws
            BadRequestException,
            DataNotFoundException {

        mockSupervisionData();

        thrown.expect(DataNotFoundException.class);
        dashboardRequestUpdateService.getRequestUpdateSectionData(
                undeletedSupervisorUsername, ServiceConstant.TAB_MY, 0);
    }

    @Test
    public void getOthersRequestList() {

    }

    private void mockSupervisionData() {

        List< SupervisionModel > supervisions = new ArrayList<>();

        for (final String undeletedEmployeeUsername : undeletedEmployeeUsernames) {
            SupervisionModel supervision = new SupervisionModel();

            supervision.setEmployeeUsername(undeletedEmployeeUsername);
            supervision.setSupervisorUsername(undeletedSupervisorUsername);
            supervision.setDeleted(false);
            supervision.setCreatedBy("admin");
            supervision.setCreatedDate(new Date());
            supervision.setUpdatedBy("admin");
            supervision.setUpdatedDate(new Date());

            supervisions.add(supervision);
        }


        when(employeeUtilServiceApi
                     .findAllByDeletedIsFalseAndSupervisorUsername(undeletedSupervisorUsername)).thenReturn(
                supervisions);
    }

}
