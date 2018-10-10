package com.oasis.webcontroller;

import com.oasis.MappingValue;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequestModel;
import com.oasis.webmodel.response.BaseResponse;
import com.oasis.webmodel.response.ResponseStatuses;
import com.oasis.webmodel.response.fail.LoginResponseModelFAIL;
import com.oasis.webmodel.response.ok.LoginResponseModelOK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.oasis.ErrorCodeAndMessage.PASSWORD_DOES_NOT_MATCH;
import static com.oasis.ErrorCodeAndMessage.USER_NOT_FOUND;

@RestController
public class LoginController {
    @Autowired
    private LoginServiceImpl loginServiceImpl;

    @PostMapping(value = MappingValue.API_LOGIN,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<?> callLoginService(@RequestBody LoginRequestModel model) {
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

    private BaseResponse<LoginResponseModelOK> produceOKResponse(EmployeeModel result) {
        BaseResponse<LoginResponseModelOK> okResponse = new BaseResponse<>();

        okResponse.setCode("200");
        okResponse.setStatus(ResponseStatuses.SUCCESS_STATUS);
        okResponse.setValue(
                new LoginResponseModelOK(
                        result.get_id()
                )
        );

        return okResponse;
    }

    private BaseResponse<LoginResponseModelFAIL> produceFailedResponse(String errorCode, String errorMessage) {
        BaseResponse<LoginResponseModelFAIL> failedResponse = new BaseResponse<>();

        failedResponse.setCode("404");
        failedResponse.setStatus(ResponseStatuses.FAILED_STATUS);
        failedResponse.setValue(
                new LoginResponseModelFAIL(
                        errorCode,
                        errorMessage
                )
        );

        return failedResponse;
    }
}