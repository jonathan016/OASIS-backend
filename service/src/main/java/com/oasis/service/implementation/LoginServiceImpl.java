package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.service.api.LoginServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static com.oasis.exception.helper.ErrorCodeAndMessage.PASSWORD_DOES_NOT_MATCH;
import static com.oasis.exception.helper.ErrorCodeAndMessage.USER_NOT_FOUND;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class LoginServiceImpl implements LoginServiceApi {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleDeterminer roleDeterminer;

    @Override
    public EmployeeModel checkLoginCredentials(final String username, final String password)
            throws DataNotFoundException, UserNotAuthenticatedException {
        EmployeeModel result = employeeRepository.findByUsername(username);
        if (result == null) {
            throw new DataNotFoundException(USER_NOT_FOUND);
        } else {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(password, result.getPassword())) {
                throw new UserNotAuthenticatedException(PASSWORD_DOES_NOT_MATCH);
            }
        }
        return result;
    }

    @Override
    public String determineUserRole(final String employeeNik) throws DataNotFoundException {
        return roleDeterminer.determineRole(employeeNik);
    }
}
