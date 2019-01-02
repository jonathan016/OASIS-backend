package com.oasis.web_controller.initializer;

import com.oasis.web_controller.configuration.DatabaseConfiguration;
import com.oasis.web_controller.configuration.MultipartResolverConfiguration;
import com.oasis.web_controller.configuration.MvcConfiguration;
import com.oasis.web_controller.configuration.RedisConfiguration;
import com.oasis.web_controller.configuration.WebSecurityConfiguration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApplicationInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class< ? >[] getRootConfigClasses() {

        return new Class[]{
                DatabaseConfiguration.class,
                MultipartResolverConfiguration.class,
                RedisConfiguration.class,
                WebSecurityConfiguration.class
        };
    }

    @Override
    protected Class< ? >[] getServletConfigClasses() {

        return new Class[]{ MvcConfiguration.class };
    }

    @Override
    protected String[] getServletMappings() {

        return new String[]{ "/" };
    }

}
