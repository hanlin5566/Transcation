package com.wiitrans.base.cache;

import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.xml.WiitransConfig;

public class testRedis {

	public static void main(String[] args) {
		WiitransConfig.getInstance(0);
		RedisCache redis = new RedisCache();
		redis.Init("192.168.9.208");
		RedisTestThread test1 = new RedisTestThread(true, redis);
		RedisTestThread test2 = new RedisTestThread(false, redis);
		test1.start();
		test2.start();
		try {
			Thread.currentThread().sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		}

		Map<String, String> map = redis.hmget("abcdefghijklmnopq");
		for (String string : map.keySet()) {
			System.out.println(map.get(string));
		}

		// RedisCache redis = new RedisCache();
		// redis.Init("192.168.9.208");
		// redis.hset("n" + 1 + "_" + "hashsettest", "hsp", "haha");
		// String aaa = redis.hget("n" + 1 + "_" + "hashsettest", "hsp");
		//
		// String val = redis.GetString(1, "testa");
		// int ret = redis.SetString(1, "test", "testval");

		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		json1.put(
				"asdf",
				"UPDATE `node_order_description` SET description = '''''''''''''''\"\\/#' WHERE `order_id` = '222'; ");

		json.put("jjj", json1);
		json.put("sss", json1.toString());
		String str = json.toString();
		System.out.println("str" + str);
		JSONObject jsonObject = new JSONObject(str);
		System.out.println(jsonObject.get("sss"));
		// ICache redis = new RedisCache();
		//
		// redis.Init("192.168.9.208");
		// String val = redis.GetString("testa");
		// int ret = redis.SetString("test", "testval");
		//
		// ret = redis.SetString("test", "testval");
		//
		// val = redis.GetString("test");
		//
		// ret = redis.SetString("test", "testval");
		//
		// ret = redis.DelString("test");
		//
		// ret = redis.SetString("test", "testval");
		//
		// val = redis.GetString("testa");

		// redis.UnInit();

	}
}
