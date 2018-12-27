package com.oasis.initializer;

import com.oasis.configuration.DatabaseConfiguration;
import com.oasis.configuration.MultipartResolverConfiguration;
import com.oasis.configuration.MvcConfiguration;
import com.oasis.configuration.RedisConfiguration;
import com.oasis.configuration.ResourceHandlerConfiguration;
import com.oasis.configuration.WebSecurityConfiguration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApplicationInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class< ? >[] getRootConfigClasses() {

        return new Class[]{
                DatabaseConfiguration.class,
                MultipartResolverConfiguration.class,
                RedisConfiguration.class,
                ResourceHandlerConfiguration.class,
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
