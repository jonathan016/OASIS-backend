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

import static com.oasis.webcontroller.MappingValues.LOGIN_MAPPING_VALUE;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @RequestMapping(value = LOGIN_MAPPING_VALUE, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody
    LoginResponseModel callLoginService(@RequestBody LoginRequestModel loginRequestModel){
        return loginService.findLoginCredentials(loginRequestModel.getUsername(), loginRequestModel.getPassword());
    }
}
