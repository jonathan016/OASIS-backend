package com.oasis.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;

@Configuration
@EnableRedisHttpSession
@ComponentScan(basePackages = { "com.oasis", "com.oasis.repository" })
public class HttpSessionConfiguration {

    @Bean
    public LettuceConnectionFactory connectionFactory() {

        return new LettuceConnectionFactory();
    }

}
