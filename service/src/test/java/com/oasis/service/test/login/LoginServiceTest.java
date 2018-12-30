package com.oasis.service.test.login;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.api.employees.EmployeeDetailServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.implementation.login.LoginServiceImpl;
import com.oasis.tool.constant.RoleConstant;
import com.oasis.tool.helper.RoleDeterminer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginServiceTest {

    private final String[] usernames = new String[]{ "o.s.kindy", "r.sianipar", "d.william", "a.p.lim" };
    private final String[] names = new String[]{ "Oliver Sebastian Kindy", "Rani Sianipar", "David William",
                                                 "Andreas Pangestu Lim" };
    private final boolean[] deletedData = new boolean[]{ false, false, false, true };
    private final String[] roles = new String[]{ RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR,
                                                 RoleConstant.ROLE_EMPLOYEE, RoleConstant.ROLE_EMPLOYEE };
    private final String[] photos = new String[]{ "", "", "", "" };
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private LoginServiceImpl loginService;
    @Mock
    private EmployeeDetailServiceApi employeeDetailServiceApi;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Mock
    private RoleDeterminer roleDeterminer;

    @Test
    public void getLoginData_EmployeeWithUsernameExistsInDatabase_LoginDataIsComplete()
            throws
            DataNotFoundException,
            BadRequestException {

        for (int i = 0; i < usernames.length; i++) {
            generateEmployeeDataWithoutPhoto(usernames[ i ], names[ i ], deletedData[ i ], roles[ i ]);

            Map< String, String > loginData = loginService.getLoginData(usernames[ i ]);

            assertEquals(usernames[ i ], loginData.get("username"));
            assertEquals(names[ i ].split(" ")[ 0 ], loginData.get("name"));
            assertEquals(
                    "http://localhost:8085/oasis/api/employees/" + usernames[ i ] + "/photo_not_found?extension=jpg",
                    loginData.get("photo")
            );
            assertEquals(roles[ i ], loginData.get("role"));

            verify(employeeUtilServiceApi, times(3)).findByDeletedIsFalseAndUsername(usernames[ i ]);
            verify(employeeDetailServiceApi, times(1)).getEmployeeDetailPhoto(usernames[ i ], null);
            verify(roleDeterminer, times(1)).determineRole(usernames[ i ]);

            verifyNoMoreInteractions(employeeUtilServiceApi);
            verifyNoMoreInteractions(employeeDetailServiceApi);
            verifyNoMoreInteractions(roleDeterminer);
        }
    }

    //EmployeeWithUsernameJonathanDoesNotExistInDatabase

    //NoEmployeeInDatabase

    private void generateEmployeeDataWithoutPhoto(
            final String username, final String name, final boolean deleted,
            final String role
    )
            throws
            DataNotFoundException {

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername(username);
        employee.setName(name);
        employee.setDeleted(deleted);

        when(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(username)).thenReturn(employee);
        when(employeeDetailServiceApi.getEmployeeDetailPhoto(username, null)).thenReturn(
                "http://localhost:8085/oasis/api/employees/" + username + "/photo_not_found?extension=jpg"
        );
        when(roleDeterminer.determineRole(username)).thenReturn(role);
    }

    private void generateEmployeeDataWithPhoto(
            final String username, final String name, final boolean deleted,
            final String photoLocation, final String extension, final String role
    )
            throws
            DataNotFoundException {

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername(username);
        employee.setName(name);
        employee.setDeleted(deleted);

        when(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(username)).thenReturn(employee);
        when(employeeDetailServiceApi.getEmployeeDetailPhoto(username, photoLocation)).thenReturn(
                "http://localhost:8085/oasis/api/employees/" + username + "/" + username + "?extension=" + extension
        );
        when(roleDeterminer.determineRole(username)).thenReturn(role);
    }

}
