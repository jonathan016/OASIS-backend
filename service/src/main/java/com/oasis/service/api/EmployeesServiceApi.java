package com.oasis.service.api;

import com.oasis.exception.*;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EmployeesServiceApi {

    /*-------------Employees List Methods-------------*/
    Map< String, List< ? > > getEmployeesListData(
            final String username, final String query, final int page, final String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    List< EmployeeModel > getEmployeesList(
            final String username, final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    long getEmployeesCount(
            final String username, final String query
    );

    EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException;

    String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    );

    List< String > getEmployeesUsernamesForSupervisorSelection(
            final String adminUsername, final String username
    )
            throws
            BadRequestException, DataNotFoundException;

    byte[] getEmployeePhoto(
            final String username, final String photoName, final String extension
    );

    EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException;

    /*-------------Save Employee Methods-------------*/
    String saveEmployee(
            final MultipartFile photoGiven, final String username, final EmployeeModel employee,
            final String supervisorUsername, final boolean addEmployeeOperation
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException;

    void changePassword(
            final String username, final String oldPassword, final String newPassword,
            final String newPasswordConfirmation
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException,
            BadRequestException;

    /*-------------Delete Employee Methods-------------*/
    void deleteEmployee(
            final String adminUsername, final String employeeUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

    void changeSupervisorOnPreviousSupervisorDeletion(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException;

    EmployeeModel findByDeletedIsFalseAndUsername(final String username);

    boolean existsAdminModelByDeletedIsFalseAndUsernameEquals(final String username);

    boolean existsEmployeeModelByDeletedIsFalseAndUsername(final String username);

    boolean existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(final String username);

    boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
            final String supervisorUsername, final String employeeUsername
    );

    List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername);

    boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername);

}
