package com.oasis.service;

import com.oasis.exception.UserNotFoundException;
import com.oasis.model.EmployeeModel;
import com.oasis.repository.LoginRepository;
import com.oasis.webmodel.response.LoginResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    @Autowired
    private LoginRepository loginRepository;

    public LoginResponseModel findLoginCredentials(String username, String password) {
        EmployeeModel result = loginRepository.findByUsername(username);
        if (result == null) throw new UserNotFoundException("User not found!");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, result.getEmployeePassword())) throw new UserNotFoundException("Incorrect password!");

        LoginResponseModel response = new LoginResponseModel();
        response.set_id(result.get_id());

        return response;
    }
}
