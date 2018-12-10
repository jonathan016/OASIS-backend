package com.oasis;

import com.oasis.exception.DataNotFoundException;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.ServiceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.oasis.exception.helper.ErrorCodeAndMessage.USER_NOT_FOUND;

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

        boolean administratorWithUsernameExists = adminRepository.existsAdminModelByDeletedIsFalseAndUsernameEquals(
                username);

        if (administratorWithUsernameExists) {
            return ServiceConstant.ROLE_ADMINISTRATOR;
        } else {
            boolean employeeWithUsernameExists = employeeRepository.existsEmployeeModelByDeletedIsFalseAndUsername(
                    username);

            if (employeeWithUsernameExists) {
                boolean supervisionWithUsernameAsSupervisorExists =
                        supervisionRepository.existsSupervisionModelsByDeletedIsFalseAndSupervisorUsername(
                        username);

                if (supervisionWithUsernameAsSupervisorExists) {
                    return ServiceConstant.ROLE_SUPERIOR;
                } else {
                    return ServiceConstant.ROLE_EMPLOYEE;
                }
            } else {
                throw new DataNotFoundException(USER_NOT_FOUND);
            }
        }
    }

}
