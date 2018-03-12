package com.wiitrans.base.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.wiitrans.base.bundle.BundleBolt;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleMT;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.mail.template.Mail;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.sync.SyncData;

// TODO: 需要增加错误处理
public class AppConfig {
//
//	// public ArrayList<BundleParam> _bundles = new ArrayList<BundleParam>();
//	public ConcurrentHashMap<String, BundleParam> _bundles = new ConcurrentHashMap<String, BundleParam>();
//
//	public static void main(String[] args) {
//
//		AppConfig app = new AppConfig();
//		// app.Parse();
//		app.ParseBundle();
//	}
//
//	private int ParseManager(XNode parent) {
//		int ret = Const.FAIL;
//
//		String flag = parent.GetAttr("debug");
//		if (0 == flag.compareToIgnoreCase("true")) {
//			BundleConf.DEBUG = true;
//		} else {
//			BundleConf.DEBUG = false;
//		}
//
//		// BundleConf.DEFAULT_NID = Util.String2Int(parent.GetAttr("nid"));
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParsePushServer(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.BUNDLE_PUSHSERVER_IP = parent.GetAttr("ip");
//		BundleConf.BUNDLE_PUSHSERVER_PORT = Integer.parseInt(parent
//				.GetAttr("port"));
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseCacheServer(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.BUNDLE_REDIS_IP = parent.GetAttr("ip");
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseReportServer(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.BUNDLE_REPORT_IP = parent.GetAttr("ip");
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseChatServer(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.BUNDLE_CHATSERVER_IP = parent.GetAttr("ip");
//		BundleConf.BUNDLE_CHATSERVER_PORT = Integer.parseInt(parent
//				.GetAttr("port"));
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseHost(XNode parent) {
//		int ret = Const.FAIL;
//
//		for (XNode node : parent.GetChildren()) {
//
//			switch (node.GetTagName()) {
//			case "PushServer": {
//				ParsePushServer(node);
//				break;
//			}
//			case "CacheServer": {
//				ParseCacheServer(node);
//				break;
//			}
//			case "TaskReportor": {
//				ParseReportServer(node);
//				break;
//			}
//			case "ChatServer": {
//				ParseChatServer(node);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		return ret;
//	}
//
//	private int ParseNode(XNode parent) {
//		int ret = Const.FAIL;
//
//		ConfigNode node = new ConfigNode();
//		node.nid = Util.String2Int(parent.GetAttr("nid"));
//		node.mybatis = parent.GetAttr("mybatis");
//		node.api = parent.GetAttr("api");
//		if (Util.String2Bool(parent.GetAttr("default"))) {
//			BundleConf.DEFAULT_NID = node.nid;
//		}
//		if (Util.String2Bool(parent.GetAttr("recom"))) {
//			BundleConf.RECOM_NID = node.nid;
//		}
//		if (!BundleConf.BUNDLE_Node.containsKey(node.nid)) {
//			BundleConf.BUNDLE_Node.put(node.nid, node);
//		}
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseWord(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.FILTER_WORD_CONF_URL = parent.GetAttr("url");
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseRegx(XNode parent) {
//		int ret = Const.FAIL;
//
//		String url = parent.GetAttr("url");
//
//		BundleConf.FILTER_REGX_CONF_URL = url;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse(url);
//
//		for (XNode node : root.GetChildren()) {
//			switch (node.GetTagName()) {
//			case "regex": {
//				String value = node.GetAttr("value");
//				String name = node.GetAttr("name");
//				BundleConf.FILTER_REGXS_MAP.put(name, value);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseHbase(XNode parent) {
//		int ret = Const.FAIL;
//
//		String url = parent.GetAttr("url");
//
//		BundleConf.HBASE_CONF_URL = url;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse(url);
//
//		for (XNode node : root.GetChildren()) {
//			switch (node.GetTagName()) {
//			case "hbase.zookeeper.property.clientport": {
//				HbaseConfig.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT = node
//						.GetAttr("value");
//				break;
//			}
//			case "hbase.zookeeper.quorum": {
//				HbaseConfig.HBASE_ZOOKEEPER_QUORUM = node.GetAttr("value");
//				break;
//			}
//			case "hbase.master": {
//				HbaseConfig.HBASE_MASTER = node.GetAttr("value");
//				break;
//			}
//			case "hbase.column.family.msg.content": {
//				HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT = node
//						.GetAttr("value");
//				break;
//			}
//			case "hbase.table.prefix.room": {
//				HbaseConfig.HBASE_TABLE_PREFIX_ROOM = node.GetAttr("value");
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseNodes(XNode parent) {
//		int ret = Const.FAIL;
//		if (BundleConf.BUNDLE_Node.isEmpty()) {
//			for (XNode node : parent.GetChildren()) {
//				switch (node.GetTagName()) {
//				case "node": {
//					ParseNode(node);
//					break;
//				}
//				default:
//					break;
//				}
//			}
//			// log4j
//			Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
//			for (int nid : set) {
//				ConfigNode confignode = BundleConf.BUNDLE_Node.get(nid);
//				if (confignode != null) {
//					Log4j.info("nid:" + confignode.nid + "  mybatis:"
//							+ confignode.mybatis + " api:" + confignode.api);
//				}
//			}
//			Log4j.info("default nid:" + BundleConf.DEFAULT_NID + " recom nid:"
//					+ BundleConf.RECOM_NID);
//		}
//
//		return ret;
//	}
//
//	private int ParseDB(XNode parent) {
//		int ret = Const.FAIL;
//
//		for (XNode node : parent.GetChildren()) {
//
//			switch (node.GetTagName()) {
//			case "hbase": {
//				ParseHbase(node);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		return ret;
//	}
//
//	private int ParseFilter(XNode parent) {
//		int ret = Const.FAIL;
//
//		for (XNode node : parent.GetChildren()) {
//
//			switch (node.GetTagName()) {
//			case "word": {
//				ParseWord(node);
//				break;
//			}
//			case "regex": {
//				ParseRegx(node);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		return ret;
//	}
//
//	private int ParseBundle(XNode parent) {
//		int ret = Const.FAIL;
//
//		String url = parent.GetAttr("url");
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse(url);
//
//		BundleParam param = new BundleParam();
//		param.BUNDLE_FILE_TYPE = root.GetAttr("type");
//		param.BUNDLE_NAME = root.GetAttr("name");
//		String name = root.GetAttr("name");
//		if (!_bundles.containsKey(name)) {
//			if (0 == root.GetAttr("localcluster").compareToIgnoreCase("true")) {
//				param.BUNDLE_IS_LOCALCLUSTER = true;
//			} else {
//				param.BUNDLE_IS_LOCALCLUSTER = false;
//			}
//			if (0 == root.GetAttr("debug").compareToIgnoreCase("true")) {
//				param.BUNDLE_IS_DEBUG = true;
//			} else {
//				param.BUNDLE_IS_DEBUG = false;
//			}
//
//			for (XNode node : root.GetChildren()) {
//				switch (node.GetTagName()) {
//				case "classpath": {
//					param.BUNDLE_CLASS_FILEPATH = node.GetAttr("url");
//					break;
//				}
//				case "jarpath": {
//					param.BUNDLE_JAR_FILENAME = node.GetAttr("url");
//					break;
//				}
//				case "classname": {
//					param.BUNDLE_CLASS_FILENAME = node.GetAttr("url");
//					break;
//				}
//				case "spout": {
//					param.BUNDLE_SPOUT_COUNT = Util.String2Int(node
//							.GetAttr("count"));
//					break;
//				}
//				case "bolt": {
//					BundleBolt bolt = new BundleBolt();
//					bolt.name = node.GetAttr("name");
//					bolt.count = Util.String2Int(node.GetAttr("count"));
//					param.BUNDLE_BOLT_COUNT.add(bolt);
//					break;
//				}
//				case "worker": {
//					param.BUNDLE_WORKER_NUM = Util.String2Int(node
//							.GetAttr("num"));
//					break;
//				}
//				case "tempfilepath": {
//					param.BUNDLE_TEMPFILE_PATH = node.GetAttr("url");
//					break;
//				}
//				case "ordersyn": {
//					param.BUNDLE_ORDER_SYN = Boolean.parseBoolean(node
//							.GetAttr("automation"));
//					param.BUNDLE_ORDER_SYN_CYCLE = Util.String2Int(node
//							.GetAttr("cycle"));
//					break;
//				}
//				case "machinetranslation": {
//					BundleMT mt = new BundleMT();
//					mt.name = node.GetAttr("name");
//					mt.url = node.GetAttr("url");
//					mt.timeout = Util.String2Int(node.GetAttr("timeout"));
//					mt.use = Util.String2Bool(node.GetAttr("use"));
//					param.BUNDLE_MACHINE_TRANSLATION.add(mt);
//					break;
//				}
//				case "translator": {
//					param.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT = Util
//							.String2Int(node.GetAttr("maxcount"));
//					break;
//				}
//				case "msg": {
//					param.BUNDLE_MSG_URL = node.GetAttr("url");
//					param.BUNDLE_MSG_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "recom": {
//					param.BUNDLE_RECOM_URL = node.GetAttr("url");
//					param.BUNDLE_RECOM_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "opera": {
//					param.BUNDLE_OPERA_URL = node.GetAttr("url");
//					param.BUNDLE_OPERA_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "state": {
//					param.BUNDLE_STATE_URL = node.GetAttr("url");
//					param.BUNDLE_STATE_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "online": {
//					param.BUNDLE_ONLINE_SHOWCOUNT = Util.String2Int(node
//							.GetAttr("listcount"));
//					break;
//				}
//				case "monitor": {
//					param.BUNDLE_MONITOR_CYCLE = Util.String2Int(node
//							.GetAttr("cycle"));
//					param.BUNDLE_MONITOR_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "levenshtein": {
//					param.BUNDLE_TM_LEVENSHTEIN = Util.String2Bool(node
//							.GetAttr("value"));
//					break;
//				}
//				case "dictterm": {
//					param.BUNDLE_DICTTERM_TIMEOUT = Util.String2Int(node
//							.GetAttr("timeout"));
//					break;
//				}
//				case "tmsvrmybatis": {
//					param.TMSVR_MYBATIS = node.GetAttr("value");
//					break;
//				}
//				case "exec": {
//					param.TMSVR_COMMAND = node.GetAttr("command");
//					break;
//				}
//				case "tmsvr": {
//					param.TMSVR_API = node.GetAttr("api");
//					break;
//				}
//				default:
//					break;
//				}
//			}
//			// _bundles.add(param);
//			_bundles.putIfAbsent(name, param);
//		}
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseBundles(XNode parent) {
//		int ret = Const.FAIL;
//
//		for (XNode node : parent.GetChildren()) {
//
//			switch (node.GetTagName()) {
//			case "bundle": {
//				ret = ParseBundle(node);
//				if (ret != Const.SUCCESS) {
//					Log4j.error("Parse bundle XML failed.");
//				}
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		return ret;
//	}
//
//	private int ParseLog4j(int log, XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.LOG4J_CONFIGURE_URL = parent.GetAttr("url");
//		BundleConf.LOG4J_PRIORITY = parent.GetAttr("priority");
//
//		Log4j.initOuter(log, BundleConf.LOG4J_CONFIGURE_URL);
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	private int ParseMail(XNode parent) {
//		int ret = Const.FAIL;
//
//		String url = parent.GetAttr("url");
//
//		BundleConf.FILTER_MAIL_CONF_URL = url;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse(url);
//
//		for (XNode node : root.GetChildren()) {
//			String name = node.GetAttr("name");
//			String hostname = node.GetAttr("hostname");
//			String username = node.GetAttr("username");
//			String pwd = node.GetAttr("pwd");
//			String send = node.GetAttr("send");
//			String nickname = "";
//			String recipient = "";
//			String subject = "";
//			String content = "";
//			String sql = "";
//			for (XNode nodeAtt : node.GetChildren()) {
//				switch (nodeAtt.GetTagName()) {
//				case "nickname": {
//					nickname = nodeAtt.GetAttr("value");
//					break;
//				}
//				case "recipient": {
//					recipient = nodeAtt.GetAttr("value");
//					break;
//				}
//				case "subject": {
//					subject = nodeAtt.getContent();
//					break;
//				}
//				case "content": {
//					content = nodeAtt.getContent();
//					break;
//				}
//				case "sql": {
//					sql = nodeAtt.getContent();
//					break;
//				}
//				default:
//					break;
//				}
//			}
//			Mail mail = new Mail(name, hostname, username, pwd, nickname,
//					recipient, subject, content, sql, send);
//			BundleConf.MAIL_TEMPLATE.put(name, mail);
//		}
//		ret = Const.SUCCESS;
//		return ret;
//	}
//
//	private int ParseSyncDatas(XNode parent) {
//		int ret = Const.FAIL;
//		String targetURL = parent.GetAttr("targetURL");
//		for (XNode node : parent.GetChildren()) {
//			switch (node.GetTagName()) {
//			case "syncdata": {
//				ParseSyncData(node, targetURL);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//		return ret;
//	}
//
//	private int ParseSyncData(XNode parent, String targetURL) {
//		int ret = Const.FAIL;
//		try {
//
//			String url = parent.GetAttr("url");
//
//			XDocument _xmlDoc = new XDocument();
//			XNode root = _xmlDoc.Parse(url);
//			for (XNode node : root.GetChildren()) {
//				String name = node.GetAttr("name");
//				String sourceSql = "";
//				List<String> targetSql = new ArrayList<String>();
//				for (XNode nodeAtt : node.GetChildren()) {
//					switch (nodeAtt.GetTagName()) {
//					case "source": {
//						sourceSql = nodeAtt.getContent();
//						break;
//					}
//					case "targets": {
//						for (XNode nodeTarget : nodeAtt.GetChildren()) {
//							targetSql.add(nodeTarget.getContent());
//						}
//						break;
//					}
//					default:
//						break;
//					}
//				}
//				SyncData syncData = new SyncData(name, targetURL, sourceSql,
//						targetSql);
//				if (!BundleConf.SYNC_DATA_TEMPLATE.containsKey(name)) {
//					BundleConf.SYNC_DATA_TEMPLATE.put(name, syncData);
//					// Log4j.info("data template load:[" + name + "]");
//				}
//			}
//			ret = Const.SUCCESS;
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			Log4j.error(e);
//		}
//		return ret;
//	}
//
//	private int ParseReq(XNode parent) {
//		int ret = Const.FAIL;
//
//		String file_url = parent.GetAttr("url");
//
//		BundleConf.FILTER_MAIL_CONF_URL = file_url;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse(file_url);
//
//		for (XNode node : root.GetChildren()) {
//			String id = node.GetAttr("id");
//			String name = node.GetAttr("name");
//			String url = node.GetAttr("url");
//			String param = node.GetAttr("param");
//			String req_type = node.GetAttr("req_type");
//			String cron = node.GetAttr("cron");
//			String className = node.GetAttr("className");
//			Map<String, String> reqTemplate = new HashMap<String, String>();
//			reqTemplate.put("id", id);
//			reqTemplate.put("name", name);
//			reqTemplate.put("url", url);
//			reqTemplate.put("param", param);
//			reqTemplate.put("req_type", req_type);
//			reqTemplate.put("cron", cron);
//			reqTemplate.put("className", className);
//			BundleConf.REQ_TEMPLATE.put(name, reqTemplate);
//		}
//		ret = Const.SUCCESS;
//		return ret;
//	}
//
//	private int ParseMatch(XNode parent) {
//		int ret = Const.FAIL;
//
//		BundleConf.BUNDLE_MATCH_INDUSTRY = Util.String2Bool(parent
//				.GetAttr("value"));
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	public int Parse(int log) {
//		int ret = Const.FAIL;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse("/opt/wiitrans/conf/conf.xml");
//
//		for (XNode node : root.GetChildren()) {
//			switch (node.GetTagName()) {
//			case "manager": {
//				ParseManager(node);
//				break;
//			}
//			case "host": {
//				ParseHost(node);
//				break;
//			}
//			case "nodes": {
//				ParseNodes(node);
//				break;
//			}
//			case "db": {
//				ParseDB(node);
//				break;
//			}
//			case "bundles": {
//				ParseBundles(node);
//				break;
//			}
//			case "filter": {
//				ParseFilter(node);
//				break;
//			}
//			case "log4j": {
//				ParseLog4j(log, node);
//				break;
//			}
//			case "mail": {
//				ParseMail(node);
//				break;
//			}
//			case "syncdatas": {
//				ParseSyncDatas(node);
//				break;
//			}
//			case "req": {
//				ParseReq(node);
//				break;
//			}
//			case "matchindustry": {
//				ParseMatch(node);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
//
//	public int Parse() {
//		return Parse(0);
//	}
//
//	public int ParseBundle() {
//		int ret = Const.FAIL;
//
//		XDocument _xmlDoc = new XDocument();
//		XNode root = _xmlDoc.Parse("/opt/wiitrans/conf/conf.xml");
//
//		for (XNode node : root.GetChildren()) {
//			switch (node.GetTagName()) {
//
//			case "bundles": {
//				ParseBundles(node);
//				break;
//			}
//			default:
//				break;
//			}
//		}
//
//		ret = Const.SUCCESS;
//
//		return ret;
//	}
}
