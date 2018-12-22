package com.oasis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.oasis.exception.helper.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

/**
 * The Entry Point will not redirect to any sort of Login - it will return the 401
 */
@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public final class OasisRestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Override
    public void commence(
            final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException authException
    )
            throws
            IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_OPERATION.getErrorCode(),
                UNAUTHORIZED_OPERATION.getErrorMessage()
        ), HttpStatus.UNAUTHORIZED));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(String.valueOf(objectMapper.readTree(value).path("body")));
    }

}