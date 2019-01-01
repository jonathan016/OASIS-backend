package com.oasis.service.implementation.authentication;

import com.oasis.model.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.api.authentication.AuthenticationApi;
import com.oasis.service.api.employees.EmployeeUtilServiceApi;
import com.oasis.service.tool.helper.RoleDeterminer;
import com.oasis.service.tool.util.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class AuthenticationImpl
        implements AuthenticationApi {

    @Autowired
    private EmployeeUtilServiceApi employeeUtilServiceApi;

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private BCryptPasswordEncoder encoder;



    @Override
    @SuppressWarnings("UnnecessaryLocalVariable")
    public Authentication getAuthentication(String username, final String password) {

        final boolean validUsernameWithSuffix = username.matches(Regex.REGEX_USERNAME_LOGIN_SUFFIX);
        final boolean validUsernameWithoutSuffix = username.matches(Regex.REGEX_USERNAME_LOGIN_NO_SUFFIX);

        if (!validUsernameWithSuffix && !validUsernameWithoutSuffix) {
            return null;
        } else {
            if (validUsernameWithSuffix) {
                username = username.toLowerCase().substring(0, username.indexOf('@'));
            } else {
                username = username.toLowerCase();
            }

            if (employeeUtilServiceApi.existsEmployeeModelByDeletedIsFalseAndUsername(username)) {
                final EmployeeModel employee = employeeUtilServiceApi.findByDeletedIsFalseAndUsername(username);

                if (encoder.matches(password, employee.getPassword())) {
                    try {
                        final String role = roleDeterminer.determineRole(username);

                        final List< GrantedAuthority > grantedAuths = new ArrayList<>();
                        grantedAuths.add(new SimpleGrantedAuthority(role));
                        final UserDetails principal = new User(username, password, grantedAuths);

                        final Authentication auth = new UsernamePasswordAuthenticationToken(
                                principal, password, grantedAuths);

                        return auth;
                    } catch (DataNotFoundException dataNotFoundException) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

}
