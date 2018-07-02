package com.hzcf.edge.components.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by liqinwen on 2017/8/30.
 */
public class PoolConfig extends GenericObjectPoolConfig {

    public PoolConfig()
    {
        setMinIdle(200);
        setMaxIdle(2000);
        setMaxTotal(3000);
        setTestWhileIdle(true);
        setMinEvictableIdleTimeMillis(60000);
        setTimeBetweenEvictionRunsMillis(30000);
        setNumTestsPerEvictionRun(100);
    }
}
