package com.hzcf.configuration;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create by hanlin on 2017年12月5日
 **/
public class ZKConnector {
	private static final Logger logger = LoggerFactory.getLogger(ZKConnector.class);

	private CountDownLatch latch = new CountDownLatch(1);

	@Value("${zookeeper.address}")
	private String address;
	@Value("${zookeeper.session.timeout}")
	private int zkSessionTimeout;
	private ZooKeeper zookeeper;

	private ZKConnector() {
		try {
			zookeeper = new ZooKeeper(address, zkSessionTimeout, new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					final KeeperState STATE = event.getState();
					switch (STATE) {
					case SyncConnected:
						latch.countDown();
						logger.info("成功连接zookeeper服务器");
						break;
					case Disconnected:
						logger.warn("与zookeeper服务器断开连接");
						break;
					case Expired:
						logger.error("session会话失效...");
						break;
					default:
						break;
					}
				}
			});
			latch.await();
		} catch (IOException | InterruptedException e) {
			logger.error("", e);
		}
	}

}
