package com.oasis.service.api.employees;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeSaveServiceApi {

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

    List< String > getEmployeesUsernamesForSupervisorSelection(
            final String adminUsername, final String username
    )
            throws
            BadRequestException,
            DataNotFoundException;

}
