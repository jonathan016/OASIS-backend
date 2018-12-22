package com.oasis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.response_mapper.FailedResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.oasis.exception.helper.ErrorCodeAndMessage.DATA_NOT_FOUND;
import static com.oasis.exception.helper.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class OasisAccessDeniedHandler
        implements AccessDeniedHandler {

    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception
    )
            throws
            IOException,
            ServletException {

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                HttpStatus.BAD_REQUEST.value(), DATA_NOT_FOUND.getErrorCode(),
                DATA_NOT_FOUND.getErrorMessage()
        ), HttpStatus.BAD_REQUEST));

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.getWriter().write(String.valueOf(objectMapper.readTree(value).path("body")));

    }

}