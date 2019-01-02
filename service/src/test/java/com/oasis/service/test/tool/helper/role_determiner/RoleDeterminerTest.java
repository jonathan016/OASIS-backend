package com.oasis.service.test.tool.helper.role_determiner;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.tool.helper.RoleDeterminer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class RoleDeterminerTest {

    private final String[] usernames = new String[]{ "admin", "superior", "employee" };
    private final String unregisteredUsername = "unregistered";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private RoleDeterminer roleDeterminer;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SupervisionRepository supervisionRepository;

    @Before
    public void setUp()
            throws
            Exception {

        when(adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(usernames[ 0 ])).thenReturn(true);
        when(adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(unregisteredUsername)).thenReturn(false);
        when(employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(usernames[ 1 ])).thenReturn(true);
        when(employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(usernames[ 2 ])).thenReturn(true);
        when(employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(unregisteredUsername)).thenReturn(
                false);
        when(supervisionRepository
                     .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(usernames[ 1 ])).thenReturn(
                true);
        when(supervisionRepository
                     .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(usernames[ 2 ])).thenReturn(
                false);
    }

    @Test
    public void determineRole_UsernameHasAdministratorRole_ReturnsADMINISTRATOR()
            throws
            DataNotFoundException {

        when(adminRepository.existsAdminModelByDeletedIsTrueAndUsernameEquals(usernames[ 0 ])).thenReturn(true);

        final String role = roleDeterminer.determineRole(usernames[ 0 ]);

        assertEquals(RoleConstant.ROLE_ADMINISTRATOR, role);

        verify(adminRepository, times(1)).existsAdminModelByDeletedIsFalseAndUsernameEquals(usernames[ 0 ]);
        verify(employeeRepository, times(0)).existsEmployeeModelByDeletedIsFalseAndUsernameEquals(usernames[ 0 ]);
        verify(supervisionRepository, times(0)).existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                usernames[ 0 ]);
    }

    @Test
    public void determineRole_UsernameHasSuperiorRole_ReturnsSUPERIOR()
            throws
            DataNotFoundException {

        final String role = roleDeterminer.determineRole(usernames[ 1 ]);

        assertEquals(RoleConstant.ROLE_SUPERIOR, role);

        verify(adminRepository, times(1)).existsAdminModelByDeletedIsFalseAndUsernameEquals(usernames[ 1 ]);
        verify(employeeRepository, times(1)).existsEmployeeModelByDeletedIsFalseAndUsernameEquals(usernames[ 1 ]);
        verify(supervisionRepository, times(1)).existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                usernames[ 1 ]);
    }

    @Test
    public void determineRole_UsernameHasEmployeeRole_ReturnsEMPLOYEE()
            throws
            DataNotFoundException {

        final String role = roleDeterminer.determineRole(usernames[ 2 ]);

        assertEquals(RoleConstant.ROLE_EMPLOYEE, role);

        verify(adminRepository, times(1)).existsAdminModelByDeletedIsFalseAndUsernameEquals(usernames[ 2 ]);
        verify(employeeRepository, times(1)).existsEmployeeModelByDeletedIsFalseAndUsernameEquals(usernames[ 2 ]);
        verify(supervisionRepository, times(1)).existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                usernames[ 2 ]);
    }

    @After
    public void tearDown()
            throws
            Exception {

        verifyNoMoreInteractions(adminRepository);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(supervisionRepository);
    }

}