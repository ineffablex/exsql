package com.newland.computer.boss.bossbiz.bosscomponent.cachestore.datasource;
import java.io.*;
import java.net.InetAddress;
import java.util.Properties;

// Assuming placeholder/actual implementations for these will be provided by the user
import com.example.exsql.legacy.newland.DealXMLMgr;
import com.example.exsql.legacy.newland.ReqInfoBean;
import com.example.exsql.legacy.newland.ResInfoBean;
import com.example.exsql.legacy.newland.UigClient;
import com.example.exsql.legacy.newland.XMLUtil;
import com.example.exsql.legacy.newland.DecodeUtil;
import com.example.exsql.legacy.newland.HostUtil;
import com.example.exsql.legacy.newland.DecodeException;


/**
 * 数据库密码解密工具
 * Created by Owen on 2015/10/27.
 */
public class CNLDBConnectMgr
{
    private static DealXMLMgr dealXMLMgr;
    // 数据库连接池集合
    private static String appCheckCode;
    private static String appName;    
    private static String serverUrl; // This is the UIG server URL

    //TT数据库

    /**
     * 初始化工具，加载配置文件
     * @return 
     *
     * @return
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public static synchronized void init() throws Exception // Made synchronized
    {
        appCheckCode = "abcdefg";
        appName = "测试应用";
        serverUrl = "http://10.32.40.72:40001/securityserver-1.0-SNAPSHOT/security/querydbuserinfo.do"; // UIG Server URL
    }

    // Overload as specified by user
    public static String getPasswd(int nDbType, String tns, String sUser, String url, String passwd)
    {
        dealXMLMgr = new DealXMLMgr();
        //初始化REQ
        ReqInfoBean reqInfoBean = setReq(nDbType , tns , sUser , passwd);
        reqInfoBean.setCfgFileFlag("1");//表示配置文件存在
        try {
            String reqStr = dealXMLMgr.encodeXML(reqInfoBean);
            UigClient uigClient = new UigClient(reqInfoBean.getUrl());
            //发送报文
            String resMsg = uigClient.requestService(reqStr);
            //解析返回报文
            ResInfoBean resInfoBean = dealXMLMgr.decodeXML(XMLUtil.fromXML(resMsg , "UTF-8"));

            if(resInfoBean.getResultCode() == 1)
            {
                String randomCode = resInfoBean.getRandomCode();
                String desKey = randomCode + reqInfoBean.getAppCode();
                String dbPassword = resInfoBean.getDbUserpwd();
                String decodePassword = DecodeUtil.decrypt(dbPassword , desKey);

                return decodePassword;
            }
            else
            {
                throw new DecodeException("获取密码失败！！，错误信息：：" + resInfoBean.getErrorMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //初始化REQ
    private static ReqInfoBean setReq(int nDbType, String tns, String sUser, String passwd) {
        ReqInfoBean reqInfoBean=new ReqInfoBean();
        reqInfoBean.setDbType(nDbType);
        reqInfoBean.setDbTns(tns);
        reqInfoBean.setDbUser(sUser);
        reqInfoBean.setDbUserpwd(passwd);
        reqInfoBean.setUrl(serverUrl);
        reqInfoBean.setHostApp(appName);
        reqInfoBean.setAppCode(appCheckCode);
        //获取部署主机IP+主机名
        InetAddress netAddress = HostUtil.getInetAddress();
        //IP
        String hostIp = HostUtil.getHostIp(netAddress);
        reqInfoBean.setHostIp(hostIp);
        //主机名
        String hostName = HostUtil.getHostName(netAddress);
        reqInfoBean.setHostName(hostName);
        return reqInfoBean;
    }

} 