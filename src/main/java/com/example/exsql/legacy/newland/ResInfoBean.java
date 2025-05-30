package com.example.exsql.legacy.newland;

/**
 * @author zxy
 * @version 2017/12/8 Description: Modified By:
 */
public class ResInfoBean {

  // Query result code
  private int resultCode = 1;

  // Error description
  private String errorMsg = "";

  // Database TNS
  private String dbTns = "";

  // Database username/schema
  private String dbUser = "";

  // Database user password
  private String dbUserpwd = "";

  // Random code
  private String randomCode = "";

  public int getResultCode() {
    return resultCode;
  }

  public void setResultCode(int resultCode) {
    this.resultCode = resultCode;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
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

  public String getDbUserpwd() {
    return dbUserpwd;
  }

  public void setDbUserpwd(String dbUserpwd) {
    this.dbUserpwd = dbUserpwd;
  }

  public String getRandomCode() {
    return randomCode;
  }

  public void setRandomCode(String randomCode) {
    this.randomCode = randomCode;
  }

  @Override
  public String toString() {
    return "ResInfoBean{"
        + "resultCode="
        + resultCode
        + ", errorMsg='"
        + errorMsg
        + '\''
        + ", dbTns='"
        + dbTns
        + '\''
        + ", dbUser='"
        + dbUser
        + '\''
        + ", dbUserpwd='"
        + dbUserpwd
        + '\''
        + ", randomCode='"
        + randomCode
        + '\''
        + '}';
  }
}
