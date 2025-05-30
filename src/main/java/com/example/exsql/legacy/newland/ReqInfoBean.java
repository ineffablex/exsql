package com.example.exsql.legacy.newland;

/**
 * @author zxy
 * @version 2017/12/8 Description: Modified By:
 */
public class ReqInfoBean {

  // Server invocation address
  private String url = "";

  // Database type
  // 1. TT
  // 2. Oracle (Default)
  // 3. MySQL
  private int dbType = 2;

  // Database TNS
  private String dbTns = "";

  // Database username/schema
  private String dbUser = "";

  // Hostname
  private String hostName = "";

  // Host IP
  private String hostIp = "";

  // Application name
  private String hostApp = "";

  // Application check code
  private String appCode = "";

  // Database user password
  private String dbUserpwd = "";

  // Configuration file read flag: 1=Host, 2=JAR
  private String cfgFileFlag = "1";

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getDbType() {
    return dbType;
  }

  public void setDbType(int dbType) {
    this.dbType = dbType;
  }

  public String getDbTns() {
    return dbTns;
  }

  public void setDbTns(String dbTns) {
    this.dbTns = dbTns;
  }

  public String getDbUser() {
    return dbUser;
  }

  public void setDbUser(String dbUser) {
    this.dbUser = dbUser;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getHostIp() {
    return hostIp;
  }

  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }

  public String getHostApp() {
    return hostApp;
  }

  public void setHostApp(String hostApp) {
    this.hostApp = hostApp;
  }

  public String getAppCode() {
    return appCode;
  }

  public void setAppCode(String appCode) {
    this.appCode = appCode;
  }

  public String getDbUserpwd() {
    return dbUserpwd;
  }

  public void setDbUserpwd(String dbUserpwd) {
    this.dbUserpwd = dbUserpwd;
  }

  public String getCfgFileFlag() {
    return cfgFileFlag;
  }

  public void setCfgFileFlag(String cfgFileFlag) {
    this.cfgFileFlag = cfgFileFlag;
  }

  @Override
  public String toString() {
    return "ReqInfoBean{"
        + "url='"
        + url
        + '\''
        + ", dbType="
        + dbType
        + ", dbTns='"
        + dbTns
        + '\''
        + ", dbUser='"
        + dbUser
        + '\''
        + ", hostName='"
        + hostName
        + '\''
        + ", hostIp='"
        + hostIp
        + '\''
        + ", hostApp='"
        + hostApp
        + '\''
        + ", appCode='"
        + appCode
        + '\''
        + ", dbUserpwd='"
        + dbUserpwd
        + '\''
        + ", cfgFileFlag='"
        + cfgFileFlag
        + '\''
        + '}';
  }
}
