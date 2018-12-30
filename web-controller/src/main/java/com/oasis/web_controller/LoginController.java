package com.oasis.web_controller;

import com.oasis.exception.BadRequestException;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.LoginResponseMapper;
import com.oasis.service.api.login.LoginServiceApi;
import com.oasis.tool.helper.ActiveComponentManager;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = APIMappingValue.CROSS_ORIGIN_LINK)
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class LoginController {

    @Autowired
    private LoginResponseMapper loginResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @Autowired
    private LoginServiceApi loginServiceApi;

    @Autowired
    private ActiveComponentManager activeComponentManager;



    @PostMapping(value = APIMappingValue.API_LOGIN,
                 produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getLoginData(
            @AuthenticationPrincipal
            final User user
    ) {

        final Map< String, String > loginData;
        final String username;
        final String name;
        final String photo;
        final String role;

        try {
            loginData = loginServiceApi.getLoginData(user.getUsername());

            username = loginData.get("username");
            name = loginData.get("name");
            photo = loginData.get("photo");
            role = loginData.get("role");
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.NOT_FOUND.value(),
                    dataNotFoundException.getErrorCode(),
                    dataNotFoundException.getErrorMessage(),
                    null
            ), HttpStatus.NOT_FOUND);
        } catch (UserNotAuthenticatedException userNotAuthenticatedException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.UNAUTHORIZED.value(),
                    userNotAuthenticatedException.getErrorCode(),
                    userNotAuthenticatedException.getErrorMessage(),
                    null
            ), HttpStatus.UNAUTHORIZED);
        } catch (BadRequestException badRequestException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(
                    HttpStatus.BAD_REQUEST.value(),
                    badRequestException.getErrorCode(),
                    badRequestException.getErrorMessage(),
                    null
            ), HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(
                loginResponseMapper.produceLoginSuccessResponse(HttpStatus.OK.value(), username, name, photo, role),
                HttpStatus.OK
        );
    }

    @GetMapping(value = APIMappingValue.API_LOGOUT)
    public ResponseEntity logout(
            HttpSession session,
            @AuthenticationPrincipal
                    User user
    ) {

        user.eraseCredentials();
        session.invalidate();

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = APIMappingValue.API_SIDE_BAR,
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSideBarActiveComponents(
            @AuthenticationPrincipal
                    User user
    ) {

        final Map< String, Boolean > activeComponents = activeComponentManager
                .getSideBarActiveComponents(user.getUsername(), new ArrayList<>(user.getAuthorities()).get(0)
                                                                                                      .getAuthority());

        return new ResponseEntity<>(loginResponseMapper.produceSideBarActiveComponentResponse(
                HttpStatus.OK.value(), activeComponents
        ), HttpStatus.OK);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @RequestMapping(value = APIMappingValue.API_MISDIRECT,
                    method = {
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
                                                                             HttpStatus.BAD_REQUEST.name(), message,
                                                                             null
        ), HttpStatus.BAD_REQUEST);
    }

}
