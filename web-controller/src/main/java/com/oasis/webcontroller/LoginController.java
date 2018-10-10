package com.oasis.webcontroller;

import com.oasis.MappingValues;
import com.oasis.model.EmployeeModel;
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

    @RequestMapping(value = MappingValues.LOGIN_MAPPING_VALUE, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public BaseResponse callLoginService(@RequestBody LoginRequestModel loginRequestModel){
        EmployeeModel result = loginService.checkLoginCredentials(loginRequestModel.getUsername(), loginRequestModel.getPassword());

        if(result == null) {
            BaseResponse<LoginResponseModelFAIL> response = new BaseResponse<>();
            response.setCode("404");
            response.setStatus(ResponseStatuses.FAILED_STATUS);
            response.setValue(new LoginResponseModelFAIL("USER_NOT_FOUND", "User with specified username could not be found in database"));

            return response;
        }

        BaseResponse response = new BaseResponse();
        response.setCode("200");
        response.setStatus(ResponseStatuses.SUCCESS_STATUS);
        response.setValue(new LoginResponseModelOK(result.get_id()));

        return response;
    }
}