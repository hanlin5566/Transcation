package com.hzcf.configuration;

import org.apache.zookeeper.ZooKeeper;

/**
 * Create by hanlin on 2017年12月6日
 **/
public interface RegisterNode {
	/**
	 * 注册Znode
	 * 
	 */
	public void register(ZooKeeper zk_client, String... paths) throws Exception;
}
