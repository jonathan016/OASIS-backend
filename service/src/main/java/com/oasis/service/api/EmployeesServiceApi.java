package com.oasis.service.api;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.DuplicateDataException;
import com.oasis.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface EmployeesServiceApi {

    List< EmployeeModel > getEmployeesList(
            final String username, final String query, final int page, String sort
    )
            throws
            BadRequestException,
            DataNotFoundException;

    Set< EmployeeModel > getSortedEmployeesList(
            final String username, final int page, final String sort
    );

    Set< EmployeeModel > getSortedEmployeesListFromQuery(
            final int page, final String query, final String sort
    );

    int getEmployeesCount(
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

    String getEmployeeDetailPhoto(
            final String username, final String photoDirectory
    );

    List< String > getEmployeesUsernames(
            String username
    )
            throws
            BadRequestException;

    byte[] getEmployeePhoto(
            final String username, final String photoName, final String extension
    );

    List< String > getEmployeePhotos(
            final List< EmployeeModel > employees
    );

    EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException;

    String saveEmployee(
            final MultipartFile photo, final String username, final EmployeeModel employee,
            final String supervisorUsername, final boolean isAddOperation
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            DuplicateDataException,
            BadRequestException;

    String generateEmployeeUsername(
            final String name, final String dob
    );

    String generateEmployeeDefaultPassword(
            final String dob
    );

    String getSupervisionId(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    )
            throws
            DataNotFoundException;

    void createSupervision(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    );

    boolean checkCyclicSupervisingExists(
            final String employeeUsername, final String supervisorUsername
    );

    void savePhoto(
            final MultipartFile employeePhoto, final String sku
    );

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

}
