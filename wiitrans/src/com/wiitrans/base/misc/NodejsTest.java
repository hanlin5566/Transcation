package com.wiitrans.base.misc;

import java.util.HashMap;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class NodejsTest {
	public static void main(String[] args) {
//		AppConfig app = new AppConfig();
//		app.Parse();
		WiitransConfig.getInstance(0);
		Log4j.initOuter(1, BundleConf.LOG4J_CONFIGURE_URL);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		map.put("key4", "value4");

		// Log4j.log(map);
	}

	public static void main_____(String[] args) {
		// int count = 50;
		// Random r = new Random();
		//
		// int[] array = new int[count];
		//
		// for (int i = 0; i < count; i++) {
		// array[i] = r.nextInt(1000);
		// }

		// array[2] = 500;
		// array[39] = 500;

		int[] array = new int[] { 1, 2, 3 };

		for (int i : array) {
			System.out.print(i);
			System.out.print(' ');
		}
		int[] result = new QuickSort().Sort(array);
		System.out.println();
		for (int i : result) {
			System.out.print(i);
			System.out.print(' ');
		}
		System.out.println("----------");
	}

	public static void main_(String[] args) {
		TaskReportor _pushServer = null;

		_pushServer = new TaskReportor("192.168.9.204", 3001, false);
		_pushServer.Start();

		JSONObject pushObj = new JSONObject();
		pushObj.put("aid", "klarck");
		pushObj.put("sid", "sid_ebola");
		_pushServer.Report(pushObj);
	}
}
