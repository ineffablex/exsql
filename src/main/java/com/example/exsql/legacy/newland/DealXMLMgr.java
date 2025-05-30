package com.example.exsql.legacy.newland;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author zxy
 * @version 2017/12/20 Description: Modified By:
 */
public class DealXMLMgr {

  // Build request XML
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

  // Parse response XML
  public ResInfoBean decodeXML(Document doc) throws Exception {
    // Get content element
    Element content = XMLUtil.child(doc.getRootElement(), "content");
    String resultCodeStr = content.element("resultcode").getText();
    ResInfoBean resInfoBean = new ResInfoBean();
    int resultCodeInt = Integer.parseInt(resultCodeStr);
    resInfoBean.setResultCode(resultCodeInt);
    if (resultCodeInt == 0) {
      resInfoBean.setErrorMsg(content.element("errormsg").getText());
    } else {
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
