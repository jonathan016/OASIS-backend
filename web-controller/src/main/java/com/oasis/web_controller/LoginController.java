package com.oasis.web_controller;

import com.oasis.web_model.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.response_mapper.FailedResponseMapper;
import com.oasis.response_mapper.LoginResponseMapper;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.web_model.request.login.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "http://localhost")
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class LoginController {

    @Autowired
    private LoginServiceImpl loginServiceImpl;
    @Autowired
    private LoginResponseMapper loginResponseMapper;
    @Autowired
    private FailedResponseMapper failedResponseMapper;

    @PostMapping(value = APIMappingValue.API_LOGIN,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callLoginService(
            @RequestBody final LoginRequest request
    ) {

        EmployeeModel result;
        String role;

        try {
            result = loginServiceImpl.checkLoginCredentials(request.getUsername().toLowerCase(), request.getPassword());
            role = loginServiceImpl.determineUserRole(result.getUsername());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (UserNotAuthenticatedException userNotAuthenticatedException) {
            return new ResponseEntity<>(failedResponseMapper.produceFailedResult(HttpStatus.UNAUTHORIZED.value(), userNotAuthenticatedException.getErrorCode(), userNotAuthenticatedException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(loginResponseMapper.produceLoginSuccessResponse(HttpStatus.OK.value(), result.getUsername()
                , role), HttpStatus.OK);
    }

}
