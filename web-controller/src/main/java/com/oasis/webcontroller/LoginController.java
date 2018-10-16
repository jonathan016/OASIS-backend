package com.oasis.webcontroller;

import com.oasis.constant.APIMappingValue;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.implementation.LoginServiceImpl;
import com.oasis.webmodel.request.LoginRequest;
import com.oasis.webmodel.response.NoPagingResponse;
import com.oasis.webmodel.response.ResponseStatus;
import com.oasis.webmodel.response.failed.FailedResponse;
import com.oasis.webmodel.response.success.LoginSuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.oasis.constant.ErrorCodeAndMessage.PASSWORD_DOES_NOT_MATCH;
import static com.oasis.constant.ErrorCodeAndMessage.USER_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "http://localhost")
@RestController
public class LoginController {
    @Autowired
    private LoginServiceImpl loginServiceImpl;

    @PostMapping(value = APIMappingValue.API_LOGIN,
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public NoPagingResponse<?> callLoginService(@RequestBody LoginRequest request) {
        EmployeeModel result =
                loginServiceImpl.checkLoginCredentials(
                        request.getUsername().toLowerCase(),
                        request.getPassword()
                );

        if (result == null)
            return produceFailedResponse(USER_NOT_FOUND[0], USER_NOT_FOUND[1]);

        if (result.getPassword() == null)
            return produceFailedResponse(PASSWORD_DOES_NOT_MATCH[0], PASSWORD_DOES_NOT_MATCH[1]);

        return produceSuccessResponse(result.get_id());
    }

    private NoPagingResponse<LoginSuccessResponse> produceSuccessResponse(String employeeId) {
        NoPagingResponse<LoginSuccessResponse> successResponse = new NoPagingResponse<>();

        String role = loginServiceImpl.determineUserRole(employeeId);

        successResponse.setCode(HttpStatus.OK.value());
        successResponse.setSuccess(ResponseStatus.SUCCESS);
        successResponse.setValue(
                new LoginSuccessResponse(
                        employeeId,
                        role
                )
        );

        return successResponse;
    }

    private NoPagingResponse<FailedResponse> produceFailedResponse(String errorCode, String errorMessage) {
        NoPagingResponse<FailedResponse> failedResponse = new NoPagingResponse<>();

        failedResponse.setCode(HttpStatus.NOT_FOUND.value());
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