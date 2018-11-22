package com.oasis.configuration;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebMvc
@MultipartConfig(fileSizeThreshold = 1048576, location = "C:\\oasis\\images", maxFileSize = 20848820, maxRequestSize
        = 418018841)
@ComponentScan(basePackages = "com.oasis")
public class ExtendedMultipartResolver extends CommonsMultipartResolver {

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return (request != null && isMultipartContent(request));
    }

    public static final boolean isMultipartContent(HttpServletRequest request) {
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        if (HttpMethod.POST != httpMethod && HttpMethod.PUT != httpMethod) {
            return false;
        }
        return FileUploadBase.isMultipartContent(new ServletRequestContext(request));
    }
}
