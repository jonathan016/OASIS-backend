package com.oasis.service.test.tool.helper.role_determiner;

import com.oasis.model.exception.DataNotFoundException;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
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

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class RoleDeterminerExceptionTest {

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
    public void determineRole_UsernameNotInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException {

        thrown.expect(DataNotFoundException.class);
        roleDeterminer.determineRole(unregisteredUsername);
    }

}
