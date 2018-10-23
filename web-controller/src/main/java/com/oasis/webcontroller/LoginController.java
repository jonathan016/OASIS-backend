package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.exception.DataNotFoundException;
import com.oasis.exception.UserNotAuthenticatedException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.responsemapper.LoginResponseMapper;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequest;
import com.oasis.webmodel.response.NoPagingResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    public NoPagingResponse<?> callLoginService(
            @RequestBody
                    LoginRequest request
    ) {
        EmployeeModel result;

        try {
            result = loginServiceImpl.checkLoginCredentials(
                    request.getUsername()
                            .toLowerCase(), request.getPassword());
        } catch (DataNotFoundException | UserNotAuthenticatedException e) {
            return loginResponseMapper.produceFailedResponse(e.getErrorCode(), e.getErrorMessage());
        }

        String role;

        try {
            role = loginServiceImpl.determineUserRole(result.getNik());
        } catch (DataNotFoundException e){
            return loginResponseMapper.produceFailedResponse(e.getErrorCode(), e.getErrorMessage());
        }

        return loginResponseMapper.produceSuccessResponse(result.getNik(), role);
    }
}
