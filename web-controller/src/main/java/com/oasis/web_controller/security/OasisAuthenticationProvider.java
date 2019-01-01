package com.oasis.web_controller.security;

import com.oasis.service.api.authentication.AuthenticationApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class OasisAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private AuthenticationApi authenticationApi;

    public OasisAuthenticationProvider() {

        super();
    }

    @Override
    public Authentication authenticate(final Authentication authentication)
            throws
            AuthenticationException {

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        return authenticationApi.getAuthentication(username, password);
    }

    @Override
    public boolean supports(final Class< ? > authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
