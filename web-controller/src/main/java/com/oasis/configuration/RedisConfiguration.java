package com.oasis.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableCaching
@EnableRedisHttpSession
@ComponentScan(basePackages = "com.oasis")
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplateObject() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory());
        return template;
    }

    @Bean
    JedisConnectionFactory connectionFactory() {

        return new JedisConnectionFactory();
    }

    @Bean
    public RedisCacheManager cacheManager() {

        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplateObject());
        redisCacheManager.setTransactionAware(true);
        return redisCacheManager;
    }

}
