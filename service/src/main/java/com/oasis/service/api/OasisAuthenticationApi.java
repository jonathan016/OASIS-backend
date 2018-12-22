package com.oasis.service.api;

import org.springframework.security.core.Authentication;

public interface OasisAuthenticationApi {

    Authentication getAuthentication(final String username, final String password);

}
