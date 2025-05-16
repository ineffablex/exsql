package com.example.exsql.legacy.newland;


import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author zxy
 * @version 2017/12/20
 *          Description:
 *          Modified By:
 */
public class DealXMLMgr {

    //组织请求报文
    public String encodeXML(ReqInfoBean bean) throws Exception {
        Document doc = XMLUtil.createDocument();
        Element root = doc.addElement("operation_in");
        Element content = root.addElement("content");

        content.addElement("dbtype").setText(String.valueOf(bean.getDbType()));
        content.addElement("dbtns").setText(bean.getDbTns());
        content.addElement("dbuser").setText(bean.getDbUser());
        content.addElement("hostname").setText(bean.getHostName());
        content.addElement("hostip").setText(bean.getHostIp());
        content.addElement("hostapp").setText(bean.getHostApp());
        content.addElement("appcode").setText(bean.getAppCode());
        content.addElement("dbuserpwd").setText(bean.getDbUserpwd());
        content.addElement("remark").setText(bean.getCfgFileFlag());

        return XMLUtil.toXML(doc, "UTF-8");
    }

    //解析返回报文
    public ResInfoBean decodeXML(Document doc) throws Exception{
        //获取根元素
        Element content = XMLUtil.child(doc.getRootElement() , "content");
        String resultCode = content.element("resultcode").getText();
        ResInfoBean resInfoBean = new ResInfoBean();
        resInfoBean.setResultCode(Integer.parseInt(resultCode));
        if(Integer.parseInt(resultCode) == 0)
        {
            resInfoBean.setErrorMsg(content.element("errormsg").getText());
        }else
        {
            String dbtns = content.element("dbtns").getText();
            String dbuser = content.element("dbuser").getText();
            String dbuserpwd = content.element("dbuserpwd").getText();
            String randomcode = content.element("randomcode").getText();

            resInfoBean.setDbTns(dbtns);
            resInfoBean.setDbUser(dbuser);
            resInfoBean.setDbUserpwd(dbuserpwd);
            resInfoBean.setRandomCode(randomcode);
        }
        return resInfoBean;
    }
}
