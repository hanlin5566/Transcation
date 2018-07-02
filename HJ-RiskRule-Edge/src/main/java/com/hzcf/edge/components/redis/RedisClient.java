package com.hzcf.edge.components.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by liqinwen on 2017/8/30.
 */
@Component
public class RedisClient {

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private int port;
    @Value("${redis.password}")
    private String password;
    @Value("${redis.timeout}")
    private int timeout = 5000;
    private static JedisPool jedisPool;

    private RedisClient() {}

    @PostConstruct
    public void initRedis()
    {
        PoolConfig poolConfig = new PoolConfig();
        jedisPool = new JedisPool(poolConfig,host,
       port,this.timeout,password);
        System.out.println("====== jedisPool 启动成功 ======");
    }

    public static Jedis cache()
    {
        return jedisPool.getResource();
    }
    public void destroy()
    {
        jedisPool.close();
    }

}
