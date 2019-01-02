package com.oasis.service.implementation.employees;

import com.oasis.model.constant.service_constant.RoleConstant;
import com.oasis.model.constant.service_constant.StatusConstant;
import com.oasis.model.entity.AdminModel;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.RequestModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.model.exception.BadRequestException;
import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.exception.UnauthorizedOperationException;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.api.employees.EmployeeDeleteServiceApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.api.requests.RequestUtilServiceApi;
import com.oasis.service.tool.helper.RoleDeterminer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.INCORRECT_PARAMETER;
import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class EmployeeDeleteServiceImpl
        implements EmployeeDeleteServiceApi {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;
    @Autowired
    private RequestUtilServiceApi requestUtilServiceApi;

    @Autowired
    private RoleDeterminer roleDeterminer;



    @Override
    @CacheEvict(value = { "employeesListData", "employeeDetailData" },
                allEntries = true)
    public void deleteEmployee(
            final String adminUsername, final String employeeUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        final boolean emptyEmployeeUsernameGiven = employeeUsername.isEmpty();
        final boolean emptyAdministratorUsernameGiven = adminUsername.isEmpty();

        if (emptyAdministratorUsernameGiven || emptyEmployeeUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                                                             .equals(RoleConstant.ROLE_ADMINISTRATOR);
            final boolean selfDeletionAttempt = employeeUsername.equals(adminUsername);
            final boolean employeeWithEmployeeUsernameStillSupervises = supervisionRepository
                    .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(employeeUsername);
            final boolean allDeliveredAssetsHaveBeenReturned = requestUtilServiceApi
                    .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_DELIVERED).isEmpty();
            final boolean targetEmployeeIsTopAdministrator =
                    employeeUtilServiceApi.isEmployeeTopAdministrator(employeeUsername);

            if (!validAdministrator || selfDeletionAttempt || employeeWithEmployeeUsernameStillSupervises ||
                !allDeliveredAssetsHaveBeenReturned || targetEmployeeIsTopAdministrator) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                EmployeeModel targetEmployee = employeeRepository.findByDeletedIsFalseAndUsernameEquals(
                        employeeUsername);
                SupervisionModel supervisionOfTargetEmployee = supervisionRepository
                        .findByDeletedIsFalseAndEmployeeUsernameEquals(employeeUsername);

                final boolean targetEmployeeDoesNotExist = ( targetEmployee == null );
                final boolean supervisionOfTargetEmployeeDoesNotExist = ( supervisionOfTargetEmployee == null );

                if (targetEmployeeDoesNotExist || supervisionOfTargetEmployeeDoesNotExist) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    List< RequestModel > requests = new ArrayList<>();

                    final List< RequestModel > acceptedRequests = requestUtilServiceApi
                            .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_ACCEPTED);
                    final List< RequestModel > requestedRequests = requestUtilServiceApi
                            .findAllByUsernameAndStatus(employeeUsername, StatusConstant.STATUS_REQUESTED);

                    requests.addAll(acceptedRequests);
                    requests.addAll(requestedRequests);

                    final boolean acceptedOrRequestedRequestsExist = !requests.isEmpty();

                    if (acceptedOrRequestedRequestsExist) {
                        for (RequestModel request : requests) {
                            request.setStatus(StatusConstant.STATUS_CANCELLED);
                            request.setUpdatedDate(new Date());
                            request.setUpdatedBy(adminUsername);

                            requestUtilServiceApi.save(request);
                        }
                    }

                    final boolean employeeWithEmployeeUsernameIsAdministrator = adminRepository
                            .existsAdminModelByDeletedIsFalseAndUsernameEquals(employeeUsername);

                    if (employeeWithEmployeeUsernameIsAdministrator) {
                        AdminModel administrator = adminRepository.findByDeletedIsFalseAndUsernameEquals(
                                employeeUsername);

                        administrator.setDeleted(true);

                        adminRepository.save(administrator);
                    }

                    targetEmployee.setDeleted(true);

                    employeeRepository.save(targetEmployee);

                    supervisionOfTargetEmployee.setDeleted(true);

                    supervisionRepository.save(supervisionOfTargetEmployee);
                }
            }
        }
    }

    @Override
    @CacheEvict(value = { "employeesListData", "employeeDetailData" },
                allEntries = true)
    public void changeSupervisorOnPreviousSupervisorDeletion(
            final String adminUsername, final String oldSupervisorUsername, final String newSupervisorUsername
    )
            throws
            UnauthorizedOperationException,
            DataNotFoundException,
            BadRequestException {

        final boolean noAdministratorUsernameGiven = adminUsername.isEmpty();
        final boolean noOldSupervisorUsernameGiven = oldSupervisorUsername.isEmpty();
        final boolean noNewSupervisorUsernameGiven = newSupervisorUsername.isEmpty();

        if (noAdministratorUsernameGiven || noOldSupervisorUsernameGiven || noNewSupervisorUsernameGiven) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean validAdministrator = roleDeterminer.determineRole(adminUsername)
                                                             .equals(RoleConstant.ROLE_ADMINISTRATOR);
            final boolean selfSupervisorChangeOnDeletionAttempt = oldSupervisorUsername.equals(adminUsername);

            if (!validAdministrator || selfSupervisorChangeOnDeletionAttempt) {
                throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
            } else {
                if (newSupervisorUsername.contains(" ")) {
                    throw new BadRequestException(INCORRECT_PARAMETER);
                } else {
                    final boolean employeeWithOldSupervisorUsernameExists = employeeRepository
                            .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(oldSupervisorUsername);
                    final boolean employeeWithNewSupervisorUsernameExists = employeeRepository
                            .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(newSupervisorUsername);

                    if (!employeeWithOldSupervisorUsernameExists || !employeeWithNewSupervisorUsernameExists) {
                        throw new DataNotFoundException(DATA_NOT_FOUND);
                    } else {
                        List< SupervisionModel > supervisions = supervisionRepository
                                .findAllByDeletedIsFalseAndSupervisorUsernameEquals(oldSupervisorUsername);

                        final boolean employeeWithOldSupervisorUsernameDoesNotSupervise = supervisions.isEmpty();

                        if (employeeWithOldSupervisorUsernameDoesNotSupervise) {
                            throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                        } else {
                            final List< String > supervisedEmployeesUsernames = new ArrayList<>();

                            for (final SupervisionModel supervision : supervisions) {
                                supervisedEmployeesUsernames.add(supervision.getEmployeeUsername());
                            }

                            for (final String supervisedEmployeeUsername : supervisedEmployeesUsernames) {
                                if (employeeUtilServiceApi.hasCyclicSupervising(
                                        supervisedEmployeeUsername,
                                        newSupervisorUsername
                                )) {
                                    throw new UnauthorizedOperationException(UNAUTHORIZED_OPERATION);
                                }
                            }

                            employeeUtilServiceApi.demotePreviousSupervisorFromAdminIfNecessary(
                                    adminUsername, oldSupervisorUsername, newSupervisorUsername, supervisions
                            );
                        }
                    }
                }
            }
        }
    }

}
