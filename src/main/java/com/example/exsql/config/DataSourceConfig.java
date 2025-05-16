package com.example.exsql.config;

import com.example.exsql.service.CustomPasswordRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private CustomPasswordRetrievalService passwordRetrievalService;

    // Inject standard DataSourceProperties to get URL, username, driver etc.
    // We will override the password.
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String username = properties.getUsername();
        if (!StringUtils.hasText(username)) {
            logger.error("Database username (spring.datasource.username) is not configured.");
            throw new IllegalStateException("Database username is not configured.");
        }

        logger.info("Attempting to retrieve dynamic password for datasource user: {}", username);
        String password = passwordRetrievalService.retrievePassword(username);
        logger.info("Dynamic password retrieved successfully for user: {}. Proceeding to configure DataSource.", username);

        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(username)
                .password(password) // Use dynamically fetched password
                .driverClassName(properties.getDriverClassName())
                .build();
    }
} 