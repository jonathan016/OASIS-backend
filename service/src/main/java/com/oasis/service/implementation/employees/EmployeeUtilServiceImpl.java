package com.oasis.service.implementation.employees;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.tool.constant.ImageDirectoryConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeeUtilServiceImpl
        implements EmployeeUtilServiceApi {

    private Logger logger = LoggerFactory.getLogger(EmployeeUtilServiceImpl.class);

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;



    @Override
    public byte[] getEmployeePhoto(
            final String username, final String photoName, final String extension
    ) {

        final boolean employeeWithUsernameExists = employeeRepository
                .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username);

        if (!employeeWithUsernameExists) {
            logger.info("Failed to load employee photo as username does not refer any employee in database");
            return new byte[ 0 ];
        } else {
            final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);

            File file = new File(employee.getPhoto());

            final boolean photoNameIsImageNotFound = photoName.equals("image_not_found");
            final boolean correctExtensionForPhoto = file.getName().endsWith(extension);

            if (!correctExtensionForPhoto || photoNameIsImageNotFound) {
                file = new File(ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator)
                                                                             .concat("image_not_found.jpeg"));
            }

            final byte[] photo;

            try {
                photo = Files.readAllBytes(file.toPath());
            } catch (IOException | NullPointerException exception) {
                logger.error("Failed to read photo as IOException or NullPointerException occurred with message " +
                             exception.getMessage());
                return new byte[ 0 ];
            }

            return photo;
        }
    }

    @Override
    public EmployeeModel findByDeletedIsFalseAndUsername(final String username) {

        return employeeRepository.findByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsAdminModelByDeletedIsFalseAndUsernameEquals(final String username) {

        return adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsEmployeeModelByDeletedIsFalseAndUsername(final String username) {

        return employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username);
    }

    @Override
    public boolean existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(final String username) {

        return employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsernameEqualsAndSupervisionIdIsNull(username);
    }

    @Override
    public boolean existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameAndEmployeeUsername(
            final String supervisorUsername, final String employeeUsername
    ) {

        return supervisionRepository
                .existsSupervisionModelByDeletedIsFalseAndSupervisorUsernameEqualsAndEmployeeUsernameEquals(
                        supervisorUsername,
                        employeeUsername
                );
    }

    @Override
    public List< SupervisionModel > findAllByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername) {

        return supervisionRepository.findAllByDeletedIsFalseAndSupervisorUsernameEquals(supervisorUsername);
    }

    @Override
    public boolean existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(final String supervisorUsername) {

        return supervisionRepository.existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                supervisorUsername);
    }

}
