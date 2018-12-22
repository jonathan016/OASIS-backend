package com.oasis.security;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.api.EmployeesServiceApi;
import com.oasis.service.api.OasisAuthenticationApi;
import com.oasis.tool.helper.RoleDeterminer;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class OasisAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private OasisAuthenticationApi oasisAuthenticationApi;

    public OasisAuthenticationProvider() {

        super();
    }

    @Override
    public Authentication authenticate(final Authentication authentication)
            throws
            AuthenticationException {

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        return oasisAuthenticationApi.getAuthentication(username, password);
    }

    @Override
    public boolean supports(final Class< ? > authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
