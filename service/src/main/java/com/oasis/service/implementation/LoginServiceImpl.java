package com.oasis.service.implementation;

import com.oasis.RoleDeterminer;
import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.service.api.LoginServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.oasis.exception.helper.ErrorCodeAndMessage.*;

@Service
@Transactional
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class LoginServiceImpl
        implements LoginServiceApi {

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Map< String, String > getLoginData(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            BadRequestException,
            UserNotAuthenticatedException {

        Map< String, String > loginData = new HashMap<>();

        loginData.put("username", getUsernameIfEmployeeWithCredentialExists(username, password));
        loginData.put("name", getFirstNameFromUsername(username));
        loginData.put("role", getRoleFromUsername(username));

        return loginData;
    }

    private String getUsernameIfEmployeeWithCredentialExists(
            final String username, final String password
    )
            throws
            DataNotFoundException,
            UserNotAuthenticatedException,
            BadRequestException {

        if (username == null) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean validUsernameWithSuffix = username.matches("([A-Za-z0-9]+.?[A-Za-z0-9]+)+@gdn-commerce.com");
            final boolean validUsernameWithoutSuffix = username.matches("([A-Za-z0-9]+.?[A-Za-z0-9]+)+");

            if (!validUsernameWithSuffix && !validUsernameWithoutSuffix) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            }

            final EmployeeModel employee;

            if (validUsernameWithSuffix) {
                employee = employeeRepository
                        .findByDeletedIsFalseAndUsername(username.toLowerCase().substring(0, username.indexOf('@')));
            } else {
                employee = employeeRepository.findByDeletedIsFalseAndUsername(username.toLowerCase());
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getFirstNameFromUsername(
            final String username
    ) {

        final EmployeeModel employee = employeeRepository.findByDeletedIsFalseAndUsername(username);

        final String name = employee.getName();
        final String firstName;

        if (name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(' '));
        } else {
            firstName = name;
        }

        return firstName;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getRoleFromUsername(
            final String username
    )
            throws
            DataNotFoundException {

        final String role = roleDeterminer.determineRole(username);

        return role;
    }

}
