package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequest;
import com.oasis.webmodel.response.PagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.LoginSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.oasis.constant.ErrorCodeAndMessage.PASSWORD_DOES_NOT_MATCH;
import static com.oasis.constant.ErrorCodeAndMessage.USER_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LoginController {
    @Autowired
    private LoginServiceImpl loginServiceImpl;

    @CrossOrigin(origins = "http://localhost")
    @PostMapping(value = APIMappingValue.API_LOGIN,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public PagingResponse<?> callLoginService(@RequestBody LoginRequest model) {
        EmployeeModel result =
                loginServiceImpl.checkLoginCredentials(
                        model.getUsername().toLowerCase(),
                        model.getPassword()
                );

        if (result == null)
            return produceFailedResponse(USER_NOT_FOUND[0], USER_NOT_FOUND[1]);

        if (result.getPassword() == null)
            return produceFailedResponse(PASSWORD_DOES_NOT_MATCH[0], PASSWORD_DOES_NOT_MATCH[1]);

        return produceSuccessResponse(result);
    }

    private PagingResponse<LoginSuccessResponse> produceSuccessResponse(EmployeeModel result) {
        PagingResponse<LoginSuccessResponse> successResponse = new PagingResponse<>();

        String role = loginServiceImpl.determineUserRole(result.get_id());

        successResponse.setCode("200");
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new LoginSuccessResponse(
                        result.get_id(),
                        role
                )
        );

        return successResponse;
    }

    private PagingResponse<FailedResponse> produceFailedResponse(String errorCode, String errorMessage) {
        PagingResponse<FailedResponse> failedResponse = new PagingResponse<>();

        failedResponse.setCode("404");
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new FailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }
}