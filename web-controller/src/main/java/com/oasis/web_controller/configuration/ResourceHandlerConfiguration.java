package com.oasis.web_controller.configuration;

import com.oasis.model.constant.service_constant.ImageDirectoryConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = "com.oasis")
@PropertySource("classpath:environment.properties")
public class ResourceHandlerConfiguration
        implements WebMvcConfigurer {

    @Value("${resource.path.patterns}")
    private String pathPatterns;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler(pathPatterns)
                .addResourceLocations("file:" + ImageDirectoryConstant.ASSET_IMAGE_DIRECTORY);
    }

}