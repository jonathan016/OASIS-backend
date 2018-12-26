package com.oasis.configuration;

import com.oasis.security.OasisAccessDeniedHandler;
import com.oasis.security.OasisAuthenticationProvider;
import com.oasis.security.OasisRestAuthenticationEntryPoint;
import com.oasis.tool.constant.RoleConstant;
import com.oasis.web_model.constant.APIMappingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.oasis")
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class WebSecurityConfiguration
        extends WebSecurityConfigurerAdapter {

    @Autowired
    private OasisAccessDeniedHandler oasisAccessDeniedHandler;
    @Autowired
    private OasisAuthenticationProvider oasisAuthenticationProvider;
    @Autowired
    private OasisRestAuthenticationEntryPoint oasisRestAuthenticationEntryPoint;

    public WebSecurityConfiguration() {

        super();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {

        auth.authenticationProvider(oasisAuthenticationProvider);
    }

    @Override
    protected void configure(final HttpSecurity http)
            throws
            Exception {

        http.cors().and().csrf().disable().anonymous().and().httpBasic().and().authorizeRequests()
                .antMatchers(HttpMethod.POST, APIMappingValue.API_LOGIN).permitAll()
                .antMatchers(HttpMethod.POST, APIMappingValue.API_LOGOUT).authenticated()
                .antMatchers(HttpMethod.GET, APIMappingValue.API_DASHBOARD + "/**").authenticated()
                .antMatchers(HttpMethod.GET, APIMappingValue.API_ASSET + APIMappingValue.API_LIST).authenticated()
                .antMatchers(HttpMethod.POST, APIMappingValue.API_ASSET + APIMappingValue.API_SAVE)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR)
                .antMatchers(HttpMethod.DELETE, APIMappingValue.API_ASSET + APIMappingValue.API_DELETE)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR).antMatchers(HttpMethod.GET, APIMappingValue.API_ASSET + "/**")
                .authenticated().antMatchers(HttpMethod.GET, APIMappingValue.API_EMPLOYEE + APIMappingValue.API_LIST)
                .authenticated().antMatchers(HttpMethod.POST, APIMappingValue.API_EMPLOYEE + APIMappingValue.API_SAVE)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR).antMatchers(HttpMethod.POST, APIMappingValue.API_EMPLOYEE +
                APIMappingValue.API_CHANGE_SUPERVISOR_ON_DELETE)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR)
                .antMatchers(HttpMethod.GET, APIMappingValue.API_EMPLOYEE + APIMappingValue.API_USERNAMES)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR)
                .antMatchers(HttpMethod.DELETE, APIMappingValue.API_EMPLOYEE + APIMappingValue.API_DELETE)
                .hasRole(RoleConstant.ROLE_ADMINISTRATOR)
                .antMatchers(HttpMethod.POST, APIMappingValue.API_EMPLOYEE + APIMappingValue.API_PASSWORD_CHANGE)
                .authenticated().antMatchers(HttpMethod.GET, APIMappingValue.API_EMPLOYEE + "/**").authenticated()
                .antMatchers(HttpMethod.GET, APIMappingValue.API_REQUEST + APIMappingValue.API_LIST).authenticated()
                .antMatchers(HttpMethod.GET, APIMappingValue.API_REQUEST + APIMappingValue.API_MY_REQUESTS).authenticated()
                .antMatchers(HttpMethod.GET, APIMappingValue.API_REQUEST + APIMappingValue.API_MY_REQUESTS)
                .hasAnyRole(new String[]{RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR})
                .antMatchers(HttpMethod.POST, APIMappingValue.API_REQUEST + APIMappingValue.API_SAVE)
                .hasAnyAuthority(new String[]{
                        RoleConstant.ROLE_ADMINISTRATOR, RoleConstant.ROLE_SUPERIOR, RoleConstant.ROLE_EMPLOYEE
                }).anyRequest().authenticated().and().exceptionHandling()
                .authenticationEntryPoint(oasisRestAuthenticationEntryPoint).accessDeniedHandler(oasisAccessDeniedHandler);
    }

}
