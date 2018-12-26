package com.oasis.configuration;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@PropertySource("classpath:environment.properties")
@EnableMongoRepositories(basePackages = "com.oasis.repository")
@ComponentScan(basePackages = {"com.oasis", "com.oasis.repository"})
public class DatabaseConfiguration {

    @Value("${mongodb.host}")
    private String host;

    @Value("${mongodb.port}")
    private int port;

    @Value("${mongodb.database.name}")
    private String databaseName;

    @Bean
    public MongoTemplate mongoTemplate() {

        return new MongoTemplate(mongoDbFactory());
    }

    @Bean
    public MongoDbFactory mongoDbFactory() {

        MongoClient mongoClient = new MongoClient(host, port);
        return new SimpleMongoDbFactory(mongoClient, databaseName);
    }

}
