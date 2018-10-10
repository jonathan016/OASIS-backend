package com.oasis.webcontroller;

import com.oasis.MappingValues;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.service.LoginService;
import com.oasis.webmodel.request.LoginRequestModel;
import com.oasis.webmodel.response.BaseResponse;
import com.oasis.webmodel.response.fail.LoginResponseModelFAIL;
import com.oasis.webmodel.response.ok.LoginResponseModelOK;
import com.oasis.webmodel.response.ResponseStatuses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(value = MappingValues.LOGIN_MAPPING_VALUE, method = RequestMethod.POST,
                    produces = "application/json", consumes = "application/json")
    public BaseResponse<?> callLoginService(@RequestBody LoginRequestModel model){
        EmployeeModel result = loginService.checkLoginCredentials(model.getUsername(), model.getPassword());

        if(result == null) return produceFailedResponse();

        return produceOKResponse(result);
    }

    private BaseResponse<LoginResponseModelOK> produceOKResponse(EmployeeModel result){
        BaseResponse<LoginResponseModelOK> okResponse = new BaseResponse<>();

        okResponse.setCode("200");
        okResponse.setStatus(ResponseStatuses.SUCCESS_STATUS);
        okResponse.setValue(
                new LoginResponseModelOK(
                        result.get_id()));

        return okResponse;
    }

    private BaseResponse<LoginResponseModelFAIL> produceFailedResponse(){
        BaseResponse<LoginResponseModelFAIL> failedResponse = new BaseResponse<>();

        failedResponse.setCode("404");
        failedResponse.setStatus(ResponseStatuses.FAILED_STATUS);
        failedResponse.setValue(
                new LoginResponseModelFAIL(
                        "USER_NOT_FOUND",
                        "User with specified username could not be found in database"));

        return failedResponse;
    }
}