package com.wiitrans.base.cache;

public class RedisTestThread extends Thread {

	private boolean aaa;
	private RedisCache redis;

	public RedisTestThread(boolean aaa, RedisCache redis) {
		this.aaa = aaa;
		this.redis = redis;
	}

	@Override
	public void run() {
		for (int i = 0; i < 10000; i++) {
			redis.hset("abcdefghijklmnopq", "hsp" + i * 2 + (aaa ? 1 : 0),
					"haha");
		}
	}

}
