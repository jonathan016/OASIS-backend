package com.oasis.service.tool.helper;

import com.oasis.model.exception.DataNotFoundException;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.model.constant.service_constant.RoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.DATA_NOT_FOUND;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class RoleDeterminer {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private SupervisionRepository supervisionRepository;

    public String determineRole(
            final String username
    )
            throws
            DataNotFoundException {

        boolean administratorWithUsernameExists = adminRepository
                .existsAdminModelByDeletedIsFalseAndUsernameEquals(username);

        if (administratorWithUsernameExists) {
            return RoleConstant.ROLE_ADMINISTRATOR;
        } else {
            boolean employeeWithUsernameExists = employeeRepository
                    .existsEmployeeModelByDeletedIsFalseAndUsernameEquals(username);

            if (employeeWithUsernameExists) {
                boolean supervisionWithUsernameAsSupervisorExists = supervisionRepository
                        .existsSupervisionModelsByDeletedIsFalseAndSupervisorUsernameEquals(username);

                if (supervisionWithUsernameAsSupervisorExists) {
                    return RoleConstant.ROLE_SUPERIOR;
                } else {
                    return RoleConstant.ROLE_EMPLOYEE;
                }
            } else {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }
        }
    }

}
