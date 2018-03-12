package com.wiitrans.base.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;

public class TransUtil_ICIBA {
	// private static final String ICIBA_URL =
	// "http://dict-co.iciba.com/api/dictionary.php";
	// private static final String AUTH_KEY =
	// "EB791462F3C4EE0D4649FE4B37B26126";
	private static final String type = "json";

	public static final int EXCHANGE_TYPE_PL = 1;
	public static final int EXCHANGE_TYPE_PAST = 2;
	public static final int EXCHANGE_TYPE_DONE = 3;
	public static final int EXCHANGE_TYPE_ING = 4;
	public static final int EXCHANGE_TYPE_THIRD = 5;
	public static final int EXCHANGE_TYPE_ER = 6;
	public static final int EXCHANGE_TYPE_EST = 7;

	public static void translate(ICIBAWord word) throws Exception {
		HttpClient client = new HttpClient();

		PostMethod method = null;
		method = new UTF8PostMethod(word.iciba_url + "?key=" + word.auth_key);
		method.setParameter("type", type);
		method.setParameter("w", word.source);
		client.executeMethod(method);
		InputStream inputStream = method.getResponseBodyAsStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuffer stringBuffer = new StringBuffer();
		String str = "";
		while ((str = br.readLine()) != null) {
			stringBuffer.append(str);
		}
		str = stringBuffer.toString();
		Thread.sleep(100);
		JSONObject json = new JSONObject(str);
		// if (json.get("word_name") != null) {
		if (Util.GetStringFromJSon("word_name", json) != null) {
			word.isWord = true;
			JSONObject exchangeJson = (JSONObject) json.get("exchange");
			word.pl = getExchangeVaue(exchangeJson, "word_pl");
			word.past = getExchangeVaue(exchangeJson, "word_past");
			word.done = getExchangeVaue(exchangeJson, "word_done");
			word.ing = getExchangeVaue(exchangeJson, "word_ing");
			word.third = getExchangeVaue(exchangeJson, "word_third");
			word.er = getExchangeVaue(exchangeJson, "word_er");
			word.est = getExchangeVaue(exchangeJson, "word_est");
		} else {
			word.isWord = false;
		}
	}

	public static class UTF8PostMethod extends PostMethod {
		public UTF8PostMethod(String url) {
			super(url);
		}

		@Override
		public String getRequestCharSet() {
			return "UTF-8";
		}
	}

	private static String getExchangeVaue(JSONObject exchangeJson, String key) {
		String ret = "";
		try {
			Object obj = exchangeJson.get(key);
			if (obj.toString().length() > 0 && obj instanceof JSONArray) {
				ret = ((JSONArray) obj).get(0).toString();
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}
		return ret;
	}
}
