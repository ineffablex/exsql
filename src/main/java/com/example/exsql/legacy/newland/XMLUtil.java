package com.example.exsql.legacy.newland;

import java.io.*;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLUtil {
  /** 缺省字符集 */
  public static final String DEFAULT_ENCODING = "UTF-8";

  /** 私有构造函数，阻止非法调用构造函数 */
  private XMLUtil() {}

  /**
   * Return the child element with the given name. The element must be in the same name space as the
   * parent element.
   *
   * @param element The parent element
   * @param name The child element name
   * @return The child element
   */
  public static Element child(Element element, String name) {
    return element.element(new QName(name, element.getNamespace()));
  }

  /**
   * 新建文档
   *
   * @return Document 文档节点
   * @throws Exception
   */
  public static Document createDocument() throws Exception {
    DocumentFactory factory = new DocumentFactory();
    Document document = factory.createDocument();
    return document;
  }

  /**
   * 通过Reader读取Document文档 如果encodingStr为null或是""，那么采用缺省编码utf-8
   *
   * @param in Reader器
   * @param encoding 编码器
   * @return documment
   * @throws Exception
   */
  public static Document fromXML(Reader in, String encoding) throws Exception {
    try {
      if (StringUtils.isEmpty(encoding)) {
        encoding = DEFAULT_ENCODING;
      }
      SAXReader reader = new SAXReader();
      Document document = reader.read(in, encoding);
      return document;
    } catch (Exception ex) {
      throw new Exception(ex);
    }
  }

  /**
   * 直接从字符串得到XML的Document
   *
   * @param source 把一个字符串文本转化为XML的Document对象
   * @param encoding 编码器
   * @return <code>Document</code> 788
   * @throws Exception
   */
  public static Document fromXML(String source, String encoding) throws Exception {
    return fromXML(new StringReader(source), encoding);
  }

  /**
   * 把XML的Document转化为java.io.Writer输出流 不支持给定Schema文件的校验
   *
   * @param document XML文档
   * @param outStream 输出写入器
   * @param encoding 编码类型
   * @throws Exception 如果有任何异常转化为该异常输出
   */
  public static void toXML(Document document, OutputStream outStream, String encoding)
      throws Exception {
    OutputFormat outformat = OutputFormat.createPrettyPrint();
    if (StringUtils.isEmpty(encoding)) {
      encoding = DEFAULT_ENCODING;
    }
    // 设置编码类型
    outformat.setEncoding(encoding);
    XMLWriter xmlWriter = null;
    try {
      xmlWriter = new XMLWriter(outStream, outformat);
      xmlWriter.write(document);
      xmlWriter.flush();
    } catch (IOException ex) {
      throw new Exception(ex);
    } finally {
      if (xmlWriter != null) {
        xmlWriter.close();
      }
    }
  }

  /**
   * 把XML文档转化为String返回
   *
   * @param document 要转化的XML的Document
   * @param encoding 编码类型
   * @return <code>String</code>
   * @throws Exception 如果有任何异常转化为该异常输出
   */
  public static String toXML(Document document, String encoding) throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    toXML(document, stream, encoding);
    if (stream != null) {
      stream.close();
    }
    String effectiveEncoding = StringUtils.isEmpty(encoding) ? DEFAULT_ENCODING : encoding;
    return stream.toString(effectiveEncoding);
  }
}
