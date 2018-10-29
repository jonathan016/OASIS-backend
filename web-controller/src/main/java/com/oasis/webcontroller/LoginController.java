package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.responsemapper.LoginResponseMapper;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class LoginController {

    @Autowired
    private LoginServiceImpl loginServiceImpl;
    @Autowired
    private LoginResponseMapper loginResponseMapper;

    @PostMapping(value = APIMappingValue.API_LOGIN,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity callLoginService(
            @RequestBody
                    LoginRequest request
    ) {
        EmployeeModel result;

        try {
            result = loginServiceImpl.checkLoginCredentials(
                    request.getUsername()
                            .toLowerCase(), request.getPassword());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(loginResponseMapper.produceLoginFailedResponse(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        } catch (UserNotAuthenticatedException userNotAuthenticatedException) {
            return new ResponseEntity<>(loginResponseMapper.produceLoginFailedResponse(HttpStatus.UNAUTHORIZED.value(), userNotAuthenticatedException.getErrorCode(), userNotAuthenticatedException.getErrorMessage()), HttpStatus.UNAUTHORIZED);
        }

        String role;

        try {
            role = loginServiceImpl.determineUserRole(result.getNik());
        } catch (DataNotFoundException dataNotFoundException) {
            return new ResponseEntity<>(loginResponseMapper.produceLoginFailedResponse(HttpStatus.NOT_FOUND.value(), dataNotFoundException.getErrorCode(), dataNotFoundException.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(loginResponseMapper.produceLoginSuccessResponse(HttpStatus.OK.value(), result.getNik(), role), HttpStatus.OK);
    }
}
