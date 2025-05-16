package com.example.exsql.service;

import com.newland.computer.boss.bossbiz.bosscomponent.cachestore.datasource.CNLDBConnectMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;

@Service
public class CustomPasswordRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(CustomPasswordRetrievalService.class);

    @Value("${app.datasource.cndl.auth-file-path}")
    private String cndlAuthFilePath;

    @Value("${app.datasource.cndl.ndbtype}")
    private int cndlNDbType;

    @Value("${app.datasource.cndl.tns}")
    private String cndlTns;

    // This URL is passed directly to getPasswd. Its specific use depends on CNLDBConnectMgr's logic.
    @Value("${app.datasource.cndl.getpasswd-url:}") // Default to empty if not provided
    private String cndlGetPasswdUrl;

    // This dummy password is passed to getPasswd. 
    @Value("${app.datasource.cndl.getpasswd-dummy-password:}") // Default to empty if not provided
    private String cndlGetPasswdDummyPassword;

    private boolean initialized = false;

    @PostConstruct
    public void initialize() {
        try {
            CNLDBConnectMgr.init();
            initialized = true;
        } catch (Exception e) {
            logger.error("Failed to initialize CNLDBConnectMgr using auth file path: {}. Error: {}", cndlAuthFilePath, e.getMessage(), e);
            // Depending on policy, you might want to rethrow or prevent app startup
            throw new RuntimeException("Failed to initialize CNLDBConnectMgr: " + e.getMessage(), e);
        }
    }

    public String retrievePassword(String username) {
        if (!initialized) {
            logger.error("CNLDBConnectMgr was not initialized successfully. Cannot retrieve password.");
            throw new IllegalStateException("CNLDBConnectMgr not initialized.");
        }
        logger.info("Retrieving password using CNLDBConnectMgr for user: {}, tns: {}, nDbType: {}", username, cndlTns, cndlNDbType);
        logger.info("Parameters for CNLDBConnectMgr.getPasswd -> url: '{}', dummyPassword: '{}'", cndlGetPasswdUrl, "*****"); // Log dummy password carefully

        try {
            // Calling the specific getPasswd method as requested by the user:
            // String realPass = CNLDBConnectMgr.getPasswd(2, tns, userName, url, passWord);
            String password = CNLDBConnectMgr.getPasswd(cndlNDbType, cndlTns, username, cndlGetPasswdUrl, cndlGetPasswdDummyPassword);
            if (password == null) {
                logger.error("CNLDBConnectMgr.getPasswd returned null for user: {}", username);
                throw new RuntimeException("Failed to retrieve password using CNLDBConnectMgr, returned null.");
            }
            logger.info("Password retrieved successfully for user: {}", username);
            return password;
        } catch (Exception e) {
            logger.error("Error retrieving password using CNLDBConnectMgr for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Error retrieving password via CNLDBConnectMgr: " + e.getMessage(), e);
        }
    }
} 