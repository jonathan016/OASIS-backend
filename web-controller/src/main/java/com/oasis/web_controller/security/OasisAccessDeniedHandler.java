package com.oasis.web_controller.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasis.web_controller.mapper.response.FailedResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.oasis.model.constant.exception_constant.ErrorCodeAndMessage.UNAUTHORIZED_OPERATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@SuppressWarnings({ "SpringJavaAutowiredFieldsWarningInspection", "Duplicates" })
public class OasisAccessDeniedHandler
        implements AccessDeniedHandler {

    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception
    )
            throws
            IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_OPERATION.getErrorCode(),
                UNAUTHORIZED_OPERATION.getErrorMessage(), null
        ), HttpStatus.UNAUTHORIZED));

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(String.valueOf(objectMapper.readTree(value).path("body")));

    }

}