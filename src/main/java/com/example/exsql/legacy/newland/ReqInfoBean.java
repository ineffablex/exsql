package com.example.exsql.legacy.newland;

/**
 * @author zxy
 * @version 2017/12/8
 *          Description:
 *          Modified By:
 */
public class ReqInfoBean {

    /*服务端调用地址*/
    private String url="";

    /*  数据库类型
        1、TT
        2、Oracle  默认
        3、mysql
    */
    private int dbType=2;

    /*数据库Tns*/
    private String dbTns="";

    /*用户模式*/
    private String dbUser="";

    /*主机名*/
    private String hostName="";

    /*主机IP*/
    private String hostIp="";

    /*应用名*/
    private String hostApp="";

    /*校验码*/
    private String appCode="";

    /*密码*/
    private String dbUserpwd="";

    /*配置文件读取标记:1：主机，2：jar*/
    private String cfgFileFlag="1";

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
        return "ReqInfoBean{" +
                "url='" + url + '\'' +
                ", dbType=" + dbType +
                ", dbTns='" + dbTns + '\'' +
                ", dbUser='" + dbUser + '\'' +
                ", hostName='" + hostName + '\'' +
                ", hostIp='" + hostIp + '\'' +
                ", hostApp='" + hostApp + '\'' +
                ", appCode='" + appCode + '\'' +
                ", dbUserpwd='" + dbUserpwd + '\'' +
                ", cfgFileFlag='" + cfgFileFlag + '\'' +
                '}';
    }
}
