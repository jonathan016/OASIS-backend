package com.oasis.service.implementation;

import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.LoginRepository;
import com.oasis.service.api.LoginServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginServiceApi {
    @Autowired
    private LoginRepository loginRepository;

    @Override
    public EmployeeModel checkLoginCredentials(String username, String password) {
        EmployeeModel result = loginRepository.findByUsername(username);

        if (result != null){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(password, result.getPassword())) {
                result.setPassword(null);

                return result;
            }
        }

        return result;
    }
}
