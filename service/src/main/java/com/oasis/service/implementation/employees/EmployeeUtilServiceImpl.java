package com.oasis.service.implementation.employees;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.AdminModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.tool.constant.ImageDirectoryConstant;
import com.oasis.tool.constant.RoleConstant;
import com.oasis.tool.helper.RoleDeterminer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
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

    @Autowired
    private RoleDeterminer roleDeterminer;



    @Override
    @SuppressWarnings("PointlessBooleanExpression")
    public void demotePreviousSupervisorFromAdminIfNecessary(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername,
            final List< SupervisionModel > supervisions
    ) {

        boolean hadSupervisingEmployees = false;

        for (SupervisionModel supervision : supervisions) {
            final boolean correctAssumptionOfNotHavingSupervisingEmployees = ( hadSupervisingEmployees == false );
            final boolean supervisedEmployeeFromSupervisionSupervises = supervisionRepository
                    .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(
                            supervision.getEmployeeUsername());

            if (correctAssumptionOfNotHavingSupervisingEmployees && supervisedEmployeeFromSupervisionSupervises) {
                hadSupervisingEmployees = true;

                AdminModel demotedAdmin = adminRepository.findByDeletedIsFalseAndUsernameEquals(oldSupervisorUsername);

                demotedAdmin.setDeleted(true);

                adminRepository.save(demotedAdmin);
            }
            supervision.setSupervisorUsername(newSupervisorUsername);
            supervision.setUpdatedDate(new Date());
            supervision.setUpdatedBy(adminUsername);

            supervisionRepository.save(supervision);
        }
    }

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

            final boolean photoNameIsImageNotFound = photoName.equals("photo_not_found");
            final boolean correctExtensionForPhoto = file.getName().endsWith(extension);

            if (!correctExtensionForPhoto || photoNameIsImageNotFound) {
                file = new File(ImageDirectoryConstant.STATIC_IMAGE_DIRECTORY.concat(File.separator)
                                                                             .concat("photo_not_found.jpg"));
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
    public void updateSupervisorDataOnEmployeeDataModification(
            final String adminUsername, final String employeeUsername, final String selectedSupervisorUsername,
            final boolean addEmployeeOperation
    )
            throws
            DataNotFoundException {

        if (addEmployeeOperation) {
            createSupervision(employeeUsername, selectedSupervisorUsername, adminUsername);
        }

        final SupervisionModel supervisionOfSelectedSupervisor = supervisionRepository
                .findByDeletedIsFalseAndEmployeeUsernameEquals(selectedSupervisorUsername);

        if (supervisionOfSelectedSupervisor != null) {
            final String supervisorUsernameOfSelectedSupervisor = supervisionOfSelectedSupervisor
                    .getSupervisorUsername();

            if (roleDeterminer.determineRole(supervisorUsernameOfSelectedSupervisor).equals(RoleConstant
                                                                                                    .ROLE_SUPERIOR)) {
                AdminModel demotedAdmin = adminRepository.findByDeletedIsTrueAndUsernameEquals(
                        supervisorUsernameOfSelectedSupervisor);

                if (demotedAdmin != null) {
                    final EmployeeModel supervisorOfSelectedSupervisor = employeeRepository
                            .findByDeletedIsFalseAndUsernameEquals(supervisorUsernameOfSelectedSupervisor);

                    demotedAdmin.setPassword(supervisorOfSelectedSupervisor.getPassword());
                    demotedAdmin.setDeleted(false);
                    demotedAdmin.setUpdatedBy(adminUsername);
                    demotedAdmin.setUpdatedDate(new Date());

                    adminRepository.save(demotedAdmin);
                } else {
                    final EmployeeModel supervisorOfSelectedSupervisor = employeeRepository
                            .findByDeletedIsFalseAndUsernameEquals(supervisorUsernameOfSelectedSupervisor);

                    AdminModel newAdmin = new AdminModel();

                    newAdmin.setUsername(supervisorOfSelectedSupervisor.getUsername());
                    newAdmin.setPassword(supervisorOfSelectedSupervisor.getPassword());
                    newAdmin.setDeleted(false);
                    newAdmin.setCreatedBy(adminUsername);
                    newAdmin.setCreatedDate(new Date());
                    newAdmin.setUpdatedBy(adminUsername);
                    newAdmin.setUpdatedDate(new Date());

                    adminRepository.save(newAdmin);
                }
            }

            if (!addEmployeeOperation) {
                final SupervisionModel supervisionOfEmployeeUsername = supervisionRepository
                        .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);
                final String supervisorUsernameOfEmployee = supervisionOfEmployeeUsername.getSupervisorUsername();

                final SupervisionModel supervisionOfSupervisor = supervisionRepository
                        .findByDeletedIsFalseAndEmployeeUsernameEquals(supervisorUsernameOfEmployee);

                if (supervisionOfSupervisor != null) {
                    List< SupervisionModel > supervisionsOfSelectedSupervisorSupervisor = supervisionRepository
                            .findAllByDeletedIsFalseAndSupervisorUsernameEquals(supervisionOfSupervisor
                                                                                        .getSupervisorUsername());

                    boolean hasSupervisingEmployee = false;

                    for (final SupervisionModel supervision : supervisionsOfSelectedSupervisorSupervisor) {
                        if (supervision.getEmployeeUsername().equals(supervisorUsernameOfEmployee)) {
                            continue;
                        }
                        if (!roleDeterminer.determineRole(supervision.getEmployeeUsername()).equals(RoleConstant
                                                                                                            .ROLE_EMPLOYEE)) {
                            hasSupervisingEmployee = true;
                            break;
                        }
                    }

                    if (!hasSupervisingEmployee) {
                        AdminModel toBeDemotedAdmin = adminRepository
                                .findByDeletedIsFalseAndUsernameEquals(supervisionOfSupervisor
                                                                               .getSupervisorUsername());

                        if (employeeRepository.findByDeletedIsFalseAndUsernameEquals(supervisionOfSupervisor
                                                                                             .getSupervisorUsername())
                                              .getSupervisionId() != null) {
                            logger.info("Supervisor: " + supervisionOfSupervisor.getSupervisorUsername());
                            toBeDemotedAdmin.setDeleted(true);
                            toBeDemotedAdmin.setUpdatedBy(adminUsername);
                            toBeDemotedAdmin.setUpdatedDate(new Date());

                            adminRepository.save(toBeDemotedAdmin);
                        }
                    }
                }
            }
        }

        if (!addEmployeeOperation) {
            SupervisionModel supervision = supervisionRepository
                    .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);

            supervision.setSupervisorUsername(selectedSupervisorUsername);
            supervision.setUpdatedBy(adminUsername);
            supervision.setUpdatedDate(new Date());

            supervisionRepository.save(supervision);
        }
    }

    @Override
    public boolean isEmployeeTopAdministrator(final String username) {

        return employeeRepository.findByDeletedIsFalseAndUsernameEquals(username).getSupervisionId() == null;
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

    private void createSupervision(
            final String employeeUsername, final String supervisorUsername, final String adminUsername
    ) {

        SupervisionModel supervision = new SupervisionModel();

        supervision.setSupervisorUsername(supervisorUsername);
        supervision.setEmployeeUsername(employeeUsername);
        supervision.setCreatedDate(new Date());
        supervision.setUpdatedDate(new Date());
        supervision.setCreatedBy(adminUsername);
        supervision.setUpdatedBy(adminUsername);

        supervisionRepository.save(supervision);
    }

}
