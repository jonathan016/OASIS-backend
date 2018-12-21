package com.oasis.provider;

import com.oasis.exception.DataNotFoundException;
import com.oasis.model.entity.EmployeeModel;
import com.oasis.repository.EmployeeRepository;
import com.oasis.tool.helper.RoleDeterminer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class OasisAuthenticationService
        implements UserDetailsService {

    @Autowired
    private RoleDeterminer roleDeterminer;
    @Autowired
    private EmployeeRepository employeeRepository;

    public UsernamePasswordAuthenticationToken authenticateUser(
            final String username, final String password
    )
            throws
            DataNotFoundException {

        final EmployeeModel possibleEmployee = employeeRepository.findByDeletedIsFalseAndUsername(username);

        if (possibleEmployee == null) {
            return null;
        } else {
            final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            final String role = roleDeterminer.determineRole(username);
            List< GrantedAuthority > grantedAuthorities = new ArrayList<>();

            if (!encoder.matches(password, possibleEmployee.getPassword())) {
                return null;
            } else {
                grantedAuthorities.add(new SimpleGrantedAuthority(role));
            }

            return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String s)
            throws
            UsernameNotFoundException {

        return null;
    }

}
