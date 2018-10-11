package com.oasis.webcontroller;

import com.oasis.MappingValue;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequest;
import com.oasis.webmodel.response.BaseResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.LoginFailedResponse;
import com.oasis.webmodel.response.success.LoginSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.oasis.ErrorCodeAndMessage.PASSWORD_DOES_NOT_MATCH;
import static com.oasis.ErrorCodeAndMessage.USER_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class LoginController {
    @Autowired
    private LoginServiceImpl loginServiceImpl;

    @CrossOrigin(origins = "http://localhost")
    @PostMapping(value = MappingValue.API_LOGIN,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public BaseResponse<?> callLoginService(@RequestBody LoginRequest model) {
        EmployeeModel result =
                loginServiceImpl.checkLoginCredentials(
                        model.getUsername().toLowerCase(),
                        model.getPassword()
                );

        if (result == null)
            return produceFailedResponse(USER_NOT_FOUND[0], USER_NOT_FOUND[1]);

        if (result.getPassword() == null)
            return produceFailedResponse(PASSWORD_DOES_NOT_MATCH[0], PASSWORD_DOES_NOT_MATCH[1]);

        return produceOKResponse(result);
    }

    private BaseResponse<LoginSuccessResponse> produceOKResponse(EmployeeModel result) {
        BaseResponse<LoginSuccessResponse> successResponse = new BaseResponse<>();

        successResponse.setCode("200");
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new LoginSuccessResponse(
                        result.get_id()
                )
        );

        return successResponse;
    }

    private BaseResponse<LoginFailedResponse> produceFailedResponse(String errorCode, String errorMessage) {
        BaseResponse<LoginFailedResponse> failedResponse = new BaseResponse<>();

        failedResponse.setCode("404");
        failedResponse.setSuccess(ResponseStatus.FAILED);
        failedResponse.setValue(
                new LoginFailedResponse(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }
}