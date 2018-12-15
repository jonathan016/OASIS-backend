package com.oasis.service.api;

import com.oasis.exception.*;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    Set< EmployeeModel > getSortedEmployees(
            final String username, final int page, final String sort
    );

    Set< EmployeeModel > getSortedEmployeesFromQuery(
            final int page, final String query, final String sort
    );

    long getEmployeesCount(
            final String username, final String query, String sort
    );

    List< EmployeeModel > getSupervisorsList(
            final List< EmployeeModel > employees
    );

    EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException;

    String getEmployeeDetailImage(
            final String username, final String photoLocation
    );

    List< String > getEmployeesUsernamesForSupervisorSelection(
            final String username
    )
            throws
            BadRequestException;

    boolean isSafeFromCyclicSupervising(
            final String targetUsername, final List< String > usernames
    );

    byte[] getEmployeeImage(
            final String username, final String photoName, final String extension
    );

    List< String > getEmployeesPhotos(
            final List< EmployeeModel > employees
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

    void updateSupervisorDataOnEmployeeUpdate(
            final String username, final EmployeeModel savedEmployee, final String employeeUsername,
            final String supervisorUsername
    )
            throws
            DataNotFoundException,
            UnauthorizedOperationException;

    void validateAndSaveImage(
            final MultipartFile photoGiven, final boolean addEmployeeOperation, final EmployeeModel savedEmployee
    );

    String generateUsername(
            final String name, final String dobString
    );

    String generateDefaultPassword(
            final String dobString
    );

    String getSupervisionId(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    )
            throws
            DataNotFoundException;

    void createSupervision(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    );

    boolean hasCyclicSupervising(
            final String employeeUsername, final String supervisorUsername
    );

    void savePhoto(
            final MultipartFile photoGiven, final String username
    );

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

    void demotePreviousSupervisorFromAdminIfNecessary(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername,
            final List< SupervisionModel > supervisions
    );

}
