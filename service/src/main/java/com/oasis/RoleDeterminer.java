package com.oasis;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.AdminRepository;
import com.oasis.repository.EmployeeRepository;
import com.oasis.service.ServiceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.oasis.exception.helper.ErrorCodeAndMessage.USER_NOT_FOUND;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class RoleDeterminer {

    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    public String determineRole(String employeeNik) throws DataNotFoundException {
        if (adminRepository.findByNik(employeeNik) != null) {
            return ServiceConstant.ROLE_ADMINISTRATOR;
        } else {
            EmployeeModel employee = employeeRepository.findByNik(employeeNik);
            if(employee == null){
                throw new DataNotFoundException(USER_NOT_FOUND);
            } else if (employee.getSupervisingCount() > 0) {
                return ServiceConstant.ROLE_SUPERIOR;
            } else {
                return ServiceConstant.ROLE_EMPLOYEE;
            }
        }
    }
}
