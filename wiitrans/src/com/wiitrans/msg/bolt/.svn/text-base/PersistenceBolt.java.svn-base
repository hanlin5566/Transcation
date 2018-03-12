package com.wiitrans.msg.bolt;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.hbase.HbaseDAO;
import com.wiitrans.base.hbase.HbaseRow;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

// 1.Admin消息此处理器不许要处理
// 2.持久化Chat消息

public class PersistenceBolt extends BaseBasicBolt {
    private TaskReportor _reportor = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
	// AppConfig app = new AppConfig();
	// app.Parse();
	WiitransConfig.getInstance(0);
	_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
		BundleConf.MSG_BUNDLE_PORT);
	_reportor.Start();
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

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
	String room_id = tuple.getStringByField("room_id");
	String content = tuple.getStringByField("content");
	JSONObject obj = new JSONObject(content);
	String aid = Util.GetStringFromJSon("aid", obj);
	String method = Util.GetStringFromJSon("method", obj);
	switch (method) {
	case "POST": {
	    switch (aid) {
	    case "newmsg": {
		// 聊天消息记录至HBASE
		saveMsgContent(room_id, content);
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
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer arg0) {

    }

    private void saveMsgContent(String room_id, String content) {
	JSONObject obj = new JSONObject(content);
	try {
	    String uid = Util.GetStringFromJSon("uid", obj);
	    String sourceMsg = Util.GetStringFromJSon("sourcemsg", obj);
	    String msg = Util.GetStringFromJSon("msg", obj);
	    String timestamp = Util.GetStringFromJSon("timestamp", obj);
	    Map<String, String> cols = new HashMap<String, String>();
	    cols.put("uid", uid);
	    cols.put("content", sourceMsg);
	    if ("true".equals(Util.GetStringFromJSon("isFilter", obj))) {
		cols.put("filterContent", msg);
	    }
	    cols.put("timestamp", timestamp);
	    HbaseRow row = new HbaseRow(
		    HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT, timestamp,
		    cols);
	    HbaseDAO hbaseDAO = new HbaseDAO();
	    hbaseDAO.Init(true);
	    hbaseDAO.insert(HbaseConfig.HBASE_TABLE_PREFIX_ROOM + room_id, row);
	    hbaseDAO.UnInit();
	} catch (Exception e) {
	    Log4j.error("保存聊天记录异常:" + e.getMessage() + "  记录未：[room_id:"
		    + room_id + "] [content:" + content + "]");
	    SendToPHP(obj, "FALIED:" + e.getMessage());
	    // Hbase创建聊天记录表
	    try {
		HbaseDAO hbaseDAO = new HbaseDAO();
		hbaseDAO.Init(true);
		hbaseDAO.createTable(HbaseConfig.HBASE_TABLE_PREFIX_ROOM + room_id,
			false, HbaseConfig.HBASE_COLUMN_FAMILY_MSG_CONTENT);
		hbaseDAO.UnInit();
	    } catch (Exception e2) {
		Log4j.error("创建聊天记录表异常:" + e2.getMessage());
	    }
	}

    }
}
