package com.oasis.repository.test.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "com.oasis")
@Profile("mvc-test")
public class MvcTestConfiguration
        implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("http://localhost", "*")
                .allowedHeaders("Content-Type", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers",
                        "Authorization", "X-Requested-With", "requestId", "Correlation-Id", "X-Auth-Token"
                )
                .exposedHeaders("Content-Type", "Access-Control-Allow-Origin", "Access-Control-Allow-Headers",
                        "Authorization", "X-Requested-With", "requestId", "Correlation-Id", "X-Auth-Token"
                )
                .allowCredentials(false)
                .maxAge(1800);
    }

}