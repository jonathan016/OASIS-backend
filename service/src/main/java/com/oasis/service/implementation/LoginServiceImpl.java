package com.oasis.service.implementation;

import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.service.api.LoginServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.INVALID_PASSWORD;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class LoginServiceImpl
        implements LoginServiceApi {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public String checkLoginCredentials(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException {

        final boolean validUsernameWithSuffix = username.matches("([A-Za-z0-9]+.?[A-Za-z0-9]+)+@gdn-commerce.com");
        final boolean validUsernameWithoutSuffix = username.matches("([A-Za-z0-9]+.?[A-Za-z0-9]+)+");

        if (!validUsernameWithSuffix && !validUsernameWithoutSuffix) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        }

        final EmployeeModel employee;

        if (validUsernameWithSuffix) {
            employee = employeeRepository.findByDeletedIsFalseAndUsername(username.substring(0, username.indexOf('@')));
        } else {
            employee = employeeRepository.findByDeletedIsFalseAndUsername(username);
        }

        if (employee == null) {
            throw new DataNotFoundException(DATA_NOT_FOUND);
        } else {
            final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            final boolean passwordMatch = encoder.matches(password, employee.getPassword());
            if (!passwordMatch) {
                throw new UserNotAuthenticatedException(INVALID_PASSWORD);
            }
        }

        return employee.getUsername();
    }

}
