package com.oasis.service.implementation;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.service.api.LoginServiceApi;
import com.oasis.tool.helper.RoleDeterminer;
import com.oasis.tool.util.Regex;
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
    private EmployeesServiceApi employeesServiceApi;

    @Override
    public Map< String, String > getLoginData(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException {

        Map< String, String > loginData = new HashMap<>();

        loginData.put("username", getUsernameIfEmployeeWithCredentialExists(username));
        loginData.put("name", getFirstNameFromUsername(username));
        loginData.put("role", getRoleFromUsername(username));

        return loginData;
    }

    private String getUsernameIfEmployeeWithCredentialExists(
            final String username
    )
            throws
            DataNotFoundException,
            BadRequestException {

        if (username == null) {
            throw new BadRequestException(INCORRECT_PARAMETER);
        } else {
            final boolean validUsernameWithSuffix = username.matches(Regex.REGEX_USERNAME_LOGIN_SUFFIX);
            final boolean validUsernameWithoutSuffix = username.matches(Regex.REGEX_USERNAME_LOGIN_NO_SUFFIX);

            if (!validUsernameWithSuffix && !validUsernameWithoutSuffix) {
                throw new DataNotFoundException(DATA_NOT_FOUND);
            } else {
                final EmployeeModel employee;

                if (validUsernameWithSuffix) {
                    employee = employeesServiceApi
                            .findByDeletedIsFalseAndUsername(username.toLowerCase().substring(0, username.indexOf('@')));
                } else {
                    employee = employeesServiceApi.findByDeletedIsFalseAndUsername(username.toLowerCase());
                }

                if (employee == null) {
                    throw new DataNotFoundException(DATA_NOT_FOUND);
                } else {
                    return employee.getUsername();
                }
            }
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getFirstNameFromUsername(
            final String username
    ) {

        final EmployeeModel employee = employeesServiceApi.findByDeletedIsFalseAndUsername(username);

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
