package com.wiitrans.base.db;

import java.util.Date;

import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.FileAccess;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class HttpTest {

	public static void main(String[] args) {

		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

		FileAccess.CreateDirectory(true, "/root/Desktop/fragaaaa/","");

		try {
			for (int i = 0; i < 100000; i++) {
				HttpSimulator simulator = new HttpSimulator(
						"http://192.168.9.204:10000/service/analysis/money");
				simulator
						.executeMethodTimeOut(
								"{\"sid\":\"customer-sessionid\", \"pairid\":\"14\",\"wordcount\":\"8620\",\"manual_start_time\":\"2015-01-01 00:00:00\",\"nid\":\"1\"}",
								2);
				System.out.print("i:" + i + " ");
				System.out.println(new Date());
				try {
					Thread.currentThread().sleep(100);
				} catch (Exception e) {
					Log4j.error(e);
				}

			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		// HttpSimulator simulator = new
		// HttpSimulator("http://192.168.9.204:10000/service/recom/user/");
		// simulator.Init("http://192.168.9.204:10000/service/recom/user/");
		// simulator.executeMethod("{\"sid\": \"df45545dfs\",\"uid\": \"3\"}");
		// String object =
		// simulator.executeMethod("{sid: df45545dfs,uid: \"3\"}");
		// simulator.Uninit();
	}
}
