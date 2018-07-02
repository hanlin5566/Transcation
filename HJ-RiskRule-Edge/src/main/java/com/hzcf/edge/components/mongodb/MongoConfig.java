package com.hzcf.edge.components.mongodb;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class MongoConfig {
    @Primary
    @Bean(name="mongoProperties")
    @ConfigurationProperties(prefix="spring.data.mongodb")
    public MongoProperties apiMongoProperties() {
        return new MongoProperties();
    }


}