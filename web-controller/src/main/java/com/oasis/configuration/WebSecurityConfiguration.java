package com.oasis.configuration;

import com.oasis.provider.OasisAuthenticationProvider;
import com.oasis.service.ServiceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class WebSecurityConfiguration
        extends WebSecurityConfigurerAdapter {

    @Autowired
    private OasisAuthenticationProvider oasisAuthenticationProvider;

    @Override
    protected void configure(
            AuthenticationManagerBuilder auth
    )
            throws
            Exception {

        auth.authenticationProvider(oasisAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http)
            throws
            Exception {

        http.csrf().disable().authorizeRequests().anyRequest().permitAll();
//        http.csrf().disable().anonymous().and().authorizeRequests()
//            .antMatchers(HttpMethod.POST, "api/login").authenticated()
//            .antMatchers(HttpMethod.GET, "api/dashboard/*").authenticated()
//            .antMatchers(HttpMethod.GET, "api/assets/list").authenticated()
//            .antMatchers(HttpMethod.GET, "api/employees/list").authenticated()
//            .antMatchers(HttpMethod.POST, "api/assets/save").hasRole(ServiceConstant.ROLE_ADMINISTRATOR)
//            .antMatchers(HttpMethod.POST, "api/employees/save").hasRole(ServiceConstant.ROLE_ADMINISTRATOR)
//            .antMatchers(HttpMethod.DELETE, "api/assets/delete").hasRole(ServiceConstant.ROLE_ADMINISTRATOR)
//            .antMatchers(HttpMethod.DELETE, "api/employees/delete").hasRole(ServiceConstant.ROLE_ADMINISTRATOR)
//            .antMatchers(HttpMethod.GET, "api/requests/*").permitAll()
//            .anyRequest().authenticated().and().httpBasic();
    }

}
