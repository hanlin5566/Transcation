package com.wiitrans.msg.bolt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.RoomDAO;
import com.wiitrans.base.db.model.RoomBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.ChatServer;
import com.wiitrans.base.task.PushServer;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

// 路由器负责转发消息到(PushServer和未来的ChatServer)

public class RouterBolt extends BaseBasicBolt {
    private TaskReportor _reportor = null;
    private ICache _cache = null;
    private PushServer _pushServer = null;
    private ChatServer _chatServer = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
	// AppConfig app = new AppConfig();
	// app.Parse();
	WiitransConfig.getInstance(0);
	_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
		BundleConf.MSG_BUNDLE_PORT);
	_reportor.Start();

	if (_cache == null) {
	    _cache = new RedisCache();
	    _cache.Init(BundleConf.BUNDLE_REDIS_IP);
	}

	_pushServer = new PushServer();

	_chatServer = new ChatServer();

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

    public void PostCount(JSONObject obj) {

	String uid = Util.GetStringFromJSon("senduid", obj);
	int nid = Util.GetIntFromJSon("nid", obj);
	String userkey = "uid_" + uid;
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
	    JSONObject userInfoObj = new JSONObject(userInfo);
	    String sid = Util.GetStringFromJSon("sid", userInfoObj);
	    JSONObject object = new JSONObject();
	    object.put("sid", sid);
	    object.put("uid", uid);
	    object.put("aid", "newm");
	    object.put("msgcount", Util.GetStringFromJSon("msgcount", obj));
	    _pushServer.Report(object);
	}

	SendToPHP(obj, "OK");
    }

    public void SendMsg(JSONObject obj) {
	JSONObject pushObj = new JSONObject();
	/**
	 * socket.oid = obj.oid; socket.rid = obj.rid; socket.sid = obj.sid;
	 * socket.room = obj.oid + '-' + obj.rid; socket.peer = obj.oid + '-' +
	 * obj.rid + '-' + obj.sid; if(obj && obj.hasOwnProperty('room') &&
	 * obj.hasOwnProperty('peerid') && obj.hasOwnProperty('aid'))
	 */
	if (String.valueOf(BundleConf.DEFAULT_NID).equals(
		Util.GetStringFromJSon("nid", obj))) {
	    String oid = Util.GetStringFromJSon("oid", obj);
	    int rid = Util.GetIntFromJSon("rid", obj);
	    String sid = Util.GetStringFromJSon("sid", obj);
	    String aid = Util.GetStringFromJSon("aid", obj);
	    String timestamp = Util.GetStringFromJSon("timestamp", obj);
	    String msg = Util.GetStringFromJSon("msg", obj);
	    String uid = Util.GetStringFromJSon("uid", obj);
	    String tuid = Util.GetStringFromJSon("tuid", obj);
	    String tuserOnlineStatus = Util.GetStringFromJSon(
		    "tuserOnlineStatus", obj);

	    pushObj.put("room", rid);
	    pushObj.put("peerid", oid + '-' + rid + '-' + tuid);
	    pushObj.put("aid", aid);
	    pushObj.put("timestamp",
		    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			    .format(new Date(Long.parseLong(timestamp))));
	    pushObj.put("msg", msg);
	    // TODO:未获取用户名
	    pushObj.put("uid", uid);
	    pushObj.put("sid", sid);

	    /**
	     * var timeval = evtObj.time; var msgval = evtObj.msg; var msguser =
	     * evtObj.user;
	     */
	    // 此处取出的状态有可能为空，所以判断是否online
	    int user_nid = 0;
	    // chat分开部署后向user所在nid的chat发送
	    int nid = Util.GetIntFromJSon("nid", obj);
	    String userkey = "uid_" + tuid;
	    String userInfoString = _cache.GetString(nid, userkey);
	    if (userInfoString == null) {
		// 如果没查找到userinfo则去其他节点查找
		BundleConf.BUNDLE_Node.keySet();
		for (Integer node_id : BundleConf.BUNDLE_Node.keySet()) {
		    if (node_id != nid) {
			userInfoString = _cache.GetString(node_id, userkey);
			if (userInfoString != null) {
			    user_nid = node_id;
			    break;
			}
		    }
		}
	    }
	    user_nid = user_nid==0?nid:user_nid;
	    if ("online".equals(tuserOnlineStatus)) {
		Log4j.log("send msg --> node:"+user_nid+" msg:" + obj.toString());
		_chatServer.Report(user_nid, pushObj);
	    } else {
		offlineRoomMsgCount(obj);
	    }
	    String sendStatus = "OK";
	    if ("true".equals(Util.GetStringFromJSon("isFilter", obj))) {
		sendStatus = "FILTER";
	    }
	    SendToPHP(obj, sendStatus);
	} else {
	    SendToPHP(obj, "OK");
	}
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {

	String content = tuple.getStringByField("content");
	JSONObject obj = new JSONObject(content);
	String aid = Util.GetStringFromJSon("aid", obj);
	String method = Util.GetStringFromJSon("method", obj);
	String rid = Util.GetStringFromJSon("rid", obj);

	Log4j.log("routerbolt " + obj.toString());

	boolean send = true;

	switch (method) {
	case "POST": {
	    switch (aid) {
	    case "newm": {
		// 信息推送
		PostCount(obj);
		send = false;
		break;
	    }
	    case "newmsg": {
		// 聊天消息
		SendMsg(obj);
		send = true;
		break;
	    }
	    default:
		break;
	    }
	    break;
	}
	default:
	    break;
	}
	// 发送Storm消息到下一个Bolt
	if (send) {
	    collector.emit(new Values(rid, content));
	}
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declare(new Fields("room_id", "content"));
    }

    // TODO 发送提示离线消息记录
    public void offlineRoomMsgCount(JSONObject obj) {

	try {
	    String uid = Util.GetStringFromJSon("uid", obj);
	    String rid = Util.GetStringFromJSon("rid", obj);
	    String tuid = Util.GetStringFromJSon("tuid", obj);
	    JSONObject pushObject = new JSONObject();
	    String userkey = "uid_" + tuid;
	    int nid = Util.GetIntFromJSon("nid", obj);
	    String userInfoString = _cache.GetString(nid, userkey);
	    int user_nid = 0;
	    if (userInfoString == null) {
		// 如果没查找到userinfo则去其他节点查找
		BundleConf.BUNDLE_Node.keySet();
		for (Integer node_id : BundleConf.BUNDLE_Node.keySet()) {
		    if (node_id != nid) {
			userInfoString = _cache.GetString(node_id, userkey);
			if (userInfoString != null) {
			    user_nid = node_id;
			    break;
			}
		    }
		}
	    }
	    Log4j.log("offline--> userInfo" + userInfoString.toString());
	    user_nid = user_nid==0?nid:user_nid;
	    if (userInfoString != null) {
		JSONObject userInfo = new JSONObject(userInfoString);
		List<JSONObject> roomUnreadList = new ArrayList<JSONObject>();
		// 根据用户ID查询所有参与过的聊天室
		RoomDAO roomDAO = new RoomDAO();
		roomDAO.Init(true);
		RoomBean room = new RoomBean();
		room.room_id = Integer.parseInt(rid);
		room = roomDAO.selectRoomByRoom(room);
		// 拼装返回JSON
		if (room != null) {
		    String roomName = room.name;
		    JSONObject nameJsonObject = new JSONObject(roomName);
		    JSONObject jsonObject = new JSONObject();
		    String ocode = nameJsonObject.getString("oCode");
		    String tNickName = nameJsonObject.getString(uid
			    + "_nickName");
		    String tRole = nameJsonObject.getString(uid + "_role");
		    jsonObject.put("ocode", ocode);
		    jsonObject.put("tnickName", tNickName);
		    jsonObject.put("tRole", tRole);
		    jsonObject.put("tUid", uid);
		    jsonObject.put("msgCount", "increase");
		    roomUnreadList.add(jsonObject);
		}
		roomDAO.UnInit();
		pushObject.put("sid", Util.GetStringFromJSon("sid", userInfo));
		pushObject.put("uid", tuid);
		pushObject.put("aid", "roomNewMsg");
		pushObject.put("roomMsgCount", roomUnreadList.toString());
		Log4j.log("offline--> send push " + pushObject.toString());
		_pushServer.Report(user_nid, pushObject);
		SendToPHP(obj, "OK");
	    }
	} catch (Exception e) {
	    // TODO: handle exception
	    SendToPHP(obj, e.getMessage());
	    e.printStackTrace();
	}
    }
}
