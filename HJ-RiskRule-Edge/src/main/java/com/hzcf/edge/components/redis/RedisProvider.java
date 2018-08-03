package com.hzcf.edge.components.redis;

import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by liqinwen on 2017/8/30.
 */
public class RedisProvider {

    //redis 前缀
    private final static String prefix="HJ-RiskRule-Edge-";

    public static void set(String key,String value)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        String reply = jedis.set(key,value);
        jedis.close();
    }

    public static String get(String key)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        String reply = jedis.get(key);
        jedis.close();
        return reply;
    }

    public static String set(String key,String value,long expr)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        String reply = jedis.set(key,value,"NX","EX",expr);
        jedis.close();
        return reply;
    }

    /**
     * 换集群不支持 index
     * @param key
     * @param value
     * @param index
     * @return
     */
    public static String set(String key,String value,int index)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        index =  index>0 ? index : 0;
        jedis.select(index);
        String reply = jedis.set(key,value);
        jedis.close();
        return reply;
    }

    public static String set(String key,String value,int index,int seconds)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        index =  index>0 ? index : 0;
        jedis.select(index);
        String reply = jedis.set(key,value);
        jedis.expire(key,seconds);
        jedis.close();
        return reply;
    }

    public static String get(String key,int index)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        index =  index>0 ? index : 0;
        jedis.select(index);
        String reply = jedis.get(key);
        jedis.close();
        return reply;
    }

    public static String getWithOutPrefix(String key,int index)
    {
        Jedis jedis = RedisClient.cache();
        index =  index>0 ? index : 0;
        jedis.select(index);
        String reply = jedis.get(key);
        jedis.close();
        return reply;
    }


    public static Long lpush(String key,String value)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        Long reply = jedis.lpush(key,value);
        jedis.close();
        return reply;
    }

    public static Long del(String key)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        Long reply = jedis.del(key);
        jedis.close();
        return reply;
    }

    public static List<String> lrange(String key, long start, long end)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        List<String> replys = jedis.lrange(key,start,end);
        jedis.close();
        return replys;
    }

    public static List<String> lrangeAll(String key)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        Long reply = jedis.llen(key);
        List<String> replys = jedis.lrange(key,0,reply-1);
        jedis.close();
        return replys;
    }

    public static Long llen(String key)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        Long reply = jedis.llen(key);
        jedis.close();
        return reply;
    }

    public static boolean exist(String key,int index)
    {
        key = prefix+key;
        Jedis jedis = RedisClient.cache();
        index =  index>0 ? index : 0;
        jedis.select(index);
        boolean exist = jedis.exists(key);
        jedis.close();
        return exist;
    }
}
