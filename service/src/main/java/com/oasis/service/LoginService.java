package com.oasis.service;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    @Autowired
    private LoginRepository loginRepository;

    public EmployeeModel checkLoginCredentials(String username, String password) {
        EmployeeModel result = loginRepository.findByUsername(username);

        if (result != null){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(password, result.getPassword())) return null;
        }

        return result;
    }
}
