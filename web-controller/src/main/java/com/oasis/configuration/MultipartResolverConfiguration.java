package com.oasis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.annotation.MultipartConfig;

@Configuration
@EnableWebMvc
@MultipartConfig(fileSizeThreshold = 1048576, location = "C:\\oasis\\images", maxFileSize = 20848820,
                 maxRequestSize = 418018841)
@ComponentScan(basePackages = "com.oasis")
public class MultipartResolverConfiguration {

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(20971520);
        multipartResolver.setMaxInMemorySize(1048576);
        return multipartResolver;
    }

}
