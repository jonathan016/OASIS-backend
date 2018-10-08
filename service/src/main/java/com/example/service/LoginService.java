package com.example.service;

import com.example.exception.UserNotFoundException;
import com.example.model.EmployeeModel;
import com.example.repository.LoginRepository;
import com.example.webmodel.response.LoginResponseModel;
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
