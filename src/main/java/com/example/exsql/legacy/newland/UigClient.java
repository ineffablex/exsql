package com.example.exsql.legacy.newland;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;


/**
 * UIG服务调用
 *
 * @author zxy
 *
 */
public class UigClient {
	/**
	 * http服务地址
	 */
	private String url;

	/**
	 * 字符编码
	 */
	private String charSet = "utf-8";

	/**
	 *
	 * @param url
	 */
	public UigClient(String url) {
		this.url = url;
	}


	/**
	 * 请求服务
	 *
	 * @param xml
	 * @return
	 */
	public String requestService(String xml) throws Exception {
		return postMessage(xml,0);
	}

	private String postMessage(String xml,int sendTimes) throws Exception {
		sendTimes = sendTimes+1;
		PostMethod post = null;
		String resultMsg = "";
		try {
			post = new PostMethod(url);
			post.setRequestHeader("Content-type", "text/xml; charset=" + charSet);
			post.setRequestHeader("Connection", "close");
			//设置期望返回的报文头编码
			post.setRequestHeader("Accept", "text/plain;charset=utf-8");

			post.setRequestBody(xml);
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
			httpclient.getHttpConnectionManager().getParams().setSoTimeout(15000);
			HttpClientParams hp = new HttpClientParams();
			hp.setContentCharset(charSet);
			httpclient.setParams(hp);
			int result = httpclient.executeMethod(post);
			switch (result) {
				case HttpStatus.SC_OK:
					resultMsg = post.getResponseBodyAsString();
					break;
				default:
					break;
			}
			if (null != post) {
				post.releaseConnection();
			}
			return resultMsg;
		} catch (Exception e) 
		{
			if (null != post) {
				post.releaseConnection();
			}
			if(sendTimes < 3)
			{
				return postMessage(xml,sendTimes);
			}else
			{
				throw e;
			}
		}
	}
}
