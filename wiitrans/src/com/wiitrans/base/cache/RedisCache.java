package com.wiitrans.base.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

// 暂时暴露Redis，未来已Factory模式只暴露出来接口对象引用
// 目前暂未使用Pool方式构建Redis
public class RedisCache implements ICache {

	private String _url = null;
	private Jedis _instance = null;

	@Override
	public int Init(String url) {
		int ret = Const.FAIL;

		if (url != null) {
			_url = url;
			_instance = new Jedis(_url,BundleConf.BUNDLE_REDIS_PORT,BundleConf.BUNDLE_REDIS_TIMEOUT);

			ret = Const.SUCCESS;
		}

		return ret;
	}

	public int Init() {
		int ret = Const.FAIL;

		if (_url != null) {
		    	_instance = new Jedis(_url,BundleConf.BUNDLE_REDIS_PORT,BundleConf.BUNDLE_REDIS_TIMEOUT);

			ret = Const.SUCCESS;
		}

		return ret;
	}

	@Override
	public int UnInit() {
		int ret = Const.FAIL;

		if (_instance != null) {
			_instance.close();
			_instance = null;
		}
		ret = Const.SUCCESS;

		return ret;
	}

	private int ReInit() {
		int ret = UnInit();
		if (ret == Const.SUCCESS) {
			ret = Init();
		}

		return ret;
	}

	@Override
	public int SetString(int node_id, String key, String val) {
		int ret = Const.FAIL;

		// 需要处理Jedis的函数调用返回值
		if (node_id <= 0) {
			return ret;
		}
		try {
			_instance.set("n" + node_id + "_" + key, val);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	@Override
	public String GetString(int node_id, String key) {
		String val = null;

		if (node_id <= 0) {
			return val;
		}

		try {
			val = _instance.get("n" + node_id + "_" + key);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return val;
	}
	
	public String GetString(String key) {
		String val = null;
		try {
			val = _instance.get(key);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return val;
	}

	@Override
	public int DelString(int node_id, String key) {
		int ret = Const.FAIL;
		if (node_id <= 0) {
			return ret;
		}
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.del("n" + node_id + "_" + key);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}
	public int DelString(String key) {
	    int ret = Const.FAIL;
	    try {
		_instance.del(key);
		ret = Const.SUCCESS;
	    } catch (Exception e) {
		Log4j.error(e);
		ReInit();
	    }
	    
	    return ret;
	}

	public int sadd(String key, String... members) {
		int ret = Const.FAIL;
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.sadd(key, members);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public int srem(String key, String... members) {
		int ret = Const.FAIL;
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.srem(key, members);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public Set<String> smembers(String key) {
		Set<String> members = new HashSet<String>();
		// 需要处理Jedis的函数调用返回值
		try {
			members = _instance.smembers(key);
			members = Collections.synchronizedSet(members);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return members;
	}

	public boolean sismember(String key, String member) {
		try {
			return _instance.sismember(key, member);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return false;
	}

	public int hmset(String key, Map<String, String> map) {
		int ret = Const.FAIL;
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.hmset(key, map);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public int hset(String key, String field, String value) {
		int ret = Const.FAIL;
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.hset(key, field, value);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public String hget(String key, String field) {
		String ret = null;
		// 需要处理Jedis的函数调用返回值
		try {
			ret = _instance.hget(key, field);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public int hdel(String key, String field) {
		int ret = Const.FAIL;
		// 需要处理Jedis的函数调用返回值
		try {
			_instance.hdel(key, field);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}

	public Map<String, String> hmget(String key) {
		Map<String, String> ret = new HashMap<String, String>();
		// 需要处理Jedis的函数调用返回值
		try {
			ret = _instance.hgetAll(key);
		} catch (Exception e) {
			Log4j.error(e);
			ReInit();
		}

		return ret;
	}
	public Long setnx(String key,String value) {
	    Long ret = 0L;
	    try {
		ret = _instance.setnx(key, value);
	    } catch (Exception e) {
		Log4j.error(e);
		ReInit();
	    }
	    
	    return ret;
	}
}
