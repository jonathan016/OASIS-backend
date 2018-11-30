package com.oasis;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
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

    public String determineRole(String username) throws DataNotFoundException {

        if (adminRepository.findByUsername(username) != null) {
            return ServiceConstant.ROLE_ADMINISTRATOR;
        } else {
            EmployeeModel employee = employeeRepository.findByUsername(username);

            if(employee == null){
                throw new DataNotFoundException(USER_NOT_FOUND);
            } else if (supervisionRepository.existsSupervisionModelsBySupervisorUsername(username)) {
                return ServiceConstant.ROLE_SUPERIOR;
            } else {
                return ServiceConstant.ROLE_EMPLOYEE;
            }
        }
    }

}
