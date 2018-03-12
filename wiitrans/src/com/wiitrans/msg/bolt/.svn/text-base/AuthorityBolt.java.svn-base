package com.wiitrans.msg.bolt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.GenericSQLDAO;
import com.wiitrans.base.db.MessageDAO;
import com.wiitrans.base.db.ProcLoginDAO;
import com.wiitrans.base.db.RoomDAO;
import com.wiitrans.base.db.TranslatorDAO;
import com.wiitrans.base.db.model.MessageUserBean;
import com.wiitrans.base.db.model.RoomBean;
import com.wiitrans.base.db.model.RoomUserBean;
import com.wiitrans.base.db.model.TranslatorBean;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.hbase.HbaseDAO;
import com.wiitrans.base.hbase.HbaseRow;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.PushServer;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

@SuppressWarnings("serial")
public class AuthorityBolt extends BaseBasicBolt {

	private TaskReportor _reportor = null;
	private ICache _cache = null;
	private PushServer _pushServer = null;
	private HashMap<Integer, ConfigNode> _sync_url;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		_sync_url = new HashMap<Integer, ConfigNode>();
		Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
		for (Integer node_id : set) {
			if (node_id > 0) {
				if (!_sync_url.containsKey(node_id)) {
					ConfigNode bs = new ConfigNode();
					bs.nid = BundleConf.BUNDLE_Node.get(node_id).nid;
					bs.timeout = BundleConf.BUNDLE_Node.get(node_id).timeout;
					bs.api = BundleConf.BUNDLE_Node.get(node_id).api
							+ "automation/newtask/";
					_sync_url.put(bs.nid, bs);
					Log4j.log("          auto-sync nid = " + bs.nid + " url = "
							+ bs.api);
				}
			}
		}

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.MSG_BUNDLE_PORT);
		_reportor.Start();

		if (_cache == null) {
			_cache = new RedisCache();
			_cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}
		_pushServer = new PushServer();
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);

		Log4j.log("authoritybolt " + obj.toString());
		boolean isSend = false;
		switch (method) {
		case "POST": {
			switch (aid) {
			case "connect": {
				// 连接房间
				connectRoom(obj);
				isSend = false;
				break;
			}
			case "disconnect": {
				// 断开房间连接
				disconnectRoom(obj);
				isSend = false;
				break;
			}
			case "history": {
				// 查看房间历史消息
				isSend = false;
				showHistory(obj);
				break;
			}
			case "offLineMsg": {
				// 查看房间离线消息
				isSend = false;
				offLineMsg(obj);
				break;
			}
			case "roomnew": {
				// 查看房间历史消息总数
				isSend = false;
				offlineRoomMsgCount(obj);
				break;
			}
			case "roomhistory": {
				// admin查看房间历史消息
				isSend = false;
				roomhistory(obj);
				break;
			}
			case "syncData": {
				// 同步未读消息数据
				isSend = false;
				SyncOfflineData(obj);
				break;
			}
			case "sysMsgCount": {
				// 信息推送
				sysMsgCount(obj);
				isSend = false;
				break;
			}
			default:
				isSend = true;
				break;
			}
			break;
		}
		default:
			break;
		}
		if (isSend) {
			// 发送Storm消息到下一个Bolt
			collector.emit(new Values(content));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}

	public void sysMsgCount(JSONObject obj) {
		try {
			int nid = Util.GetIntFromJSon("nid", obj);
			int tnid = Util.GetIntFromJSon("tnid", obj);
			int datetype = Util.GetIntFromJSon("datetype", obj);
			String uids = Util.GetStringFromJSon("senduid", obj);
			// 判断俩用户是否未同一节点
			SendToPHP(obj, "OK");
			List<String> sendUids = Arrays.asList(uids.split(","));
			for (String senduid : sendUids) {
			    if (nid == tnid) {
				// 插入usermsg
				MessageDAO messageDAO = new MessageDAO();
				messageDAO.Init(true);
				MessageUserBean msg = new MessageUserBean();
				msg.user_id = Integer.parseInt(senduid);
				msg.message = Util.GetStringFromJSon("message", obj);
				if (datetype >= 0) {
				    msg.date_type = datetype;
				} else {
				    msg.date_type = 0;
				}
				messageDAO.Insert(msg);
				messageDAO.Commit();
				messageDAO.UnInit();
				// 如果为同一节点则查询未读消息总数
				String userkey = "uid_" + senduid;
				String userInfo = _cache.GetString(nid, userkey);
				if (userInfo == null) {
				    // 如果没查找到userinfo则去其他节点查找
				    BundleConf.BUNDLE_Node.keySet();
				    for (Integer node_id : BundleConf.BUNDLE_Node.keySet()) {
					if (node_id != nid) {
					    userInfo = _cache.GetString(node_id, userkey);
					}
				    }
				}
				if (userInfo != null) {
				    int msgCount = 0;
				    ProcLoginDAO procLoginDAO = new ProcLoginDAO();
				    procLoginDAO.Init(true);
				    msgCount = procLoginDAO.getSysMsgCount(Integer
					    .parseInt(senduid));
				    procLoginDAO.UnInit();
				    JSONObject userInfoObj = new JSONObject(userInfo);
				    String sid = Util.GetStringFromJSon("sid", userInfoObj);
				    JSONObject object = new JSONObject();
				    object.put("sid", sid);
				    object.put("uid", senduid);
				    object.put("aid", "newm");
				    object.put("msgcount", msgCount);
				    _pushServer.Report(object);
				}
			    } else {
				// 不是同一节点则请求其他节点进行查询
				ConfigNode sync = _sync_url.get(tnid);
				JSONObject object = new JSONObject();
				object.put("sid", Util.GetStringFromJSon("sid", obj));
				object.put("uid", senduid);
				object.put("senduid", senduid);
				object.put("aid", "sysMsgCount");
				object.put("message", Util.GetStringFromJSon("message", obj));
				String url = BundleConf.BUNDLE_Node.get(tnid).api
					+ "msg/sysMsgCount/";
				object.put("nid", "" + tnid);
				object.put("tnid", "" + tnid);
				new HttpSimulator(url).executeMethodTimeOut(object.toString(),
					sync.timeout);
			    }
			}
		} catch (Exception e) {
			Log4j.error("获取系统消息提示:", e);
			SendToPHP(obj, "FAILED");
		}
	}
	
	
	
	/**
	 * 验证用户与session一致性
	 * 
	 * @param obj
	 * @return
	 */
	public boolean checkUser(JSONObject obj) {
		String uid = Util.GetStringFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		String sid = Util.GetStringFromJSon("sid", obj);
		String userkey = "uid_" + uid;
		String userInfo = _cache.GetString(nid, userkey);
		if (userInfo != null) {
			JSONObject userInfoObj = new JSONObject(userInfo);
			String sid_cache = Util.GetStringFromJSon("sid", userInfoObj);
			return sid.equals(sid_cache);
		}
		return false;
	}

	private void connectRoom(JSONObject obj) {
		JSONObject pushObj = new JSONObject();
		int oid = Util.GetIntFromJSon("oid", obj);
		String ocode = Util.GetStringFromJSon("ocode", obj);
		String tuid = Util.GetStringFromJSon("tuid", obj);
		String trole = Util.GetStringFromJSon("trole", obj);
		String uid = Util.GetStringFromJSon("uid", obj);
		String urole = Util.GetStringFromJSon("urole", obj);
		// 查询mysql数据库，是否有此房间关系,没有则创建，有则返回
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("order_id", "" + oid);
		List<String> userids = Arrays.asList((tuid + "," + uid).split(","));
		List<String> userRoles = Arrays
				.asList((trole + "," + urole).split(","));
		// 验证用户与session一致性，如果不一致则不处理
		if (!checkUser(obj)) {
			SendToPHP(obj, "FAILED");
			Log4j.warn("非法用户：" + obj.toString());
		}
		if (userids.size() != userRoles.size()) {
			SendToPHP(obj, "FAILED");
			return;
		}
		param.put("user_ids", userids);
		param.put("user_count", userids.size());
		int rid = 0;
		// TODO:mark:hlhu 此处未写缓存代码，每次都查询MYSQL数据库，如数据库压力过大考虑缓存读取。
		RoomDAO roomDAO = new RoomDAO();
		roomDAO.Init(true);
		RoomBean room = roomDAO.selectRoom(param);
		boolean sameNode = this.isSameNodeUser(uid, tuid);
		if (room == null) {
			try {
				room = new RoomBean();
				room.order_id = oid;
				TranslatorDAO translatorDAO = new TranslatorDAO();
				translatorDAO.Init(true);
				// 拼装room name用于推送消息
				// 又改拉:拼装JSON存入name，存入内容为orderCode,uid_NickName,uid_roleName
				JSONObject nameJsonObject = new JSONObject();
				nameJsonObject.put("oCode", ocode);
				// TODO 根据用户ID查询昵称，此处应该传入用户ID list，返回
				// user_id,nickname的map。减少连接数，日后修改。 done
				List<TranslatorBean> translatorList = translatorDAO
						.SelectUserIds(userids);
				Map<String, String> userId_nickNameMap = new HashMap<String, String>();
				for (TranslatorBean translatorBean : translatorList) {
					String userId = "" + translatorBean.user_id;
					String nickName = translatorBean.nickname;
					userId_nickNameMap.put(userId, nickName);
				}
				for (int i = 0; i < userids.size(); i++) {
					String userId = userids.get(i);
					String nickName = userId_nickNameMap != null
							&& userId_nickNameMap.containsKey(userId) ? userId_nickNameMap
							.get(userId) : userId;
					String userRole = userRoles.get(i);
					nameJsonObject.put(userId + "_nickName", nickName);
					nameJsonObject.put(userId + "_role", userRole);
				}
				translatorDAO.UnInit();
				String roomName = nameJsonObject.toString();
				room.name = roomName;
				// 创建房间
				roomDAO.insertRoomBean(room);
				rid = room.room_id;
				// 创建房间与用户的关系
				for (String userid : userids) {
					RoomUserBean roomUser = new RoomUserBean();
					roomUser.room_id = rid;
					roomUser.user_id = Integer.parseInt(userid);
					roomUser.connect_time = System.currentTimeMillis();
					roomDAO.insertRoomUserBean(roomUser);
				}
				roomDAO.Commit();
				// 不同节点调用异步请求
				if (!sameNode) {
					// 调用同步，同步房间信息
					this.syncRoom(obj, room);
					// 调用同步，同步房间与用户关系
					this.syncRoomUser(obj, room);
				}
			} catch (Exception e) {
				Log4j.error("创建聊天房间异常:" + e);
			}
			// Hbase创建聊天记录表
			try {
				HbaseDAO hbaseDAO = new HbaseDAO();
				hbaseDAO.Init(true);
				hbaseDAO.createTable(HbaseConfig.HBASE_TABLE_PREFIX_ROOM + rid,
						false, HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT);
				hbaseDAO.UnInit();
			} catch (Exception e) {
				Log4j.error("创建聊天记录表异常:" + e.getMessage());
			}
		} else {
			// TODO:更新用户登录时间
			try {
				RoomUserBean roomUser = new RoomUserBean();
				roomUser.room_id = room.room_id;
				roomUser.user_id = Integer.parseInt(uid);
				roomUser.connect_time = System.currentTimeMillis();
				if (isLocalCreate("" + room.room_id)) {
					roomDAO.updateConnectTime(roomUser);
					// 不同节点调用异步请求
					// 调用同步，同步登录时间
					this.syncUpdateConnectTime(obj, room);
				} else {
					roomDAO.updateNodeConnectTime(roomUser);
					// 不同节点调用异步请求
					// 调用同步，同步登录时间
					this.syncUpdateNodeConnectTime(obj, room);
				}
				roomDAO.Commit();
			} catch (Exception e) {
				// TODO: handle exception
				Log4j.error("更新用户登录时间异常:" + e.getMessage());
			}
		}
		roomDAO.UnInit();
		if (room != null) {
			rid = room.room_id;
			pushObj.put("rid", rid);
			SendToPHP(obj, pushObj.toString());
		} else {
			SendToPHP(obj, "FAILED");
		}
	}

	// 查询俩个用户是否在同一节点数据是否需要同步
	private boolean isSameNodeUser(String uid, String tuid) {
		boolean same = false;
		try {
			GenericSQLDAO dao = new GenericSQLDAO();
			dao.Init(true);
			String sql = "SELECT user_id FROM user WHERE user_id IN ('#{uid}','#{tuid}')";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("uid", uid);
			param.put("tuid", tuid);
			List<Map<String, Object>> list = dao.selectList(sql, param);
			same = list.size() == 2;
			dao.UnInit();
		} catch (Exception e) {
			Log4j.error("判断是否未同一节点用户异常:" + e.getMessage());
		}
		return same;

	}

	// 查询房间是否未本节点创建
	private boolean isLocalCreate(String rid) {
		boolean isLocalCreate = false;
		try {
			GenericSQLDAO dao = new GenericSQLDAO();
			dao.Init(true);
			String sql = "SELECT room_id,'1' as nid FROM room where room_id = #{rid} UNION SELECT room_id,'2' as nid FROM node_room where room_id = #{rid}";
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("rid", rid);
			List<Map<String, Object>> list = dao.selectList(sql, param);
			int nid = 0;
			for (Map<String, Object> map : list) {
				nid = Integer.parseInt("" + map.get("nid"));
			}
			dao.UnInit();
			return nid == 1;// 1为自己room表查询到的数据,2为node_room查询到的数据
		} catch (Exception e) {
			Log4j.error("查询房间是否未本节点创建异常:" + e.getMessage());
		}
		return isLocalCreate;

	}

	private int syncRoom(JSONObject obj, RoomBean room) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncRoomLogicImpl");
			paramJson.put("order_id", "" + room.order_id);
			paramJson.put("room_id", "" + room.room_id);
			paramJson.put("create_time", "" + room.create_time);
			paramJson.put("name", "" + room.name);
			paramJson.put("node_id", "" + nid);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			for (int nodeId : _sync_url.keySet()) {
				// 循环发往对方节点
				if (nodeId != nid) {
					ConfigNode targetSync = _sync_url.get(nodeId);
					new HttpSimulator(targetSync.api).executeMethodTimeOut(
							jsonObject.toString(), sync.timeout);
				}
			}
		}
		return ret;
	}

	private int syncRoomUser(JSONObject obj, RoomBean room) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(obj.get("oid")));
			paramJson.put("nid", nid);
			paramJson.put("room_id", "" + room.room_id);
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "insertRoomUser");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			new HttpSimulator(sync.api).executeMethodTimeOut(
					jsonObject.toString(), sync.timeout);
		}
		return ret;
	}

	private int syncUpdateConnectTime(JSONObject obj, RoomBean room) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(obj.get("oid")));
			paramJson.put("nid", nid);
			paramJson.put("room_id", "" + room.room_id);
			paramJson.put("user_id", String.valueOf(obj.get("uid")));
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "updateConnectTime");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			new HttpSimulator(sync.api).executeMethodTimeOut(
					jsonObject.toString(), sync.timeout);
		}
		return ret;
	}

	private int syncUpdateNodeConnectTime(JSONObject obj, RoomBean room) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(obj.get("oid")));
			paramJson.put("nid", nid);
			paramJson.put("room_id", "" + room.room_id);
			paramJson.put("user_id", String.valueOf(obj.get("uid")));
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "updateNodeConnectTime");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			new HttpSimulator(sync.api).executeMethodTimeOut(
					jsonObject.toString(), sync.timeout);
		}
		return ret;
	}

	private int syncUpdateNodeDisConnectTime(JSONObject obj,
			RoomUserBean roomUser) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(obj.get("oid")));
			paramJson.put("nid", nid);
			paramJson.put("room_id", "" + roomUser.room_id);
			paramJson.put("user_id", String.valueOf(obj.get("uid")));
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "updateNodeDisConnectTime");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			new HttpSimulator(sync.api).executeMethodTimeOut(
					jsonObject.toString(), sync.timeout);
		}
		return ret;
	}

	private int syncUpdateDisConnectTime(JSONObject obj, RoomUserBean roomUser) {
		int ret = Const.FAIL;
		ConfigNode sync;
		int nid = Integer.parseInt(String.valueOf(obj.get("nid")));
		if (_sync_url.containsKey(nid)) {
			sync = _sync_url.get(nid);
			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(obj.get("oid")));
			paramJson.put("nid", nid);
			paramJson.put("room_id", "" + roomUser.room_id);
			paramJson.put("user_id", String.valueOf(obj.get("uid")));
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "updateDisConnectTime");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", String.valueOf(obj.get("sid")));
			jsonObject.put("uid", String.valueOf(obj.get("uid")));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			new HttpSimulator(sync.api).executeMethodTimeOut(
					jsonObject.toString(), sync.timeout);
		}
		return ret;
	}

	private int SendToPHP(JSONObject obj, String result) {
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		return _reportor.Report(resObj);
	}

	/**
	 * 返回聊天记录
	 * 
	 * @param obj
	 */
	private void showHistory(JSONObject obj) {
		try {
			List<JSONObject> msglist = new ArrayList<JSONObject>();
			String uid = Util.GetStringFromJSon("uid", obj);
			String rid = Util.GetStringFromJSon("rid", obj);
			String cpage = Util.GetStringFromJSon("cpage", obj);
			String pagesize = Util.GetStringFromJSon("pagesize", obj);
			String ispagination = Util.GetStringFromJSon("ispagination", obj);
			// TODO:暂时注释掉开始结束时间，日后需要根据时间过滤再添加
			// String stime = Util.GetStringFromJSon("stime", obj);
			// String etime = Util.GetStringFromJSon("etime", obj);
			// 查询用户上线时间，查询上线时间之前所有的历史聊天数据
			RoomDAO roomDAO = new RoomDAO();
			roomDAO.Init(true);
			RoomUserBean roomUser = new RoomUserBean();
			roomUser.room_id = Integer.parseInt(rid);
			roomUser.user_id = Integer.parseInt(uid);
			roomUser = roomDAO.selectRoomUser(roomUser);
			long stime = roomUser.disconnect_time;// 离线时间
			long etime = roomUser.connect_time;// 当前时间，上线时间。
			// TODO
			// 判断是否有离线消息，如果有离线消息则将历史消息开始时间改为下线时间，过滤离线数据。目前此条件不支持多人聊天，如需支持需要支持，每条消息需要记录消息状态。
			List<JSONObject> offlineMsgList = this.getOffLineMsg(uid, rid,
					stime, etime, false);
			String hirstoryEndTime = offlineMsgList.size() > 0 ? String
					.valueOf(roomUser.disconnect_time) : String
					.valueOf(roomUser.connect_time);
			// 第一次登录未下线，则设置为上线时间 因为 endTime =
			// 有离线数据?下线时间:上线时间，如果等于0则证明有离线数据并且还未下过线,此时应该查询不到数据（离线已经推送过一次），而不是查询所有数据。
			// if ("0".equals(hirstoryEndTime)) {
			// hirstoryEndTime = "" + System.currentTimeMillis();
			// }
			Map<String, String> cols = new HashMap<String, String>();
			cols.put("uid", uid);
			cols.put("content", "");
			cols.put("timestamp", "");
			cols.put("filterContent", "");
			HbaseRow row = new HbaseRow(
					HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT, "", cols);
			HbaseDAO hbaseDAO = new HbaseDAO();
			hbaseDAO.Init(true);
			List<HbaseRow> list = new ArrayList<HbaseRow>();
			if ("true".equals(ispagination)) {
				list = hbaseDAO.findRange(HbaseConfig.HBASE_TABLE_PREFIX_ROOM
						+ rid, null, hirstoryEndTime, Integer.valueOf(cpage),
						Integer.valueOf(pagesize), row, HbaseDAO.ORDER.ASC);
			} else {
				list = hbaseDAO.findRange(HbaseConfig.HBASE_TABLE_PREFIX_ROOM
						+ rid, null, hirstoryEndTime, row);
			}
			for (HbaseRow hbaseRow : list) {
				Map<String, String> retCols = hbaseRow.getCols();
				// 如果有过滤则覆盖原文
				if (StringUtils.isNotEmpty(retCols.get("filterContent"))) {
					retCols.put("content", retCols.get("filterContent"));
					retCols.remove("filterContent");
				}
				String _uid = retCols.get("uid");
				String _msg = retCols.get("content");
				String _timestamp = retCols.get("timestamp");
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("uid", _uid);
				jsonObject.put("msg", _msg);
				jsonObject.put("timestamp", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date(Long
						.parseLong(_timestamp))));
				msglist.add(jsonObject);
			}
			hbaseDAO.UnInit();
			SendToPHP(obj, msglist.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
			SendToPHP(obj, "FAILED");
		}
	}

	/**
	 * 返回聊天记录
	 * 
	 * @param obj
	 */
	private void offLineMsg(JSONObject obj) {
		try {
			List<JSONObject> msglist = new ArrayList<JSONObject>();
			String uid = Util.GetStringFromJSon("uid", obj);
			String rid = Util.GetStringFromJSon("rid", obj);
			// TODO：根据roomid+uid 查询 离线时间
			// 可以考虑将离线时间上线时间缓存则redis，通过udf将MYSQL数据同步至REDIS
			RoomDAO roomDAO = new RoomDAO();
			roomDAO.Init(true);
			RoomUserBean roomUser = new RoomUserBean();
			roomUser.room_id = Integer.parseInt(rid);
			roomUser.user_id = Integer.parseInt(uid);
			roomUser = roomDAO.selectRoomUser(roomUser);
			if (roomUser != null) {
				long stime = roomUser.disconnect_time;// 离线时间
				long etime = roomUser.connect_time;// 当前时间，上线时间。
				if (etime != 0) {
					msglist = getOffLineMsg(uid, rid, stime, etime, true);
				}
			}
			roomDAO.UnInit();
			SendToPHP(obj, msglist.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
			SendToPHP(obj, "FALIED:" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param uid
	 * @param rid
	 * @param stime
	 * @param etime
	 * @param isFilter
	 *            添加此参数为是否过滤掉自己的离线消息，因为历史消息与离线消息共用此方法，历史应该传false，离线应该传true
	 *            modify by hanson 2015/10/15
	 * @return
	 * @throws Exception
	 */
	private List<JSONObject> getOffLineMsg(String uid, String rid, long stime,
			long etime, boolean isFilter) throws Exception {
		List<JSONObject> msglist = new ArrayList<JSONObject>();
		Map<String, String> cols = new HashMap<String, String>();
		cols.put("uid", uid);
		cols.put("content", "");
		cols.put("filterContent", "");
		cols.put("timestamp", "");
		HbaseRow row = new HbaseRow(
				HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT, "", cols);
		HbaseDAO hbaseDAO = new HbaseDAO();
		hbaseDAO.Init(true);
		List<HbaseRow> list = hbaseDAO.findRange(
				HbaseConfig.HBASE_TABLE_PREFIX_ROOM + rid, "" + stime, ""
						+ etime, row);
		for (HbaseRow hbaseRow : list) {
			Map<String, String> retCols = hbaseRow.getCols();
			// 如果有过滤则覆盖原文
			if (StringUtils.isNotEmpty(retCols.get("filterContent"))) {
				retCols.put("content", retCols.get("filterContent"));
				retCols.remove("filterContent");
			}
			String _uid = retCols.get("uid");
			// 当用户重复登录时会造成自己收到自己离线消息，过滤自己发送的离线消息
			if (uid.equals(_uid) && isFilter) {
				continue;
			}
			String _msg = retCols.get("content");
			String _timestamp = retCols.get("timestamp");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("uid", _uid);
			jsonObject.put("msg", _msg);
			jsonObject.put("timestamp", new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss").format(new Date(Long
					.parseLong(_timestamp))));
			msglist.add(jsonObject);
		}
		hbaseDAO.UnInit();
		return msglist;
	}

	private void disconnectRoom(JSONObject obj) {
		try {
			JSONObject pushObj = new JSONObject();
			String rid = Util.GetStringFromJSon("rid", obj);
			String uid = Util.GetStringFromJSon("uid", obj);
			long disconnectTime = System.currentTimeMillis();
			RoomDAO roomDAO = new RoomDAO();
			roomDAO.Init(true);
			// TODO:更新用户登录时间
			RoomUserBean roomUser = new RoomUserBean();
			roomUser.room_id = Integer.parseInt(rid);
			roomUser.user_id = Integer.parseInt(uid);
			roomUser.disconnect_time = disconnectTime;
			if (isLocalCreate(rid)) {
				roomDAO.updateConnectTime(roomUser);
				roomDAO.Commit();
				roomDAO.UnInit();
				// 不同节点调用异步请求
				// 调用同步，同步登录时间
				this.syncUpdateDisConnectTime(obj, roomUser);
			} else {
				roomDAO.updateNodeConnectTime(roomUser);
				roomDAO.Commit();
				roomDAO.UnInit();
				// 不同节点调用异步请求
				// 调用同步，同步登录时间
				this.syncUpdateNodeDisConnectTime(obj, roomUser);
			}
			pushObj.put("rid", rid);
			pushObj.put("disconnect_time", disconnectTime);
			SendToPHP(obj, pushObj.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.log(e);
			SendToPHP(obj, "FALIED:" + e.getMessage());
		}
	}

	public void roomhistory(JSONObject obj) {
		try {
			String rid = Util.GetStringFromJSon("rid", obj);
			HbaseDAO hbaseDAO = new HbaseDAO();
			hbaseDAO.Init(true);
			List<JSONObject> historyList = new ArrayList<JSONObject>();
			Map<String, String> cols = new HashMap<String, String>();
			cols.put("uid", "");
			cols.put("content", "");
			cols.put("timestamp", "");
			HbaseRow row = new HbaseRow(
					HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT, "", cols);
			List<HbaseRow> roomHistoryList = hbaseDAO.findRange(
					HbaseConfig.HBASE_TABLE_PREFIX_ROOM + rid, null, null, row);
			for (HbaseRow hbaseRow : roomHistoryList) {
				Map<String, String> retCols = hbaseRow.getCols();
				// 如果有过滤则覆盖原文
				String _uid = retCols.get("uid");
				String _msg = retCols.get("content");
				String _timestamp = retCols.get("timestamp");
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("uid", _uid);
				jsonObject.put("msg", _msg);
				jsonObject.put("timestamp", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(new Date(Long
						.parseLong(_timestamp))));
				historyList.add(jsonObject);
			}
			hbaseDAO.UnInit();
			SendToPHP(obj, historyList.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
			SendToPHP(obj, "FALIED:" + e.getMessage());
		}
	}

	public void offlineRoomMsgCount(JSONObject obj) {

		try {
			SendToPHP(obj, "OK");
			String uid = Util.GetStringFromJSon("uid", obj);
			String sid = Util.GetStringFromJSon("sid", obj);
			List<JSONObject> roomUnreadList = new ArrayList<JSONObject>();
			// 根据用户ID查询所有参与过的聊天室
			RoomDAO roomDAO = new RoomDAO();
			roomDAO.Init(true);
			RoomUserBean roomUserQuery = new RoomUserBean();
			roomUserQuery.user_id = Integer.parseInt(uid);
			List<RoomUserBean> roomUserList = roomDAO
					.selectRoomUserList(roomUserQuery);
			// 根据roomID查询是否有离线消息
			Log4j.log("offlineRoomMsgCount:[uid]:" + uid
					+ " roomUserList.size():" + roomUserList.size());
			if (roomUserList.size() > 0) {
				HbaseDAO hbaseDAO = new HbaseDAO();
				hbaseDAO.Init(true);
				long etime = System.currentTimeMillis();// 本次登录时间,上线时间。期间有消息
				for (RoomUserBean roomUser : roomUserList) {
					Log4j.log("offlineRoomMsgCount:[uid]:" + uid
							+ " scan room " + roomUser.room_id + " msgCount:");
					int rid = roomUser.room_id;// 房间号
					long stime = roomUser.disconnect_time;// 离线时间不为空则取离线时间，如果为空则取0(默认为0）.未登录过房间则为0
					// 取所有数据
					// 没加入过房间页推送离线消息，所以去掉开始时间。
					if (etime != 0) {
						Map<String, String> cols = new HashMap<String, String>();
						cols.put("uid", uid);
						HbaseRow row = new HbaseRow(
								HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT,
								"", cols);
						List<HbaseRow> offlineMsgList = hbaseDAO.findRange(
								HbaseConfig.HBASE_TABLE_PREFIX_ROOM + rid,
								String.valueOf(stime), String.valueOf(etime),
								row);
						// 如果有离线记录则添加提示信息
						Log4j.log("offlineRoomMsgCount:[uid]:" + uid
								+ " roomid:" + roomUser.room_id + "offlineMsg:"
								+ offlineMsgList.size());
						if (offlineMsgList.size() > 0) {
							RoomBean room = new RoomBean();
							room.room_id = rid;
							room = roomDAO.selectRoomByRoom(room);
							String roomName = room.name;
							JSONObject nameJsonObject = new JSONObject(roomName);
							Map<String, Integer> msgCountMap = new HashMap<String, Integer>();
							for (HbaseRow hbaseRow : offlineMsgList) {
								Map<String, String> retCols = hbaseRow
										.getCols();
								String _uid = retCols.get("uid");
								// 当用户重复登录时会造成自己收到自己离线消息，过滤自己发送的离线消息 modify by
								// hanson 2015/10/15
								if (uid.equals(_uid)) {
									continue;
								}
								if (msgCountMap.containsKey(_uid)) {
									msgCountMap.put(_uid,
											msgCountMap.get(_uid) + 1);
								} else {
									msgCountMap.put(_uid, 1);
								}
							}
							Set<String> keys = msgCountMap.keySet();
							for (String userId : keys) {
								JSONObject jsonObject = new JSONObject();
								String ocode = nameJsonObject
										.getString("oCode");
								String nickName = nameJsonObject
										.getString(userId + "_nickName");
								String tRole = nameJsonObject.getString(userId
										+ "_role");
								String tUid = userId;
								jsonObject.put("ocode", ocode);
								jsonObject.put("tnickName", nickName);
								jsonObject.put("tRole", tRole);
								jsonObject.put("tUid", tUid);
								jsonObject.put("msgCount",
										msgCountMap.get(userId));
								roomUnreadList.add(jsonObject);
								Log4j.log("offlineRoomMsgCount:[uid]:" + uid
										+ " roomid:" + roomUser.room_id
										+ " sourceUser:" + userId
										+ " msgCount:"
										+ msgCountMap.get(userId));
							}
						}
					}
				}
				hbaseDAO.UnInit();
				JSONObject pushObject = new JSONObject();
				pushObject.put("sid", sid);
				pushObject.put("uid", uid);
				pushObject.put("aid", "roomnew");
				pushObject.put("roomMsgCount", roomUnreadList.toString());
				Log4j.log("offlineRoomMsgCount:[uid]:" + uid + " push msg:"
						+ pushObject.toString());
				_pushServer.Report(pushObject);
				Log4j.log("offlineRoomMsgCount:[uid]:" + uid + " push end");
			}
			roomDAO.UnInit();

		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
			SendToPHP(obj, "FALIED:" + e.getMessage());
		}
	}

	public void SyncOfflineData(JSONObject obj) {

		try {
			String uid = Util.GetStringFromJSon("uid", obj);
			String sid = Util.GetStringFromJSon("sid", obj);
			List<JSONObject> roomUnreadList = new ArrayList<JSONObject>();
			// 根据用户ID查询所有参与过的聊天室
			RoomDAO roomDAO = new RoomDAO();
			roomDAO.Init(true);
			RoomUserBean roomUserQuery = new RoomUserBean();
			roomUserQuery.user_id = Integer.parseInt(uid);
			List<RoomUserBean> roomUserList = roomDAO
					.selectRoomUserList(roomUserQuery);
			// 根据roomID查询是否有离线消息
			Log4j.log("offlineRoomMsgCount:[uid]:" + uid
					+ " roomUserList.size():" + roomUserList.size());
			if (roomUserList.size() > 0) {
				HbaseDAO hbaseDAO = new HbaseDAO();
				hbaseDAO.Init(true);
				long etime = System.currentTimeMillis();// 本次登录时间,上线时间。期间有消息
				for (RoomUserBean roomUser : roomUserList) {
					Log4j.log("offlineRoomMsgCount:[uid]:" + uid
							+ " scan room " + roomUser.room_id + " msgCount:");
					int rid = roomUser.room_id;// 房间号
					long stime = roomUser.disconnect_time;// 离线时间不为空则取离线时间，如果为空则取0(默认为0）.未登录过房间则为0
					// 取所有数据
					// 没加入过房间页推送离线消息，所以去掉开始时间。
					if (etime != 0) {
						Map<String, String> cols = new HashMap<String, String>();
						cols.put("uid", uid);
						HbaseRow row = new HbaseRow(
								HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT,
								"", cols);
						List<HbaseRow> offlineMsgList = hbaseDAO.findRange(
								HbaseConfig.HBASE_TABLE_PREFIX_ROOM + rid,
								String.valueOf(stime), String.valueOf(etime),
								row);
						// 如果有离线记录则添加提示信息
						Log4j.log("offlineRoomMsgCount:[uid]:" + uid
								+ " roomid:" + roomUser.room_id + "offlineMsg:"
								+ offlineMsgList.size());
						if (offlineMsgList.size() > 0) {
							RoomBean room = new RoomBean();
							room.room_id = rid;
							room = roomDAO.selectRoomByRoom(room);
							String roomName = room.name;
							JSONObject nameJsonObject = new JSONObject(roomName);
							Map<String, Integer> msgCountMap = new HashMap<String, Integer>();
							for (HbaseRow hbaseRow : offlineMsgList) {
								Map<String, String> retCols = hbaseRow
										.getCols();
								String _uid = retCols.get("uid");
								// 当用户重复登录时会造成自己收到自己离线消息，过滤自己发送的离线消息 modify by
								// hanson 2015/10/15
								if (uid.equals(_uid)) {
									continue;
								}
								if (msgCountMap.containsKey(_uid)) {
									msgCountMap.put(_uid,
											msgCountMap.get(_uid) + 1);
								} else {
									msgCountMap.put(_uid, 1);
								}
							}
							Set<String> keys = msgCountMap.keySet();
							for (String userId : keys) {
								JSONObject jsonObject = new JSONObject();
								String ocode = nameJsonObject
										.getString("oCode");
								String nickName = nameJsonObject
										.getString(userId + "_nickName");
								String tRole = nameJsonObject.getString(userId
										+ "_role");
								String tUid = userId;
								jsonObject.put("ocode", ocode);
								jsonObject.put("tnickName", nickName);
								jsonObject.put("tRole", tRole);
								jsonObject.put("tUid", tUid);
								jsonObject.put("msgCount",
										msgCountMap.get(userId));
								roomUnreadList.add(jsonObject);
								Log4j.log("offlineRoomMsgCount:[uid]:" + uid
										+ " roomid:" + roomUser.room_id
										+ " sourceUser:" + userId
										+ " msgCount:"
										+ msgCountMap.get(userId));
							}
						}
					}
				}
				hbaseDAO.UnInit();
			}
			JSONObject pushObject = new JSONObject();
			pushObject.put("sid", sid);
			pushObject.put("uid", uid);
			pushObject.put("aid", "syncData");
			pushObject.put("roomMsgCount", roomUnreadList.toString());
			SendToPHP(obj, pushObject.toString());
			Log4j.log("syncData offlineRoomMsgCount:[uid]:" + uid + " push end");
			roomDAO.UnInit();

		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error(e);
			SendToPHP(obj, "FALIED:" + e.getMessage());
		}
	}
}
