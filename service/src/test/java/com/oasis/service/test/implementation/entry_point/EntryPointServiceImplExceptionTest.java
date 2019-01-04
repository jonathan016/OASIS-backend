package com.oasis.service.test.implementation.entry_point;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.service.api.employees.EmployeeDetailServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.implementation.entry_point.EntryPointServiceImpl;
import com.oasis.service.tool.helper.RoleDeterminer;
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
public class EntryPointServiceImplExceptionTest {

    private final String[] notDeletedUsernames = new String[]{ "o.s.kindy", "r.sianipar", "d.william" };
    private final String[] deletedUsernames = new String[]{ "a.p.lim", "s.dewanto", "a.wijaya" };
    private final String nonExistingUsername = "j.meyer";
    private final String[] notDeletedNames = new String[]{ "Oliver Sebastian Kindy", "Rani Sianipar", "David William" };
    private final String[] deletedNames = new String[]{ "Andreas Pangestu Lim", "Stephen Dewanto", "Almond Wijaya" };
    private final String[] roles = new String[]{ RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR,
                                                 RoleConstant.ROLE_EMPLOYEE };
    private final String[] noPhotoDirectories = new String[]{ "", "", "" };
    private final String[] validPhotoDirectories = new String[]{ "C:\\oasis\\images\\employees\\o.s.kindy.jpg",
                                                                 "C:\\oasis\\images\\employees\\r.sianipar.jpg",
                                                                 "C:\\oasis\\images\\employees\\d.william.png",
                                                                 "C:\\oasis\\images\\employees\\a.p.lim.jpg",
                                                                 "C:\\oasis\\images\\employees\\s.dewanto.png",
                                                                 "C:\\oasis\\images\\employees\\a.wijaya.png" };
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private EntryPointServiceImpl entryPointService;
    @Mock
    private EmployeeDetailServiceApi employeeDetailServiceApi;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Mock
    private RoleDeterminer roleDeterminer;

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndNoPhotoDirectoryAndWithRoleAdministratorInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 0 ], deletedNames[ 0 ], true, noPhotoDirectories[ 0 ],
                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 0 ] +
                              "/photo_not_found?extension=jpg", roles[ 0 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 0 ]);
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndNoPhotoDirectoryAndWithRoleSuperiorInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 1 ], deletedNames[ 1 ], true, noPhotoDirectories[ 1 ],
                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 1 ] +
                              "/photo_not_found?extension=jpg", roles[ 1 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 1 ]);
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndNoPhotoDirectoryAndWithRoleEmployeeInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 2 ], deletedNames[ 2 ], true, noPhotoDirectories[ 2 ],
                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 2 ] +
                              "/photo_not_found?extension=jpg", roles[ 2 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 2 ]);
    }

    @Test
    public void getLoginData_NoEmployeeWithUsernameInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(notDeletedUsernames[ 0 ], notDeletedNames[ 0 ], false, noPhotoDirectories[ 0 ],
                              "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ 0 ] +
                              "/photo_not_found?extension=jpg", roles[ 0 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(nonExistingUsername);
    }

    @Test
    public void getLoginData_NoEmployeeInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(nonExistingUsername);
    }

    @Test
    public void getLoginData_InvalidUsername_ThrowsBadRequestException()
            throws
            DataNotFoundException,
            BadRequestException {

        thrown.expect(BadRequestException.class);
        entryPointService.getLoginData(null);
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndValidPhotoDirectoryAndWithRoleAdministratorInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 0 ], deletedNames[ 0 ], true, validPhotoDirectories[ 3 ],

                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 0 ] +
                              "/" + deletedUsernames[ 0 ] + "?extension=" +
                              validPhotoDirectories[ 3 ].substring(validPhotoDirectories[ 3 ].lastIndexOf(".") + 1),
                              roles[ 0 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 0 ]);
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndValidPhotoDirectoryAndWithRoleSuperiorInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 1 ], deletedNames[ 1 ], true, validPhotoDirectories[ 4 ],
                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 1 ] +
                              "/" + deletedUsernames[ 1 ] + "?extension=" +
                              validPhotoDirectories[ 4 ].substring(validPhotoDirectories[ 4 ].lastIndexOf(".") + 1),
                              roles[ 1 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 1 ]);
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsDeletedAndNValidPhotoDirectoryAndWithRoleEmployeeInDatabase_ThrowsDataNotFoundException()
            throws
            DataNotFoundException,
            BadRequestException {

        mockLoginEmployeeData(deletedUsernames[ 2 ], deletedNames[ 2 ], true, validPhotoDirectories[ 5 ],
                              "http://localhost:8085/oasis/api/employees/" + deletedUsernames[ 2 ] +
                              "/" + deletedUsernames[ 2 ] + "?extension=" +
                              validPhotoDirectories[ 5 ].substring(validPhotoDirectories[ 5 ].lastIndexOf(".") + 1),
                              roles[ 2 ]
        );

        thrown.expect(DataNotFoundException.class);
        entryPointService.getLoginData(deletedUsernames[ 2 ]);
    }

    private void mockLoginEmployeeData(
            final String username, final String name, final boolean deleted, final String photoLocation,
            final String mockedPhotoUrl, final String mockedRole
    )
            throws
            DataNotFoundException {

        EmployeeModel employee = new EmployeeModel();

        employee.setUsername(username);
        employee.setName(name);
        employee.setPhoto(photoLocation);
        employee.setDeleted(deleted);

        if (!deleted) {
            when(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(username)).thenReturn(employee);
        } else {
            when(employeeUtilServiceApi.findByDeletedIsFalseAndUsername(username)).thenReturn(null);
        }
        when(employeeDetailServiceApi.getEmployeeDetailPhoto(username, photoLocation)).thenReturn(mockedPhotoUrl);
        when(roleDeterminer.determineRole(username)).thenReturn(mockedRole);
    }

}
