package com.oasis.service.implementation.employees;

import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeDetailServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.tool.helper.ImageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeeDetailServiceImpl
        implements EmployeeDetailServiceApi {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;

    @Autowired
    private ImageHelper imageHelper;



    @Override
    @Cacheable(value = "employeeDetailData",
               key = "#username")
    public EmployeeModel getEmployeeDetailData(
            final String username
    )
            throws
            DataNotFoundException,
            UnauthorizedOperationException {

        final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);

        final boolean employeeWithUsernameExists = ( employee != null );

        if (!employeeWithUsernameExists) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else if (employeeUtilServiceApi.isEmployeeTopAdministrator(username)) {
            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
        } else {
            return employee;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public EmployeeModel getEmployeeSupervisorData(
            final String username
    )
            throws
            DataNotFoundException {

        final SupervisionModel supervision = supervisionRepository.findByDeletedIsFalseAndEmployeeUsernameEquals(
                username);
        final boolean employeeIsAdministrator = adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(
                username);

        final boolean noSupervisionForEmployeeWithUsername = ( supervision == null );

        if (noSupervisionForEmployeeWithUsername && !employeeIsAdministrator) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final boolean employeeIsTopAdministrator = noSupervisionForEmployeeWithUsername && employeeIsAdministrator;

            if (employeeIsTopAdministrator) {
                return null;
            }

            return employeeRepository.findByDeletedIsFalseAndUsernameEquals(supervision.getSupervisorUsername());
        }
    }

    @Override
    public String getEmployeeDetailPhoto(
            final String username, final String photoLocation
    ) {

        final boolean validPhotoLocation = ( photoLocation != null && !photoLocation.isEmpty() );

        if (validPhotoLocation) {
            final File photo = new File(photoLocation);

            if (photo.exists() && Files.exists(photo.toPath())) {
                return "http://localhost:8085/oasis/api/employees/" + username + "/" +
                       username.concat("?extension=").concat(imageHelper.getExtensionFromFileName(photo.getName()));
            }
        }

        return "http://localhost:8085/oasis/api/employees/" + username + "/photo_not_found".concat("?extension=jpg");
    }

}
