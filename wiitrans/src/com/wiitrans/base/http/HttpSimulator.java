package com.wiitrans.base.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import com.wiitrans.base.log.Log4j;

public class HttpSimulator {
	private static class UTF8PostMethod extends PostMethod {
		// 将url转换为utf8编码
		public UTF8PostMethod(String url) {
			super(url);
		}

		@Override
		public String getRequestCharSet() {
			return "UTF-8";
		}
	}

	// private PostMethod _method = null;
	// private HttpClient _client = null;
	private String _url = null;

	public HttpSimulator(String url) {
		// "http://192.168.9.204:10000/service/term/preprocess/"
		// _client = new HttpClient();
		// _method = new UTF8PostMethod(url);
		_url = url;
	}

	// 机器翻译专用接口，返回值是机器翻译的翻译结果
	public String executeMethod(String params) {
		return this.executeMethodTimeOut(params, 0);
	}

	// 机器翻译专用接口，返回值是机器翻译的翻译结果
	public String executeMethodTimeOut(String params, int timeout) {
		String result = null;
		PostMethod method = null;
		HttpClient client = null;
		try {

			method = new UTF8PostMethod(_url);

			RequestEntity requestEntity = new StringRequestEntity(params,
					"text/xml", "UTF-8");
			method.setRequestEntity(requestEntity);

			client = new HttpClient();
			if (timeout > 0) {
				// client.getHttpConnectionManager().getParams()
				// .setConnectionTimeout(timeout);
				client.setTimeout(timeout * 1000);
			}
			client.executeMethod(method);

			String str = method.getResponseBodyAsString();
			JSONObject json = new JSONObject(str);

			if (str.indexOf("tgt_text") != -1) {
				result = json.get("tgt_text").toString();
			}
		} catch (Exception e) {
			Log4j.error(_url, e);
			// e.printStackTrace();
		} finally {
			if (method != null) {
				method.releaseConnection();
				method = null;
			}
			if (client != null) {
				((SimpleHttpConnectionManager) client
						.getHttpConnectionManager()).shutdown();
				client = null;
			}
		}
		return result;
	}

	// 普通接口，返回的是json
	public JSONObject executeMethodJSON(String params) {
		return this.executeMethodJSONTimeOut(params, 0);
	}

	// 普通接口，返回的是json
	public JSONObject executeMethodJSONTimeOut(String params, int timeout) {
		PostMethod method = null;
		HttpClient client = null;
		try {

			method = new UTF8PostMethod(_url);

			RequestEntity requestEntity = new StringRequestEntity(params,
					"text/xml", "UTF-8");
			method.setRequestEntity(requestEntity);

			client = new HttpClient();
			if (timeout > 0) {
				// client.getHttpConnectionManager().getParams()
				// .setConnectionTimeout(timeout);
				client.setTimeout(timeout * 1000);
			}
			client.executeMethod(method);

			String str = method.getResponseBodyAsString();
			return new JSONObject(str);
		} catch (Exception e) {
			Log4j.error(_url, e);
			return new JSONObject();
			// e.printStackTrace();
		} finally {
			if (method != null) {
				method.releaseConnection();
				method = null;
			}
			if (client != null) {
				((SimpleHttpConnectionManager) client
						.getHttpConnectionManager()).shutdown();
				client = null;
			}
		}
	}
}
