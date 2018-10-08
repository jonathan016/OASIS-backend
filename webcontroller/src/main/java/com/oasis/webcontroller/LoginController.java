package com.oasis.webcontroller;

import com.oasis.service.LoginService;
import com.oasis.webmodel.request.LoginRequestModel;
import com.oasis.webmodel.response.LoginResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    LoginResponseModel callLoginService(@RequestBody LoginRequestModel loginRequestModel){
        return loginService.findLoginCredentials(loginRequestModel.getEmployeeUsername(), loginRequestModel.getEmployeePassword());
    }
}
