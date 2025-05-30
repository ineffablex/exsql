package com.example.exsql.service;

import com.newland.computer.boss.bossbiz.bosscomponent.cachestore.datasource.CNLDBConnectMgr;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CustomPasswordRetrievalService {

  private static final Logger logger =
      LoggerFactory.getLogger(CustomPasswordRetrievalService.class);

  @Value("${app.datasource.cndl.auth-file-path}")
  private String cndlAuthFilePath; // This will be used for the primary datasource init

  // Primary datasource CNLDB parameters - kept for @PostConstruct initialization
  @Value("${app.datasource.cndl.ndbtype}")
  private int primaryCndlNDbType;

  @Value("${app.datasource.cndl.tns}")
  private String primaryCndlTns;

  @Value("${app.datasource.cndl.getpasswd-url:}")
  private String primaryCndlGetPasswdUrl;

  @Value("${app.datasource.cndl.getpasswd-dummy-password:}")
  private String primaryCndlGetPasswdDummyPassword;

  private boolean cndlMgrInitialized = false;

  @PostConstruct
  public void initialize() {
    // Initialize CNLDBConnectMgr using properties for the primary datasource's auth file.
    // This assumes CNLDBConnectMgr.init() is a one-time global setup and subsequent
    // getPasswd calls can target different TNS/users if the underlying auth mechanism supports it.
    // If different auth files *must* be loaded by init() for different datasources,
    // this approach will need significant change (e.g. multiple CNLDBConnectMgr instances or
    // re-init capability).
    logger.info("Initializing CNLDBConnectMgr with primary auth file path: {}", cndlAuthFilePath);
    try {
      // Assuming cndlAuthFilePath is correctly injected for the primary datasource's init file
      // If CNLDBConnectMgr uses this path internally after init, it might be an issue.
      // For now, we proceed with the assumption that init() is for global setup.
      CNLDBConnectMgr
          .init(); // This might need to be called with auth-file path if the lib supports it
      cndlMgrInitialized = true;
      logger.info("CNLDBConnectMgr initialized successfully.");
    } catch (Exception e) {
      logger.error("Failed to initialize CNLDBConnectMgr. Error: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to initialize CNLDBConnectMgr: " + e.getMessage(), e);
    }
  }

  // Method for primary datasource, using fields initialized by @Value
  public String retrievePrimaryPassword(String username) {
    if (!cndlMgrInitialized) {
      logger.error(
          "CNLDBConnectMgr was not initialized. Cannot retrieve password for primary datasource.");
      throw new IllegalStateException("CNLDBConnectMgr not initialized for primary datasource.");
    }
    logger.info(
        "Retrieving password for primary datasource user: {}, tns: {}, nDbType: {}",
        username,
        primaryCndlTns,
        primaryCndlNDbType);
    logger.info(
        "Primary Parameters for CNLDBConnectMgr.getPasswd -> url: '{}', dummyPassword: '{}'",
        primaryCndlGetPasswdUrl,
        "*****");

    try {
      String password =
          CNLDBConnectMgr.getPasswd(
              primaryCndlNDbType,
              primaryCndlTns,
              username,
              primaryCndlGetPasswdUrl,
              primaryCndlGetPasswdDummyPassword);
      if (password == null) {
        logger.error("CNLDBConnectMgr.getPasswd returned null for primary user: {}", username);
        throw new RuntimeException(
            "Failed to retrieve password for primary datasource, CNLDBConnectMgr returned null.");
      }
      logger.info("Password retrieved successfully for primary user: {}", username);
      return password;
    } catch (Exception e) {
      logger.error(
          "Error retrieving password for primary user {} using CNLDBConnectMgr: {}",
          username,
          e.getMessage(),
          e);
      throw new RuntimeException(
          "Error retrieving password for primary datasource via CNLDBConnectMgr: " + e.getMessage(),
          e);
    }
  }

  // Overloaded method to accept specific CNLDB parameters for any datasource
  public String retrievePassword(
      String username,
      int nDbType,
      String tns,
      String getPasswdUrl,
      String dummyPassword,
      String dsName) {
    if (!cndlMgrInitialized) {
      logger.error(
          "CNLDBConnectMgr was not initialized. Cannot retrieve password for datasource: {}",
          dsName);
      throw new IllegalStateException("CNLDBConnectMgr not initialized for datasource: " + dsName);
    }
    logger.info(
        "Retrieving password for {} datasource user: {}, tns: {}, nDbType: {}",
        dsName,
        username,
        tns,
        nDbType);
    logger.info(
        "{} Parameters for CNLDBConnectMgr.getPasswd -> url: '{}', dummyPassword: '{}'",
        StringUtils.capitalize(dsName),
        getPasswdUrl,
        "*****");

    try {
      String password =
          CNLDBConnectMgr.getPasswd(nDbType, tns, username, getPasswdUrl, dummyPassword);
      if (password == null) {
        logger.error("CNLDBConnectMgr.getPasswd returned null for {} user: {}", dsName, username);
        throw new RuntimeException(
            "Failed to retrieve password for "
                + dsName
                + " datasource, CNLDBConnectMgr returned null.");
      }
      logger.info("Password retrieved successfully for {} user: {}", dsName, username);
      return password;
    } catch (Exception e) {
      logger.error(
          "Error retrieving password for {} user {} using CNLDBConnectMgr: {}",
          dsName,
          username,
          e.getMessage(),
          e);
      throw new RuntimeException(
          "Error retrieving password for "
              + dsName
              + " datasource via CNLDBConnectMgr: "
              + e.getMessage(),
          e);
    }
  }
}
