package com.hzcf.edge.components.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;


@Configuration
public class MongoTemplate {
    @Autowired
    @Qualifier("mongoProperties")
    private MongoProperties mongoProperties;

    @Primary
    @Bean(name = "apiDataMongoTemplate")
    public org.springframework.data.mongodb.core.MongoTemplate apiDataMongoTemplate() throws Exception {
        return new org.springframework.data.mongodb.core.MongoTemplate(mongoDbFactory(this.mongoProperties));
    }

    @Bean
    @Primary
    public MongoDbFactory mongoDbFactory(MongoProperties mongoProperties) throws Exception {
        ServerAddress serverAddress = new ServerAddress(mongoProperties.getUri());
        return new SimpleMongoDbFactory(new MongoClient(serverAddress), mongoProperties.getDatabase());

    }
}