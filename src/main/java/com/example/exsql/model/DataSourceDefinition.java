package com.example.exsql.model;

public class DataSourceDefinition {

  private String name;
  private String url;
  private String username;
  private String driverClassName;
  private String cndlAuthFilePath;
  private int cndlNdbType;
  private String cndlTns;
  private String cndlGetPasswdUrl;
  private String cndlGetPasswdDummyPassword;
  private String httpPasswordUrl; // Optional, only for primary in current setup
  private int httpPasswordRequestTimeoutMs; // Optional

  // Constructor
  public DataSourceDefinition(
      String name,
      String url,
      String username,
      String driverClassName,
      String cndlAuthFilePath,
      int cndlNdbType,
      String cndlTns,
      String cndlGetPasswdUrl,
      String cndlGetPasswdDummyPassword) {
    this.name = name;
    this.url = url;
    this.username = username;
    this.driverClassName = driverClassName;
    this.cndlAuthFilePath = cndlAuthFilePath;
    this.cndlNdbType = cndlNdbType;
    this.cndlTns = cndlTns;
    this.cndlGetPasswdUrl = cndlGetPasswdUrl;
    this.cndlGetPasswdDummyPassword = cndlGetPasswdDummyPassword;
  }

  // Getters
  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public String getCndlAuthFilePath() {
    return cndlAuthFilePath;
  }

  public int getCndlNdbType() {
    return cndlNdbType;
  }

  public String getCndlTns() {
    return cndlTns;
  }

  public String getCndlGetPasswdUrl() {
    return cndlGetPasswdUrl;
  }

  public String getCndlGetPasswdDummyPassword() {
    return cndlGetPasswdDummyPassword;
  }

  public String getHttpPasswordUrl() {
    return httpPasswordUrl;
  }

  public int getHttpPasswordRequestTimeoutMs() {
    return httpPasswordRequestTimeoutMs;
  }

  // Setters for optional HTTP password fields
  public void setHttpPasswordUrl(String httpPasswordUrl) {
    this.httpPasswordUrl = httpPasswordUrl;
  }

  public void setHttpPasswordRequestTimeoutMs(int httpPasswordRequestTimeoutMs) {
    this.httpPasswordRequestTimeoutMs = httpPasswordRequestTimeoutMs;
  }
}
