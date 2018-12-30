package com.oasis.service.test.login;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.EmployeeRepository;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private LoginServiceImpl loginService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeDetailServiceApi employeeDetailServiceApi;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Mock
    private RoleDeterminer roleDeterminer;

    @Test
    public void getLoginData_EmployeeWithUsernameJonathanExistsInDatabase_LoginDataIsComplete()
            throws
            DataNotFoundException,
            BadRequestException {

        final String username = "jonathan";
        generateEmployeeData();

        Map< String, String > loginData = loginService.getLoginData(username);

        assertEquals("jonathan", loginData.get("username"));
        assertEquals("Jonathan", loginData.get("name"));
        assertEquals(
                "http://localhost:8085/oasis/api/employees/jonathan/photo_not_found?extension=jpg",
                loginData.get("photo")
        );
        assertEquals(RoleConstant.ROLE_EMPLOYEE, loginData.get("role"));

        verify(employeeUtilServiceApi, times(3)).findByDeletedIsFalseAndUsername("jonathan");
        verify(employeeDetailServiceApi, times(1)).getEmployeeDetailPhoto("jonathan", null);
        verify(roleDeterminer, times(1)).determineRole("jonathan");

        verifyNoMoreInteractions(employeeUtilServiceApi);
        verifyNoMoreInteractions(employeeDetailServiceApi);
        verifyNoMoreInteractions(roleDeterminer);
    }

    private void generateEmployeeData()
            throws
            DataNotFoundException {

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername("jonathan");
        employee.setName("Jonathan Wijaya");
        employee.setDeleted(false);

        when(employeeUtilServiceApi.findByDeletedIsFalseAndUsername("jonathan")).thenReturn(employee);
        when(employeeDetailServiceApi.getEmployeeDetailPhoto("jonathan", null)).thenReturn(
                "http://localhost:8085/oasis/api/employees/jonathan/photo_not_found?extension=jpg"
        );
        when(roleDeterminer.determineRole("jonathan")).thenReturn(RoleConstant.ROLE_EMPLOYEE);
    }

}
