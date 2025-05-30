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

import com.example.exsql.model.DataSourceDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties; // Keep for property reading if needed by a helper
import org.springframework.boot.context.properties.EnableConfigurationProperties; // If using @ConfigurationProperties on a class
import org.springframework.core.env.Environment; // Alternative for property reading

import javax.sql.DataSource; // Will be removed from direct usage here
import java.util.HashMap;
import java.util.Map;

@Configuration
// Consider @EnableConfigurationProperties if you create a dedicated properties class
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    // Note: CustomPasswordRetrievalService is still a bean and can be autowired elsewhere if needed,
    // but it's not directly used in this class anymore to create DataSource beans at startup.
    // @Autowired
    // private CustomPasswordRetrievalService passwordRetrievalService;

    // Primary DataSource Properties
    @Value("${spring.datasource.primary.url}")
    private String primaryUrl;
    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;
    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDriverClassName;
    @Value("${app.datasource.primary.cndl.auth-file-path}")
    private String primaryCndlAuthFilePath;
    @Value("${app.datasource.primary.cndl.ndbtype}")
    private int primaryCndlNdbType;
    @Value("${app.datasource.primary.cndl.tns}")
    private String primaryCndlTns;
    @Value("${app.datasource.primary.cndl.getpasswd-url}")
    private String primaryCndlGetPasswdUrl;
    @Value("${app.datasource.primary.cndl.getpasswd-dummy-password}")
    private String primaryCndlGetPasswdDummyPassword;
    @Value("${app.datasource.primary.http-password.url:}") // Optional with default
    private String primaryHttpPasswordUrl;
    @Value("${app.datasource.primary.http-password.request-timeout-ms:5000}") // Optional with default
    private int primaryHttpPasswordRequestTimeoutMs;

    // Secondary DataSource Properties
    @Value("${spring.datasource.secondary.url}")
    private String secondaryUrl;
    @Value("${spring.datasource.secondary.username}")
    private String secondaryUsername;
    @Value("${spring.datasource.secondary.driver-class-name}")
    private String secondaryDriverClassName;
    @Value("${app.datasource.secondary.cndl.auth-file-path}")
    private String secondaryCndlAuthFilePath;
    @Value("${app.datasource.secondary.cndl.ndbtype}")
    private int secondaryCndlNdbType;
    @Value("${app.datasource.secondary.cndl.tns}")
    private String secondaryCndlTns;
    @Value("${app.datasource.secondary.cndl.getpasswd-url}")
    private String secondaryCndlGetPasswdUrl;
    @Value("${app.datasource.secondary.cndl.getpasswd-dummy-password}")
    private String secondaryCndlGetPasswdDummyPassword;

    @Bean
    public Map<String, DataSourceDefinition> dataSourceDefinitions() {
        Map<String, DataSourceDefinition> definitions = new HashMap<>();

        // Create Primary DataSourceDefinition
        if (!StringUtils.hasText(primaryUrl) || !StringUtils.hasText(primaryUsername) || !StringUtils.hasText(primaryDriverClassName)) {
            logger.error("Missing critical configuration for primary data source (url, username, or driverClassName).");
            // Depending on policy, could throw an exception here to prevent app startup
            // For now, just log and don't add it to definitions
        } else {
            DataSourceDefinition primaryDef = new DataSourceDefinition(
                    "primary", primaryUrl, primaryUsername, primaryDriverClassName,
                    primaryCndlAuthFilePath, primaryCndlNdbType, primaryCndlTns,
                    primaryCndlGetPasswdUrl, primaryCndlGetPasswdDummyPassword
            );
            if (StringUtils.hasText(primaryHttpPasswordUrl)) {
                primaryDef.setHttpPasswordUrl(primaryHttpPasswordUrl);
                primaryDef.setHttpPasswordRequestTimeoutMs(primaryHttpPasswordRequestTimeoutMs);
            }
            definitions.put("primary", primaryDef);
            logger.info("Primary data source definition created: {}", primaryDef.getName());
        }

        // Create Secondary DataSourceDefinition
        if (!StringUtils.hasText(secondaryUrl) || !StringUtils.hasText(secondaryUsername) || !StringUtils.hasText(secondaryDriverClassName)) {
            logger.error("Missing critical configuration for secondary data source (url, username, or driverClassName).");
        } else {
            DataSourceDefinition secondaryDef = new DataSourceDefinition(
                    "secondary", secondaryUrl, secondaryUsername, secondaryDriverClassName,
                    secondaryCndlAuthFilePath, secondaryCndlNdbType, secondaryCndlTns,
                    secondaryCndlGetPasswdUrl, secondaryCndlGetPasswdDummyPassword
            );
            // Secondary doesn't have HTTP password fields in current setup
            definitions.put("secondary", secondaryDef);
            logger.info("Secondary data source definition created: {}", secondaryDef.getName());
        }
        
        if (definitions.isEmpty()) {
            logger.warn("No data source definitions were successfully created. Check application.properties for errors.");
        }
        return definitions;
    }
} 