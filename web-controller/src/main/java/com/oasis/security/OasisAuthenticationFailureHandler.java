package com.oasis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.oasis.exception.helper.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@SuppressWarnings({ "SpringJavaAutowiredFieldsWarningInspection", "Duplicates" })
public class OasisAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    )
            throws
            IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_OPERATION.getErrorCode(),
                UNAUTHORIZED_OPERATION.getErrorMessage()
        ), HttpStatus.UNAUTHORIZED));

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(String.valueOf(objectMapper.readTree(value).path("body")));
    }

}
