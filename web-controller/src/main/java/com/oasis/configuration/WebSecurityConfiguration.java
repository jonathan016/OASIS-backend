package com.oasis.configuration;

import com.oasis.security.OasisAccessDeniedHandler;
import com.oasis.security.OasisAuthenticationFailureHandler;
import com.oasis.security.OasisAuthenticationProvider;
import com.oasis.security.OasisRestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.oasis.tool.constant.RoleConstant.ROLE_ADMINISTRATOR;
import static com.oasis.tool.constant.RoleConstant.ROLE_EMPLOYEE;
import static com.oasis.tool.constant.RoleConstant.ROLE_SUPERIOR;
import static com.oasis.web_model.constant.APIMappingValue.*;

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
    private OasisAuthenticationFailureHandler oasisAuthenticationFailureHandler;
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
    public void configure(WebSecurity web)
            throws
            Exception {

        web.ignoring().antMatchers(HttpMethod.GET, API_ASSET.concat(API_IMAGE_ASSET))
           .antMatchers(HttpMethod.GET, API_EMPLOYEE.concat(API_PHOTO_EMPLOYEE));
    }

    @Override
    protected void configure(final HttpSecurity http)
            throws
            Exception {

        http.cors().configurationSource(corsConfigurationSource()).and().csrf().disable().anonymous()
            .and().authorizeRequests()
            .antMatchers(HttpMethod.POST, API_LOGIN).permitAll()
            .antMatchers(HttpMethod.POST, API_LOGOUT).authenticated()
            .antMatchers(HttpMethod.GET, API_SIDE_BAR).authenticated()
            .antMatchers(HttpMethod.GET, API_DASHBOARD.concat("/**")).authenticated()
            .antMatchers(HttpMethod.GET, API_ASSET.concat(API_LIST)).authenticated()
            .antMatchers(HttpMethod.POST, API_ASSET.concat(API_SAVE))
            .hasAuthority(ROLE_ADMINISTRATOR)
            .antMatchers(HttpMethod.DELETE, API_ASSET.concat(API_DELETE))
            .hasAuthority(ROLE_ADMINISTRATOR).antMatchers(HttpMethod.GET, API_ASSET.concat("/**"))
            .authenticated().antMatchers(HttpMethod.GET, API_EMPLOYEE.concat(API_LIST))
            .authenticated().antMatchers(HttpMethod.POST, API_EMPLOYEE.concat(API_SAVE))
            .hasAuthority(ROLE_ADMINISTRATOR).antMatchers(
                HttpMethod.POST, API_EMPLOYEE.concat(API_CHANGE_SUPERVISOR_ON_DELETE))
            .hasAuthority(ROLE_ADMINISTRATOR)
            .antMatchers(HttpMethod.GET, API_EMPLOYEE.concat(API_USERNAMES))
            .hasAuthority(ROLE_ADMINISTRATOR)
            .antMatchers(HttpMethod.DELETE, API_EMPLOYEE.concat(API_DELETE))
            .hasAuthority(ROLE_ADMINISTRATOR)
            .antMatchers(HttpMethod.POST, API_EMPLOYEE.concat(API_PASSWORD_CHANGE))
            .authenticated().antMatchers(HttpMethod.GET, API_EMPLOYEE.concat("/**")).authenticated()
            .antMatchers(HttpMethod.GET, API_REQUEST.concat(API_LIST)).authenticated()
            .antMatchers(HttpMethod.GET, API_REQUEST.concat(API_MY_REQUESTS)).authenticated()
            .antMatchers(HttpMethod.GET, API_REQUEST.concat(API_MY_REQUESTS))
            .hasAnyAuthority(ROLE_ADMINISTRATOR, ROLE_SUPERIOR)
            .antMatchers(HttpMethod.POST, API_REQUEST.concat(API_SAVE))
            .hasAnyAuthority(ROLE_ADMINISTRATOR, ROLE_SUPERIOR, ROLE_EMPLOYEE).anyRequest().authenticated()
            .and().requestCache().requestCache(new NullRequestCache()).and().httpBasic()
            .and().sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(true).sessionRegistry(
                sessionRegistry())
            .and().sessionAuthenticationFailureHandler(oasisAuthenticationFailureHandler).and().exceptionHandling()
            .authenticationEntryPoint(oasisRestAuthenticationEntryPoint).accessDeniedHandler(oasisAccessDeniedHandler);
    }

    @Bean
    SessionRegistry sessionRegistry() {

        return new SessionRegistryImpl();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost", "*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Access-Control-Allow-Origin", "X-Auth-Token",
                                                      "Access-Control-Allow-Headers", "Authorization",
                                                      "X-Requested-With", "requestId", "Correlation-Id"
        ));
        configuration.setExposedHeaders(Arrays.asList("Content-Type", "Access-Control-Allow-Origin", "X-Auth-Token",
                                                      "Access-Control-Allow-Headers", "Authorization",
                                                      "X-Requested-With", "requestId", "Correlation-Id"
        ));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

}
