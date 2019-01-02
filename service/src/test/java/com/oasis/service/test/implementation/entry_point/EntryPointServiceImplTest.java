package com.oasis.service.test.implementation.entry_point;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.service.api.employees.EmployeeDetailServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.implementation.entry_point.EntryPointServiceImpl;
import com.oasis.service.tool.helper.RoleDeterminer;
import org.junit.After;
import org.junit.Test;
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

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class EntryPointServiceImplTest {

    private final String[] notDeletedUsernames = new String[]{ "o.s.kindy", "r.sianipar", "d.william" };
    private final String[] notDeletedNames = new String[]{ "Oliver Sebastian Kindy", "Rani Sianipar", "David William" };
    private final String[] roles = new String[]{ RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR,
                                                 RoleConstant.ROLE_EMPLOYEE };
    private final String[] noPhotoDirectories = new String[]{ "", "", "" };
    private final String[] invalidPhotoDirectories = new String[]{ "First Invalid", "asd?", "A:\\a\\bin:" };
    private final String[] validPhotoDirectories = new String[]{ "C:\\oasis\\images\\employees\\o.s.kindy.jpg",
                                                                 "C:\\oasis\\images\\employees\\r.sianipar.jpg",
                                                                 "C:\\oasis\\images\\employees\\d.william.png",
                                                                 "C:\\oasis\\images\\employees\\a.p.lim.jpg",
                                                                 "C:\\oasis\\images\\employees\\s.dewanto.png",
                                                                 "C:\\oasis\\images\\employees\\a.wijaya.png" };
    @InjectMocks
    private EntryPointServiceImpl entryPointService;
    @Mock
    private EmployeeDetailServiceApi employeeDetailServiceApi;
    @Mock
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Mock
    private RoleDeterminer roleDeterminer;

    @Test
    public void getLoginData_EmployeeWithUsernameIsNotDeletedAndNoPhotoDirectoryInDatabase_ReturnsCompleteLoginData()
            throws
            DataNotFoundException,
            BadRequestException {

        for (int i = 0; i < notDeletedUsernames.length; i++) {
            mockLoginEmployeeData(notDeletedUsernames[ i ], notDeletedNames[ i ], false, noPhotoDirectories[ i ],
                                  "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                                  "/photo_not_found?extension=jpg", roles[ i ]
            );

            Map< String, String > loginData = entryPointService.getLoginData(notDeletedUsernames[ i ]);

            assertEquals(notDeletedUsernames[ i ], loginData.get("username"));
            assertEquals(notDeletedNames[ i ].split(" ")[ 0 ], loginData.get("name"));
            assertEquals(
                    "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                    "/photo_not_found?extension=jpg",
                    loginData.get("photo")
            );
            assertEquals(roles[ i ], loginData.get("role"));

            verify(employeeUtilServiceApi, times(3)).findByDeletedIsFalseAndUsername(notDeletedUsernames[ i ]);
            verify(employeeDetailServiceApi, times(1)).getEmployeeDetailPhoto(
                    notDeletedUsernames[ i ],
                    noPhotoDirectories[ i ]
            );
            verify(roleDeterminer, times(1)).determineRole(notDeletedUsernames[ i ]);
        }
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsNotDeletedAndInvalidPhotoDirectoryInDatabase_ReturnsCompleteLoginData()
            throws
            DataNotFoundException,
            BadRequestException {

        for (int i = 0; i < notDeletedUsernames.length; i++) {
            mockLoginEmployeeData(notDeletedUsernames[ i ], notDeletedNames[ i ], false, invalidPhotoDirectories[ i ],
                                  "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                                  "/photo_not_found?extension=jpg", roles[ i ]
            );

            Map< String, String > loginData = entryPointService.getLoginData(notDeletedUsernames[ i ]);

            assertEquals(notDeletedUsernames[ i ], loginData.get("username"));
            assertEquals(notDeletedNames[ i ].split(" ")[ 0 ], loginData.get("name"));
            assertEquals(
                    "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                    "/photo_not_found?extension=jpg",
                    loginData.get("photo")
            );
            assertEquals(roles[ i ], loginData.get("role"));

            verify(employeeUtilServiceApi, times(3)).findByDeletedIsFalseAndUsername(notDeletedUsernames[ i ]);
            verify(employeeDetailServiceApi, times(1)).getEmployeeDetailPhoto(
                    notDeletedUsernames[ i ],
                    invalidPhotoDirectories[ i ]
            );
            verify(roleDeterminer, times(1)).determineRole(notDeletedUsernames[ i ]);
        }
    }

    @Test
    public void getLoginData_EmployeeWithUsernameIsNotDeletedAndValidPhotoDirectoryInDatabase_ReturnsCompleteLoginData()
            throws
            DataNotFoundException,
            BadRequestException {

        for (int i = 0; i < notDeletedUsernames.length; i++) {
            mockLoginEmployeeData(notDeletedUsernames[ i ], notDeletedNames[ i ], false, validPhotoDirectories[ i ],
                                  "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                                  "/" + notDeletedUsernames[ i ] + "?extension=" +
                                  validPhotoDirectories[ i ].substring(validPhotoDirectories[ i ].lastIndexOf(".") + 1),
                                  roles[ i ]
            );

            Map< String, String > loginData = entryPointService.getLoginData(notDeletedUsernames[ i ]);

            assertEquals(notDeletedUsernames[ i ], loginData.get("username"));
            assertEquals(notDeletedNames[ i ].split(" ")[ 0 ], loginData.get("name"));
            assertEquals(
                    "http://localhost:8085/oasis/api/employees/" + notDeletedUsernames[ i ] +
                    "/" + notDeletedUsernames[ i ] + "?extension=" +
                    validPhotoDirectories[ i ].substring(validPhotoDirectories[ i ].lastIndexOf(".") + 1),
                    loginData.get("photo")
            );
            assertEquals(roles[ i ], loginData.get("role"));

            verify(employeeUtilServiceApi, times(3)).findByDeletedIsFalseAndUsername(notDeletedUsernames[ i ]);
            verify(employeeDetailServiceApi, times(1)).getEmployeeDetailPhoto(
                    notDeletedUsernames[ i ],
                    validPhotoDirectories[ i ]
            );
            verify(roleDeterminer, times(1)).determineRole(notDeletedUsernames[ i ]);
        }
    }

    @After
    public void tearDown()
            throws
            Exception {

        verifyNoMoreInteractions(employeeUtilServiceApi);
        verifyNoMoreInteractions(employeeDetailServiceApi);
        verifyNoMoreInteractions(roleDeterminer);
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