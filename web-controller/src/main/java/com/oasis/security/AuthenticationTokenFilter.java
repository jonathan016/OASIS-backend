package com.oasis.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class AuthenticationTokenFilter
        extends GenericFilterBean {

    private String tokenHeader = "X-Auth-Token";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws
            IOException,
            ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;

        final String token = request.getHeader(tokenHeader);

        if (token != null) {
            //            final User user = new User()
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}