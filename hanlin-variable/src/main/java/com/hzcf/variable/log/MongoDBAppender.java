package com.hzcf.variable.log;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.helpers.LogLog;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Create by hanlin on 2017年11月15日
 **/
public class MongoDBAppender extends AppenderBase<LoggingEvent> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String host = "127.0.0.1";
	private int port = 27017;
	private String db = "db";
	private Document hostInfo = new Document();
	private MongoClient mongoClient;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	private void setupNetworkInfo() {
		hostInfo.put("process", ManagementFactory.getRuntimeMXBean().getName());
		try {
			hostInfo.put("name", InetAddress.getLocalHost().getHostName());
			hostInfo.put("ip", InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			LogLog.warn(e.getMessage());
		}
	}

	public MongoDBAppender() {
		setupNetworkInfo();
	}

	@Override
	protected void append(LoggingEvent event) {
		Document mongoDocument = new Document();
		mongoDocument.put("timestamp", new Date());
		mongoDocument.put("level", event.getLevel().toString());
		mongoDocument.put("thread", event.getThreadName());
		mongoDocument.put("loggerName", event.getLoggerName());
		mongoDocument.put("message", event.getMessage());
		// host
		mongoDocument.put("host", hostInfo);
		Object[] args = event.getArgumentArray();
		for (Object obj : args) {
			if(obj instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String, Object>)obj;
				mongoDocument.putAll(map);
			}
		}
		
		String collectionName = event.getLevel().levelStr;
		MongoCollection<Document> collection = mongoClient.getDatabase(db).getCollection(collectionName);
		try {
			collection.insertOne(mongoDocument);
		} catch (Exception e) {
			logger.error("日志写入mongo异常", e);
		}
	}

	@Override
	public void start() {
		// 初始化mongoClient
		ServerAddress serverAddress = new ServerAddress(host, port);
		mongoClient = new MongoClient(serverAddress);
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
	}
}
