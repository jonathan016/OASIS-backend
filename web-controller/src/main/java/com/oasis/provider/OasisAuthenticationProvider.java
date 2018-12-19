package com.oasis.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class OasisAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private OasisAuthenticationService oasisAuthenticationService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws
            AuthenticationException {

        //        try {
        //            return oasisAuthenticationService
        //                    .authenticateUser(authentication.getName(), authentication.getCredentials().toString());
        //        } catch (DataNotFoundException e) {
        //            throw new BadCredentialsException(e.getErrorMessage());
        //        }

        if (authentication.getName().equals("jonathan")) {
            return new UsernamePasswordAuthenticationToken(authentication.getName(),
                                                           authentication.getCredentials().toString(), new ArrayList<>()
            );
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class< ? > authentication) {

        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
