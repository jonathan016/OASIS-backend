package com.oasis.security;

import com.oasis.service.api.OasisAuthenticationApi;
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
