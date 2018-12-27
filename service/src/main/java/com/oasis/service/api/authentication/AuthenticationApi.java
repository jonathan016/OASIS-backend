package com.oasis.service.api.authentication;

import org.springframework.security.core.Authentication;

public interface AuthenticationApi {

    Authentication getAuthentication(final String username, final String password);

}
