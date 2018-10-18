package com.oasis.service.implementation;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.model.entity.SupervisionModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.repository.SupervisionRepository;
import com.oasis.service.RoleDeterminer;
import com.oasis.service.api.LoginServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.oasis.service.ServiceConstant.ROLE_ADMINISTRATOR;
import static com.oasis.service.ServiceConstant.ROLE_EMPLOYEE;
import static com.oasis.service.ServiceConstant.ROLE_SUPERIOR;

@Service
public class LoginServiceImpl implements LoginServiceApi {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SupervisionRepository supervisionRepository;

    @Override
    public EmployeeModel checkLoginCredentials(String username, String password) {
        EmployeeModel result = employeeRepository.findByUsername(username);

        if (result != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(password, result.getPassword())) {
                result.setPassword(null);

                return result;
            }
        }

        return result;
    }

    @Override
    public String determineUserRole(String employeeId) {
        RoleDeterminer roleDeterminer = new RoleDeterminer();
        return roleDeterminer.determineRole(employeeRepository, supervisionRepository, employeeId);
    }
}
