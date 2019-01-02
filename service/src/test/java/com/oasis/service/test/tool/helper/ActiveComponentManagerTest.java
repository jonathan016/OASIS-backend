package com.oasis.service.test.tool.helper;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.tool.helper.ActiveComponentManager;
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
public class ActiveComponentManagerTest {

    private final String[] usernames = new String[]{ "admin", "admin1", "superior", "employee" };
    private final String[] roles = new String[]{ RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_ADMINISTRATOR,
                                                 RoleConstant.ROLE_SUPERIOR, RoleConstant.ROLE_EMPLOYEE };

    @InjectMocks
    private ActiveComponentManager activeComponentManager;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;

    @Before
    public void setUp()
            throws
            Exception {

        when(employeeUtilServiceApi.isEmployeeTopAdministrator(usernames[ 0 ])).thenReturn(true);
        when(employeeUtilServiceApi.isEmployeeTopAdministrator(usernames[ 1 ])).thenReturn(false);
        when(employeeUtilServiceApi.isEmployeeTopAdministrator(usernames[ 2 ])).thenReturn(false);
        when(employeeUtilServiceApi.isEmployeeTopAdministrator(usernames[ 3 ])).thenReturn(false);
    }

    @Test
    public void getDashboardActiveComponents_RoleAdministratorAndTopAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnNewRequestChangeTab", false);
        activeComponents.put("sectionNewRequestOthers", true);
        activeComponents.put("sectionNewRequestMy", false);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getDashboardActiveComponents(usernames[ 0 ], roles[ 0 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 0 ]);
    }

    @Test
    public void getDashboardActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnNewRequestChangeTab", true);
        activeComponents.put("sectionNewRequestOthers", true);
        activeComponents.put("sectionNewRequestMy", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getDashboardActiveComponents(usernames[ 1 ], roles[ 1 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 1 ]);
    }

    @Test
    public void getDashboardActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnNewRequestChangeTab", true);
        activeComponents.put("sectionNewRequestOthers", true);
        activeComponents.put("sectionNewRequestMy", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getDashboardActiveComponents(usernames[ 2 ], roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 2 ]);
    }

    @Test
    public void getDashboardActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnNewRequestChangeTab", false);
        activeComponents.put("sectionNewRequestOthers", false);
        activeComponents.put("sectionNewRequestMy", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getDashboardActiveComponents(usernames[ 3 ], roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 3 ]);
    }

    @Test
    public void getAssetsListActiveComponents_RoleAdministratorAndTopAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetAddNew", true);
        activeComponents.put("btnAssetEdit", true);
        activeComponents.put("btnAssetDeleteBulk", true);
        activeComponents.put("btnAssetRequest", false);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetsListActiveComponents(usernames[ 0 ], roles[ 0 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 0 ]);
    }

    @Test
    public void getAssetsListActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetAddNew", true);
        activeComponents.put("btnAssetEdit", true);
        activeComponents.put("btnAssetDeleteBulk", true);
        activeComponents.put("btnAssetRequest", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetsListActiveComponents(usernames[ 1 ], roles[ 1 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 1 ]);
    }

    @Test
    public void getAssetsListActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetAddNew", false);
        activeComponents.put("btnAssetEdit", false);
        activeComponents.put("btnAssetDeleteBulk", false);
        activeComponents.put("btnAssetRequest", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetsListActiveComponents(usernames[ 2 ], roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 2 ]);
    }

    @Test
    public void getAssetsListActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetAddNew", false);
        activeComponents.put("btnAssetEdit", false);
        activeComponents.put("btnAssetDeleteBulk", false);
        activeComponents.put("btnAssetRequest", true);

        Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetsListActiveComponents(usernames[ 3 ], roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 3 ]);
    }

    @Test
    public void getAssetDetailActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        for (int i = 0; i < 2; i++) {
            Map< String, Boolean > activeComponents = new HashMap<>();

            activeComponents.put("btnAssetDetailEditDelete", true);

            final Map< String, Boolean > producedActiveComponents =
                    activeComponentManager.getAssetDetailActiveComponents(roles[ i ]);

            assertEquals(activeComponents, producedActiveComponents);
        }
    }

    @Test
    public void getAssetDetailActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetDetailEditDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetDetailActiveComponents(roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getAssetDetailActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnAssetDetailEditDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getAssetDetailActiveComponents(roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getEmployeesListActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        for (int i = 0; i < 2; i++) {
            Map< String, Boolean > activeComponents = new HashMap<>();

            activeComponents.put("sectionEmployeeUpper", false);
            activeComponents.put("btnEmployeeAdd", true);
            activeComponents.put("btnEmployeeEdit", true);
            activeComponents.put("btnEmployeeDelete", true);

            final Map< String, Boolean > producedActiveComponents =
                    activeComponentManager.getEmployeesListActiveComponents(roles[ i ]);

            assertEquals(activeComponents, producedActiveComponents);
        }
    }

    @Test
    public void getEmployeesListActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sectionEmployeeUpper", true);
        activeComponents.put("btnEmployeeAdd", false);
        activeComponents.put("btnEmployeeEdit", false);
        activeComponents.put("btnEmployeeDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getEmployeesListActiveComponents(roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getEmployeesListActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sectionEmployeeUpper", true);
        activeComponents.put("btnEmployeeAdd", false);
        activeComponents.put("btnEmployeeEdit", false);
        activeComponents.put("btnEmployeeDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getEmployeesListActiveComponents(roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getEmployeeDetailActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        for (int i = 0; i < 2; i++) {
            Map< String, Boolean > activeComponents = new HashMap<>();

            activeComponents.put("btnEmployeeDetailEditDelete", true);

            final Map< String, Boolean > producedActiveComponents =
                    activeComponentManager.getEmployeeDetailActiveComponents(roles[ i ]);

            assertEquals(activeComponents, producedActiveComponents);
        }
    }

    @Test
    public void getEmployeeDetailActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnEmployeeDetailEditDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getEmployeeDetailActiveComponents(roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getEmployeeDetailActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("btnEmployeeDetailEditDelete", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getEmployeeDetailActiveComponents(roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);
    }

    @Test
    public void getSideBarActiveComponents_RoleAdministratorAndTopAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sidebarRequestMy", false);
        activeComponents.put("sidebarRequestOther", true);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getSideBarActiveComponents(usernames[ 0 ], roles[ 0 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 0 ]);
    }

    @Test
    public void getSideBarActiveComponents_RoleAdministrator_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sidebarRequestMy", true);
        activeComponents.put("sidebarRequestOther", true);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getSideBarActiveComponents(usernames[ 1 ], roles[ 1 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 1 ]);
    }

    @Test
    public void getSideBarActiveComponents_RoleSuperior_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sidebarRequestMy", true);
        activeComponents.put("sidebarRequestOther", true);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getSideBarActiveComponents(usernames[ 2 ], roles[ 2 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 2 ]);
    }

    @Test
    public void getSideBarActiveComponents_RoleEmployee_ReturnsCompleteMap() {

        Map< String, Boolean > activeComponents = new HashMap<>();

        activeComponents.put("sidebarRequestMy", true);
        activeComponents.put("sidebarRequestOther", false);

        final Map< String, Boolean > producedActiveComponents =
                activeComponentManager.getSideBarActiveComponents(usernames[ 3 ], roles[ 3 ]);

        assertEquals(activeComponents, producedActiveComponents);

        verify(employeeUtilServiceApi, times(1)).isEmployeeTopAdministrator(usernames[ 3 ]);
    }

    @After
    public void tearDown()
            throws
            Exception {

        verifyNoMoreInteractions(employeeUtilServiceApi);
    }

}