package com.oasis.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

@Component
@EnableCaching
public class RedisConfiguration {

    @Bean
    public RedisTemplate< String, Object > redisTemplateObject() {

        RedisTemplate< String, Object > template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {

        return new JedisConnectionFactory();
    }

    @Bean
    public RedisCacheManager cacheManager() {

        RedisCacheManager redisCacheManager = RedisCacheManager.create(jedisConnectionFactory());
        redisCacheManager.setTransactionAware(true);
        return redisCacheManager;
    }

}
