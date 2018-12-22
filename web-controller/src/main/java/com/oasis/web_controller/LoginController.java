package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.LoginResponseMapper;
import com.oasis.service.api.LoginServiceApi;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class LoginController {

    @Autowired
    private LoginServiceApi loginServiceApi;
    @Autowired
    private LoginResponseMapper loginResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @PostMapping(value = APIMappingValue.API_LOGIN, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getLoginData(
            @AuthenticationPrincipal
            final Principal principal
    ) {

        final Map< String, String > loginData;
        final String username;
        final String name;
        final String role;

        try {
            loginData = loginServiceApi.getLoginData(principal.getName());

            username = loginData.get("username");
            name = loginData.get("name");
            role = loginData.get("role");
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(),
                                                                                 dataNotFoundException.getErrorCode(),
                                                                                 dataNotFoundException.getErrorMessage()
            ), HttpStatus.NOT_FOUND);
        } catch (UserNotAuthenticatedException userNotAuthenticatedException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(),
                                                                                 userNotAuthenticatedException
                                                                                         .getErrorCode(),
                                                                                 userNotAuthenticatedException
                                                                                         .getErrorMessage()
            ), HttpStatus.UNAUTHORIZED);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                                 badRequestException.getErrorCode(),
                                                                                 badRequestException.getErrorMessage()
            ), HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(
                loginResponseMapper.produceLoginSuccessResponse(HttpStatus.OK.value(), username, name, role),
                HttpStatus.OK
        );
    }

    @GetMapping(value = APIMappingValue.API_LOGOUT)
    public ResponseEntity logout(
            HttpSession session
    ) {

        session.invalidate();

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT, method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.HEAD,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,
            RequestMethod.TRACE
    })
    public ResponseEntity returnIncorrectMappingCalls(
            final MissingServletRequestParameterException exception
    ) {

        final String message;

        if (exception.getParameterName() != null) {
            message = exception.getMessage();
        } else {
            message = "Incorrect mapping/method!";
        }

        return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.BAD_REQUEST.value(),
                                                                             HttpStatus.BAD_REQUEST.name(), message
        ), HttpStatus.BAD_REQUEST);
    }

}
