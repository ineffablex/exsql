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

    // Properties for Primary DataSource CNLDB
    @Value("${app.datasource.cndl.ndbtype}")
    private int primaryCndlNDbType;
    @Value("${app.datasource.cndl.tns}")
    private String primaryCndlTns;
    @Value("${app.datasource.cndl.getpasswd-url:}")
    private String primaryCndlGetPasswdUrl;
    @Value("${app.datasource.cndl.getpasswd-dummy-password:}")
    private String primaryCndlGetPasswdDummyPassword;

    // Properties for Secondary DataSource CNLDB
    @Value("${app.datasource2.cndl.ndbtype}")
    private int secondaryCndlNDbType;
    @Value("${app.datasource2.cndl.tns}")
    private String secondaryCndlTns;
    @Value("${app.datasource2.cndl.getpasswd-url:}")
    private String secondaryCndlGetPasswdUrl;
    @Value("${app.datasource2.cndl.getpasswd-dummy-password:}")
    private String secondaryCndlGetPasswdDummyPassword;


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
            logger.error("Database username (spring.datasource.username) for primary datasource is not configured.");
            throw new IllegalStateException("Database username for primary datasource is not configured.");
        }

        logger.info("Attempting to retrieve dynamic password for primary datasource user: {}", username);
        // Using the specific method for primary, which uses @Value injected fields in the service
        String password = passwordRetrievalService.retrievePrimaryPassword(username);
        logger.info("Dynamic password retrieved successfully for primary user: {}. Proceeding to configure primary DataSource.", username);

        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(username)
                .password(password) // Use dynamically fetched password
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    // Configuration for the second data source
    @Bean(name = "dataSource2Properties")
    @ConfigurationProperties(prefix = "spring.datasource2")
    public DataSourceProperties dataSource2Properties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource2")
    public DataSource dataSource2(@org.springframework.beans.factory.annotation.Qualifier("dataSource2Properties") DataSourceProperties dataSource2Properties) {
        String username = dataSource2Properties.getUsername();
        if (!StringUtils.hasText(username)) {
            logger.error("Database username (spring.datasource2.username) for secondary datasource is not configured.");
            throw new IllegalStateException("Database username for secondary datasource is not configured.");
        }

        logger.info("Attempting to retrieve dynamic password for secondary datasource user: {}", username);
        String password = passwordRetrievalService.retrievePassword(
                username,
                secondaryCndlNDbType,
                secondaryCndlTns,
                secondaryCndlGetPasswdUrl,
                secondaryCndlGetPasswdDummyPassword,
                "secondary");
        logger.info("Dynamic password retrieved successfully for secondary user: {}. Proceeding to configure secondary DataSource.", username);

        return DataSourceBuilder.create()
                .url(dataSource2Properties.getUrl())
                .username(username)
                .password(password) // Use dynamically fetched password
                .driverClassName(dataSource2Properties.getDriverClassName())
                .build();
    }
} 