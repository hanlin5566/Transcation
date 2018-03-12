package com.wiitrans.oc.bolt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.FailUtil;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.WiitransConfig;

public class TranslatorBolt extends BaseBasicBolt {

    private TaskReportor _reportor = null;
    private RedisCache _cache = null;
    private TaskReportor _pushServer = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
	WiitransConfig.getInstance(0);

	_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
		BundleConf.ORDERCENTER_BUNDLE_PORT);
	_reportor.Start();

	if (_cache == null) {
	    _cache = new RedisCache();
	    _cache.Init(BundleConf.BUNDLE_REDIS_IP);
	}
	if (_pushServer == null) {
	    _pushServer = new TaskReportor(BundleConf.BUNDLE_PUSHSERVER_IP,
		    BundleConf.BUNDLE_PUSHSERVER_PORT, false);
	    _pushServer.Start();
	}
    }
    
    private void MyOrderList(int translator_id, String ordercode) {
	String key = Util.GetMyOrderListKey(translator_id);
	_cache.sadd(key, ordercode);
    }

    public void OrderTransList(String ordercode, int translator_id) {
	String key = Util.GetOrderTransListKey(ordercode);
	_cache.sadd(key, String.valueOf(translator_id));
    }

    public void OrderListT(int nid, int translator_id, String ordercode) {
	String key = Util.GetOrderListTKey(translator_id);
	_cache.sadd(key, nid + "|" + ordercode);
    }

    public void OrderListE(int nid, int translator_id, String ordercode) {
	String key = Util.GetOrderListEKey(translator_id);
	_cache.sadd(key, nid + "|" + ordercode);
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

    private int GetOrders(int uid, JSONObject obj) {

	JSONObject orders = new JSONObject();
	String myOrderListKey = Util.GetMyOrderListKey(uid);
	Set<String> myOrderList = _cache.smembers(myOrderListKey);
	if (myOrderList.size() >= WiitransConfig.getInstance(0).OC.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT) {
	    // 超出最大可抢订单阈值
	    Log4j.error("translator(" + uid
		    + ") more than Translator order max");
	    SendToPHP(obj, FailUtil.GetFailedMsg(FailUtil.SERVICE_ORDERCENTER,
		    FailUtil.CLASS_ORDER_BOLT,
		    FailUtil.RESERVE_ORDER_MORE_THAN_ORDER_MAX));
	}
	String key_t = Util.GetOrderListTKey(uid);
	Set<String> set_t = _cache.smembers(key_t);
	StringBuffer order_t = new StringBuffer();
	for (String nid_ordercode : set_t) {
	    order_t.append(nid_ordercode).append(" ");
	}
	orders.put("order_t", order_t.toString());

	String key_e = Util.GetOrderListEKey(uid);
	Set<String> set_e = _cache.smembers(key_e);
	StringBuffer order_e = new StringBuffer();
	for (String nid_ordercode : set_e) {
	    order_e.append(nid_ordercode).append(" ");
	}
	orders.put("order_e", order_e.toString());

	obj.put("orders", orders);
	obj.put("result", "OK");
	obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
	obj.put(Const.BUNDLE_INFO_ID,
		Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
	obj.put(Const.BUNDLE_INFO_ACTION_ID,
		Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

	return _reportor.Report(obj);
    }
    
    private int putOrders(JSONObject obj) {
	try {
	    String pair_id = Util.GetStringFromJSon("pair_id", obj);
	    String industry_id = Util.GetStringFromJSon("industry_id", obj);
	    String grade_id = Util.GetStringFromJSon("grade_id", obj);
	    String translator_id = Util.GetStringFromJSon("translator_id", obj);
	    String hasE = Util.GetStringFromJSon("hasE", obj);
	    OrderDAO orderdao = new OrderDAO();
	    orderdao.Init(true);
	    Map<String, Object> param = new HashMap<String, Object>();
	    param.put("pair_id", pair_id);
	    List<String> industry_ids;
	    industry_ids = Arrays.asList(industry_id.split(","));
	    param.put("industry_id", industry_ids);
	    param.put("grade_id", grade_id);
	    param.put("translator_id", translator_id);
	    // 查询可抢T订单
	    List<Map<String, String>> stayOrderList = orderdao
		    .SelectStayOrdersByTransLator(param);
	    for (Map<String, String> order : stayOrderList) {
		String orderCode = order.get("code");
		String nid = String.valueOf((order.get("node").equals("m") ? BundleConf.DEFAULT_NID
			: order.get("node")));
		OrderTransList(orderCode, Integer.parseInt(translator_id));
		OrderListT(Integer.parseInt(nid),
			Integer.parseInt(translator_id), orderCode);
	    }
	    // 查询可抢E订单
	    if ("true".equalsIgnoreCase(hasE)) {
		stayOrderList = orderdao.SelectStayOrdersByEditor(param);
		for (Map<String, String> order : stayOrderList) {
		    String orderCode = order.get("code");
		    String nid = String.valueOf((order.get("node").equals("m") ? BundleConf.DEFAULT_NID
			    : order.get("node")));
		    OrderTransList(orderCode,
			    Integer.parseInt(translator_id));
		    OrderListE(Integer.parseInt(nid),
			    Integer.parseInt(translator_id), orderCode);
		}
	    }
	    orderdao.UnInit();
	    
	} catch (Exception e) {
	    Log4j.error(e);
	}
	obj.put("result", "OK");
	obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
	obj.put(Const.BUNDLE_INFO_ID,
	    Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
	obj.put(Const.BUNDLE_INFO_ACTION_ID,
	    Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
	return _reportor.Report(obj);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
	String uid = tuple.getStringByField("uid");
	String content = tuple.getStringByField("content");
	JSONObject obj = new JSONObject(content);
	String aid = Util.GetStringFromJSon("aid", obj);
	String method = Util.GetStringFromJSon("method", obj);
	// int nid = Util.GetIntFromJSon("nid", obj);
	Log4j.debug("TranslatorBolt uid:" + uid + " json:" + obj.toString());

	if (uid == null) {
	    Log4j.log("translatorbolt uid is null");
	    SendToPHP(obj, FailUtil.GetFailedMsg(FailUtil.SERVICE_ORDERCENTER,
		    FailUtil.CLASS_SPOUT, 1));
	} else {
	    switch (aid) {
	    case "order": {
		switch (method) {
		// 客户下单
		case "POST": {
		    String ordercode = Util.GetStringFromJSon("ordercode", obj);
		    // 发送Storm消息到下一个Bolt
		    collector.emit(new Values(ordercode, content));
		    break;
		}
		// 获取订单列表
		case "GET": {
		    GetOrders(Util.String2Int(uid), obj);
		    break;
		}
		default:
		    String ordercode = Util.GetStringFromJSon("ordercode", obj);
		    // 发送Storm消息到下一个Bolt
		    collector.emit(new Values(ordercode, content));
		    break;
		}
		break;
	    }
	    case "translator": {
		// 客户下单
		switch (method) {
		// 议员新技能
		case "PUT": {
		    putOrders(obj);
		    break;
		}
		default:
		    break;
		}
		break;
	    }
	    default:
		SendToPHP(obj, FailUtil.GetFailedMsg(
			FailUtil.SERVICE_ORDERCENTER,
			FailUtil.CLASS_TRANSLATOR_BOLT, 3));
		break;
	    }
	}
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declare(new Fields("ordercode", "content"));
    }

}
