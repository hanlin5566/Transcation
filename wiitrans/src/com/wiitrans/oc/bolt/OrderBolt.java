package com.wiitrans.oc.bolt;

import java.io.Console;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.FailUtil;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.PushServer;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class OrderBolt extends BaseBasicBolt {
    private TaskReportor _reportor = null;
    private RedisCache  _cache = null;
    private PushServer _pushServer = null;
    private HashMap<Integer, ConfigNode> _sync_url;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
	WiitransConfig.getInstance(0);

	Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
	_sync_url = new HashMap<Integer, ConfigNode>();
	for (Integer node_id : set) {
	    if (node_id > 0) {
		if (!_sync_url.containsKey(node_id)) {
		    ConfigNode bs = new ConfigNode();
		    bs.nid = BundleConf.BUNDLE_Node.get(node_id).nid;
		    bs.timeout = BundleConf.BUNDLE_Node.get(node_id).timeout;
		    bs.api = BundleConf.BUNDLE_Node.get(node_id).api
			    + "automation/newtask/";
		    _sync_url.put(bs.nid, bs);
		    Log4j.log("          recom-sync nid = " + bs.nid
			    + " url = " + bs.api);
		}
	    }
	}
	_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
		BundleConf.ORDERCENTER_BUNDLE_PORT);
	_reportor.Start();

	if (_cache == null) {
	    _cache = new RedisCache();
	    _cache.Init(BundleConf.BUNDLE_REDIS_IP);
	}
	if (_pushServer == null) {
	    _pushServer = new PushServer();
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

    /**
     * 
     * @param nid
     *            节点ID
     * @param ordercode
     *            订单编号
     * @param transSet
     *            符合权限的议员列表
     * @param type
     *            级别1T可抢 2E可抢 3T+E可抢
     * @param owner
     *            已经挂上的T或E的议员ID
     * @return
     */
    private int TranslatorOrderSet(int nid, String ordercode,
	    Set<String> transSet, int type, int owner) {
	int ret = Const.FAIL;

	if (ordercode != null && transSet != null) {
	    for (String trans_e : transSet) {
		boolean editor = false;// 是否有E权限
		String[] trans = trans_e.split("_");
		if (trans.length == 0 || trans.length >= 3) {
		    continue;
		}

		if (trans.length == 2) {
		    editor = true;
		}
		int translator_id = Util.String2Int(trans[0]);// 议员ID
		// 译员ID无效或等于拥有者【已经抢过该订单的人】
		if (owner > 0) {
		    MyOrderList(owner, ordercode);
		}
		if (translator_id <= 0 || translator_id == owner) {
		    continue;
		}
		OrderTransList(ordercode, translator_id);
		switch (type) {
		case Const.RESERVE_TYPE_T:
		    OrderListT(nid, translator_id, ordercode);
		    break;
		case Const.RESERVE_TYPE_E:
		    if (editor) {
			OrderListE(nid, translator_id, ordercode);
		    }
		    break;
		case Const.RESERVE_TYPE_TE:
		    OrderListT(nid, translator_id, ordercode);
		    if (editor) {
			OrderListE(nid, translator_id, ordercode);
		    }
		    break;
		default:
		    break;
		}
		JSONObject pushObj = new JSONObject();
		String userkey = "uid_" + translator_id;
		// 如果没查找到userinfo则去其他节点查找
		for (Integer node_id : BundleConf.BUNDLE_Node.keySet()) {
		    String userInfoString = _cache.GetString(node_id, userkey);
		    if (userInfoString != null) {
			JSONObject jsonObject = new JSONObject(userInfoString);
			pushObj.put("sid", jsonObject.get("sid"));
			pushObj.put("aid", "neworder");
			_pushServer.Report(node_id,pushObj);
			break;
		    }
		}			
	    }

	    ret = Const.SUCCESS;
	}

	return ret;
    }
    /**
     * 根据技能取议员
     * @param pair_id
     * @return
     */
    private Set<String> getTransBySkill(int pair_id){
	String key = Util.GetRedisKey(pair_id);
	return _cache.smembers(key);
    }
    private Set<String> getTransBySkill(int pair_id,int industry_id){
	String key = Util.GetRedisKeyForIndustry(pair_id,industry_id);
	return _cache.smembers(key);
    }
    private Set<String> getTransBySkill_grade(int pair_id,int grade_id){
	Set<String> ret = new HashSet<String>();
	if (grade_id == Const.PRICE_LEVEL_PRACTICAL) {
	    // 1 实用级所有级别可抢，所以取出此语言对下所有人
	    Set<String> elementaryTransSet = this.getTransBySkill(pair_id);
	    ret.addAll(elementaryTransSet);
	} else {
	    // 3 高级议员可抢所有订单默认取出该语言对下的高级译员
	    String key = Util.GetRedisKeyForGrade(pair_id, Const.PRICE_LEVEL_PUBLISH);
	    Set<String> advancedTransSet = _cache.smembers(key);//高级议员
	    ret.addAll(advancedTransSet);
	    if (grade_id == Const.PRICE_LEVEL_STANDARD) {
		//2 如果为标准级取出中级议员，补推中级议员
		key = Util.GetRedisKeyForGrade(pair_id, Const.PRICE_LEVEL_STANDARD);
		Set<String> interTransset = _cache.smembers(key);//中级议员
		ret.addAll(interTransset);
	    }
	}
	return ret;
    }
    private Set<String> getTransBySkill(int pair_id,int industry_id, int grade_id){
	Set<String> ret = new HashSet<String>();
	// 1 实用级所有级别可抢，所以取出此语言对下所有人
	    if (grade_id == Const.PRICE_LEVEL_PRACTICAL) {
		String key = Util.GetRedisKeyForIndustry(pair_id, industry_id);
		Set<String> elementaryTransSet = _cache.smembers(key);//初级议员
		ret.addAll(elementaryTransSet);
	    } else {
		// 3 高级议员可抢所有订单默认取出该语言对下的高级译员
		String key = Util.GetRedisKeyForGradeIndustry(pair_id, Const.PRICE_LEVEL_PUBLISH,industry_id);
		Set<String> advancedTransSet = _cache.smembers(key);//高级议员
		ret.addAll(advancedTransSet);
		if (grade_id == Const.PRICE_LEVEL_STANDARD) {
		    //2 如果为标准级取出中级议员，补推中级议员
		    key = Util.GetRedisKeyForGradeIndustry(pair_id, Const.PRICE_LEVEL_STANDARD,industry_id);
		    Set<String> interTransset = _cache.smembers(key);//中级议员
		    ret.addAll(interTransset);
		}
	    }
	return ret;
    }
    
    
    private int Order(int nid, String ordercode, JSONObject obj) {
	String orderkey = "order_" + ordercode;
	String sOrder = _cache.GetString(nid, orderkey);
	if (sOrder == null || sOrder.trim().length() == 0) {
	    Log4j.log(" orderjson in redis is null. " + ordercode);
	    return SendToPHP(obj, FailUtil.GetFailedMsg(
		    FailUtil.SERVICE_ORDERCENTER, FailUtil.CLASS_ORDER_BOLT, 3));
	}
	JSONObject orderJSON = new JSONObject(sOrder);

	int pair_id = Util.GetIntFromJSon("pair_id", orderJSON);
	int industry_id = Util.GetIntFromJSon("industry_id", orderJSON);
	int price_level_id = Util.GetIntFromJSon("price_level_id", orderJSON);
	int match_type = Util.GetIntFromJSon("match_type", orderJSON);
	int translator_id = Util.GetIntFromJSon("translator_id", orderJSON);
	int editor_id = Util.GetIntFromJSon("editor_id", orderJSON);
	if (translator_id == 0 || price_level_id > Const.PRICE_LEVEL_PRACTICAL && editor_id == 0) {
	    // 1T可抢，2E可抢，3T+E可抢
	    int type = ((translator_id == 0) ? Const.RESERVE_TYPE_T : 0)+ 
		    ((price_level_id > Const.PRICE_LEVEL_PRACTICAL && editor_id == 0) ? Const.RESERVE_TYPE_E : 0);
	    // E或T未被抢才会进入此判断，所以会如果有T或E被抢则owner等于其ID否则等于0
	    int owner = (translator_id > 0 ? translator_id: (editor_id > 0 ? editor_id : 0));
	    // 待抢订单
	    // match_type==0 不手动指定
	    if (match_type == Const.ORDER_MATCH_TYPE_NORMAL) {
		// 通用领域 分级别源对
		if (BundleConf.BUNDLE_OTHERS_INDUSTRYID == industry_id) {
		    Set<String> transSet = getTransBySkill_grade(pair_id,price_level_id);
		    TranslatorOrderSet(nid, ordercode, transSet, type, owner);
		} else {
		    // 分源对 领域 级别
		    Set<String> transSet = getTransBySkill(pair_id,industry_id,price_level_id);
		    TranslatorOrderSet(nid, ordercode, transSet, type, owner);
		}
	    }
	    /**
	     * 客户选择推荐议员，推送指定议员 match_type==4 推送T match_type==5 推送E match_type==6
	     * 推送T+E
	     */
	    else if (match_type >= Const.ORDER_MATCH_TYPE_RECOM_T && match_type <= Const.ORDER_MATCH_TYPE_RECOM_TE) {
		//T可抢或TE可抢
		if (type == Const.RESERVE_TYPE_T || type == Const.RESERVE_TYPE_TE) {
		    String recom_t = Util.GetStringFromJSon("recom_t",orderJSON);
		    String[] translator_ids = recom_t.split(",");
		    Set<String> set = new HashSet<String>();
		    for (String transID : translator_ids) {
			//MLV分语言对，领域，级别
			String transID_e = transID + "_E";
			Set<String> transSet = new HashSet<String>();
			// 通用领域 分级别源对
			if (BundleConf.BUNDLE_OTHERS_INDUSTRYID == industry_id) {
			    transSet = getTransBySkill_grade(pair_id,price_level_id);
			} else {
			    // 分源对 领域 级别
			    transSet = getTransBySkill(pair_id,industry_id,price_level_id);
			}
			if (transSet.contains(transID_e)) {
			    set.add(transID_e);
			} else if (transSet.contains(transID)) {
			    set.add(transID);
			}
		    }
		    TranslatorOrderSet(nid, ordercode, set, Const.RESERVE_TYPE_T, owner);
		}
		//E可抢或TE可抢
		if (type == Const.RESERVE_TYPE_E || type == Const.RESERVE_TYPE_TE) {
		    String recom_e = Util.GetStringFromJSon("recom_e",orderJSON);
		    String[] translator_ids = recom_e.split(",");
		    Set<String> set = new HashSet<String>();
		    for (String transID : translator_ids) {
			//MLV分语言对，领域，级别
			String transID_e = transID + "_E";
			Set<String> skillTransSet = new HashSet<String>();
			// 通用领域 分级别源对
			if (BundleConf.BUNDLE_OTHERS_INDUSTRYID == industry_id) {
			    skillTransSet = getTransBySkill_grade(pair_id,price_level_id);
			} else {
			    // 分源对 领域 级别
			    skillTransSet = getTransBySkill(pair_id,industry_id,price_level_id);
			}
			if (skillTransSet.contains(transID_e)) {
			    set.add(transID_e);
			} else if (skillTransSet.contains(transID)) {
			    set.add(transID);
			}
		    }
		    TranslatorOrderSet(nid, ordercode, set, Const.RESERVE_TYPE_E, owner);
		}
	    }
	    // 专属团队
	    else if (match_type == Const.ORDER_MATCH_TYPE_TEAM) {
		String team = Util.GetStringFromJSon("team", orderJSON);
		//modify by hanson 2016.08.19 专属团队不分领域
//		key = Util.GetRedisKeyForIndustry(pair_id, industry_id);
		String[] translator_ids = new String[0];
		//可能为空，如果为空说明关闭了专属团队，或者团队下人为空,则走普通下单流程所有人可抢
		if(team == null){
		    translator_ids = getTransBySkill(pair_id,industry_id,price_level_id).toArray(translator_ids);
		}else{
		    translator_ids = team.split(",");
		}
		Set<String> skillTransSet = getTransBySkill(pair_id);
		Set<String> set = new HashSet<String>();
		for (String transID : translator_ids) {
		    String transID_e = transID + "_E";
		    if (skillTransSet.contains(transID_e)) {
			set.add(transID_e);
		    } else if (skillTransSet.contains(transID)) {
			set.add(transID);
		    }
		}
		TranslatorOrderSet(nid, ordercode, set, type, owner);
	    }

	    return SendToPHP(obj, FailUtil.SUCCESS);

	} else {
	    return SendToPHP(obj, FailUtil.GetFailedMsg(
		    FailUtil.SERVICE_ORDERCENTER, FailUtil.CLASS_ORDER_BOLT, 4));
	}

    }

    private int SyncTrans(String uid, String sid, int order_id, int pair_id,
	    int node_id, String dataTemplate) {
	int ret = Const.FAIL;
	ConfigNode sync;
	if (_sync_url.containsKey(node_id)) {
	    sync = _sync_url.get(node_id);
	    if (sync != null) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("className",
			"com.wiitrans.automation.logic.SyncDataLogicImpl");
		paramJson.put("order_id", String.valueOf(order_id));
		if (pair_id > 0) {
		    paramJson.put("pair_id", String.valueOf(pair_id));
		}
		paramJson.put("nid", String.valueOf(node_id));
		paramJson.put("uid", uid);
		paramJson.put("syncType", "send");
		paramJson.put("dataTemplate", dataTemplate);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sid", sid);
		jsonObject.put("uid", uid);
		jsonObject.put("param", paramJson);
		jsonObject.put("taskType", "1");
		jsonObject.put("corn", "");
		jsonObject.put("job_class",
			"com.wiitrans.automation.quartz.job.PushStromJob");

		new HttpSimulator(sync.api).executeMethodTimeOut(
			jsonObject.toString(), sync.timeout);
		return ret;
	    }
	}
	return ret;
    }

    private int SyncFileStatus(String uid, String sid, int order_id, int node_id) {
	int ret = Const.FAIL;
	ConfigNode sync;
	if (_sync_url.containsKey(node_id)) {
	    sync = _sync_url.get(node_id);
	    if (sync != null) {
		JSONObject paramJson = new JSONObject();
		paramJson.put("className",
			"com.wiitrans.automation.logic.SyncDataLogicImpl");
		paramJson.put("order_id", String.valueOf(order_id));
		paramJson.put("nid", String.valueOf(node_id));
		paramJson.put("syncType", "send");
		paramJson.put("dataTemplate", "setFileStatus");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("sid", sid);
		jsonObject.put("uid", uid);
		jsonObject.put("param", paramJson);
		jsonObject.put("taskType", "1");
		jsonObject.put("corn", "");
		jsonObject.put("job_class",
			"com.wiitrans.automation.quartz.job.PushStromJob");

		new HttpSimulator(sync.api).executeMethodTimeOut(
			jsonObject.toString(), sync.timeout);
		return ret;
	    }
	}
	return ret;
    }
    
    private boolean isReqOrderAuth(OrderBean orderBean,JSONObject obj){
	try {
	    String trantype = Util.GetStringFromJSon("trantype", obj);
	    String uid = Util.GetStringFromJSon("uid", obj);
	    boolean isTranslator = !"E".equalsIgnoreCase(trantype);
	    int matchType = orderBean.match_type;
	    int pair_id = orderBean.pair_id;
	    int industry_id = orderBean.industry_id;
	    int price_level_id = orderBean.price_level_id;
	    String key;
	    switch (matchType) {
	    case Const.ORDER_MATCH_TYPE_NORMAL://普通订单
		if (BundleConf.BUNDLE_OTHERS_INDUSTRYID == industry_id) {
		    key = Util.GetRedisKeyForGrade(pair_id,price_level_id);//不分领域，语言对级别
		} else {
		    // 分领域
		    key = Util.GetRedisKeyForGradeIndustry(pair_id, price_level_id,industry_id);
		}
		break;
	    case Const.ORDER_MATCH_TYPE_TEAM://专属团队 只分语言对
		key = Util.GetRedisKey(pair_id);//不分领域，语言对级别
		break;
	    default:
		//MLV 分级别领域源对
		key = Util.GetRedisKeyForGradeIndustry(pair_id, price_level_id,industry_id);
		break;
	    }
	    return _cache.sismember(key, uid);
	} catch (Exception e) {
	    Log4j.error(e);
	}
	return false;
    }
    
    private int RequireOrderSetUser(int nid, int onid, String ordercode,
	    JSONObject obj, JSONObject orderJSON) {
	int ret = Const.FAIL;
	int uid = Util.GetIntFromJSon("uid", obj);
	String sid = Util.GetStringFromJSon("sid", obj);
	// int order_id = Util.GetIntFromJSon("order_id", orderJSON);
	// int pair_id = Util.GetIntFromJSon("pair_id", orderJSON);
	// int industry_id = Util.GetIntFromJSon("industry_id", orderJSON);
	int price_level_id = Util.GetIntFromJSon("price_level_id", orderJSON);
	// int match_type = Util.GetIntFromJSon("match_type", orderJSON);
	// int translator_id = Util.GetIntFromJSon("translator_id", orderJSON);
	// int editor_id = Util.GetIntFromJSon("editor_id", orderJSON);

	String trantype = Util.GetStringFromJSon("trantype", obj);
	boolean isTranslator = !"E".equalsIgnoreCase(trantype);

	OrderBean orderBean = null;
	OrderDAO orderdao = null;
	try {

	    JSONObject json = new JSONObject();
	    json.put("nid", String.valueOf(onid));
	    String orderLockKey = Const.PREFIX_ORDRE_LOCK + ordercode + trantype;
	    Long order_lock = _cache.setnx(orderLockKey,"" + System.currentTimeMillis());// 订单是否被锁住，0锁住，1未锁。
	    if (order_lock == 0) {
		//订单被锁定
		Log4j.error("order(" + ordercode + ") is locked");

		SendToPHP(obj, FailUtil.GetFailedMsg(
			FailUtil.SERVICE_ORDERCENTER,
			FailUtil.CLASS_ORDER_BOLT, FailUtil.RESERVE_ORDER_LOCKED));
		return FailUtil.RESERVE_ORDER_LOCKED;
	    }
	    String myOrderListKey = Util.GetMyOrderListKey(uid);
	    Set<String> myOrderList =  _cache.smembers(myOrderListKey);
	    if (myOrderList.size() >= WiitransConfig.getInstance(0).OC.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT) {
		//超出最大可抢订单阈值
		Log4j.error("order(" + ordercode + ") more than Translator order max");

		SendToPHP(obj, FailUtil.GetFailedMsg(
			FailUtil.SERVICE_ORDERCENTER,
			FailUtil.CLASS_ORDER_BOLT, FailUtil.RESERVE_ORDER_MORE_THAN_ORDER_MAX));
		//移除锁
		_cache.DelString(orderLockKey);
		return FailUtil.RESERVE_ORDER_MORE_THAN_ORDER_MAX;
	    }
	    orderdao = new OrderDAO();
	    orderdao.Init(true, json);
	    orderBean = orderdao.Select(ordercode);
	    //进行中的订单
	    if (orderBean.status == Const.ORDER_STATUS_UNDERWAY) {
		//判断是否有抢单权限 低级别订单，高级别议员目前有些问题，有时间再修改
		//boolean hasAuth = isReqOrderAuth(orderBean, obj);
//		if(hasAuth){
//		    SendToPHP(obj, FailUtil.GetFailedMsg(
//				FailUtil.SERVICE_ORDERCENTER,
//				FailUtil.CLASS_ORDER_BOLT, FailUtil.RESERVE_ORDER_NOT_AUTH));
//		    //移除锁
//		    return FailUtil.RESERVE_ORDER_NOT_AUTH;
//		}
		if (isTranslator) {
		    // 抢T
		    if(orderBean.translator_id > 0) {
			//订单被抢
			Log4j.error("order(" + ordercode
				+ ") translator_id != 0");

			SendToPHP(obj, FailUtil.GetFailedMsg(
				FailUtil.SERVICE_ORDERCENTER,
				FailUtil.CLASS_ORDER_BOLT, FailUtil.RESERVE_ORDER_RESERVEED));
			return FailUtil.RESERVE_ORDER_RESERVEED;
		    } else if (orderBean.editor_id == uid) {
			//不能同时抢E与T
			Log4j.error("order("
				+ ordercode
				+ ") translator_id editor_id must be not equals. ");

			SendToPHP(obj, FailUtil.GetFailedMsg(
				FailUtil.SERVICE_ORDERCENTER,
				FailUtil.CLASS_ORDER_BOLT, FailUtil.RESERVE_ORDER_SAME_TRANSLATOR));

			return FailUtil.RESERVE_ORDER_SAME_TRANSLATOR;
		    } else {
			orderBean.translator_id = uid;
			orderBean.tnid = nid;
			orderdao.UpdateTrans(orderBean);
			orderdao.UpdateGetTimeT(orderBean.order_id);
			orderdao.Commit();
			orderBean = orderdao.Select(ordercode);
			//同步抢单结果
			if (orderBean.translator_id == uid) {
			    // ret = Const.SUCCESS;
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.node_id, "setTranslator");
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.tnid, "updateTranslator");
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.node_id, "updateOrderSearch");
			} else {
			    Log4j.error("order(" + ordercode
				    + ") translator_id != 0");

			    SendToPHP(obj, FailUtil.GetFailedMsg(
				    FailUtil.SERVICE_ORDERCENTER,
				    FailUtil.CLASS_ORDER_BOLT, 101));

			    return ret;
			}
		    }

		} else {
		 // 抢E
		    if (orderBean.editor_id > 0) {
			Log4j.error("order(" + ordercode + ") editor_id != 0");
			SendToPHP(obj, FailUtil.GetFailedMsg(
				FailUtil.SERVICE_ORDERCENTER,
				FailUtil.CLASS_ORDER_BOLT, 102));
			return ret;

		    } else if (orderBean.translator_id == uid) {
			Log4j.error("order("
				+ ordercode
				+ ") translator_id editor_id must be not equals. ");
			SendToPHP(obj, FailUtil.GetFailedMsg(
				FailUtil.SERVICE_ORDERCENTER,
				FailUtil.CLASS_ORDER_BOLT, 104));
			return ret;

		    } else {
			orderBean.editor_id = uid;
			orderBean.enid = nid;
			orderdao.UpdateEditor(orderBean);
			orderdao.UpdateGetTimeE(orderBean.order_id);
			orderdao.Commit();
			orderBean = orderdao.Select(ordercode);
			if (orderBean.editor_id == uid) {
			    // ret = Const.SUCCESS;
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.node_id, "setEditor");
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.enid, "updateTranslator");
			    SyncTrans(String.valueOf(uid), sid,
				    orderBean.order_id, orderBean.pair_id,
				    orderBean.node_id, "updateOrderSearch");
			} else {
			    Log4j.error("order(" + ordercode
				    + ") editor_id != 0");
			    SendToPHP(obj, FailUtil.GetFailedMsg(
				    FailUtil.SERVICE_ORDERCENTER,
				    FailUtil.CLASS_ORDER_BOLT, 102));
			    return ret;
			}
		    }
		}
		//实用级
		if (price_level_id == Const.PRICE_LEVEL_PRACTICAL) {
		    
		    if (orderBean.translator_id > 0) {
			orderdao.UpdateFileStatus(orderBean.order_id);
			orderdao.Commit();
			SyncFileStatus(String.valueOf(uid), sid,
				orderBean.order_id, orderBean.node_id);

		    }
		} else if (orderBean.translator_id > 0
			|| orderBean.editor_id > 0) {
		    orderdao.UpdateFileStatus(orderBean.order_id);
		    orderdao.Commit();
		    SyncFileStatus(String.valueOf(uid), sid,
			    orderBean.order_id, orderBean.node_id);
		}
		return Const.SUCCESS;
	    } else {
		Log4j.error("order(" + ordercode + ") status is wrong");
	    }

	} catch (Exception e) {
	    // e.printStackTrace();
	    Log4j.error(e);
	} finally {
	    if (orderdao != null) {
		orderdao.UnInit();
	    }
	}
	return ret;
    }

    private int reserveOrder(int nid, String ordercode, JSONObject obj) {
	int onid = Util.GetIntFromJSon("onid", obj);
	String orderkey = "order_" + ordercode;
	String sOrder = _cache.GetString(onid, orderkey);
	String trantype = Util.GetStringFromJSon("trantype", obj);
	if (sOrder == null || sOrder.trim().length() == 0) {
	    Log4j.log(" orderjson in redis is null. " + ordercode);
	    return SendToPHP(obj, FailUtil.GetFailedMsg(
		    FailUtil.SERVICE_ORDERCENTER, FailUtil.CLASS_ORDER_BOLT, 3));
	}

	JSONObject orderJSON = new JSONObject(sOrder);

	int ret = RequireOrderSetUser(nid, onid, ordercode, obj, orderJSON);
	String orderLockKey = Const.PREFIX_ORDRE_LOCK + ordercode + trantype;
	if(ret != Const.SUCCESS && ret != FailUtil.RESERVE_ORDER_LOCKED){
	    //订单因为其他原因抢单失败，需要释放锁
	    //移除锁
	    _cache.DelString(orderLockKey);
	}else if(Const.SUCCESS == ret) {
	    boolean isTranslator = !"E".equalsIgnoreCase(trantype);
	    int uid = Util.GetIntFromJSon("uid", obj);

	    String member = onid + "|" + ordercode;
	    // 获取可抢此订单的议员
	    String key = Util.GetOrderTransListKey(ordercode);
	    Set<String> set = _cache.smembers(key);
	    /**
	     * 移除已经挂上此单的议员
	     */
	    // 含有这个订单的译员去掉这次已经被抢的T或E
	    for (String translator_id : set) {
		if (isTranslator) {
		    String key_t = Util.GetOrderListTKey(Util
			    .String2Int(translator_id));
		    _cache.srem(key_t, member);
		} else {
		    String key_e = Util.GetOrderListEKey(Util
			    .String2Int(translator_id));
		    _cache.srem(key_e, member);
		}
	    }

	    // 这个译员这个订单的T和E都去掉
	    String key_t = Util.GetOrderListTKey(uid);
	    _cache.srem(key_t, member);
	    String key_e = Util.GetOrderListEKey(uid);
	    _cache.srem(key_e, member);

	    String myOrderListKey = Util.GetMyOrderListKey(uid);
	    _cache.sadd(myOrderListKey, ordercode);
	    
	    
	    //移除锁
	    _cache.DelString(orderLockKey);
	    
	    ret = SendToPHP(obj, FailUtil.SUCCESS);
	}
	return ret;

    }

    // 暂时无用
//    private void Push(int nid, String ordercode, JSONObject obj,
//	    JSONObject orderJSON) {
//	int order_id = Util.GetIntFromJSon("order_id", orderJSON);
//	int pair_id = Util.GetIntFromJSon("pair_id", orderJSON);
//	int industry_id = Util.GetIntFromJSon("industry_id", orderJSON);
//	int price_level_id = Util.GetIntFromJSon("price_level_id", orderJSON);
//	int match_type = Util.GetIntFromJSon("match_type", orderJSON);
//	int translator_id = Util.GetIntFromJSon("translator_id", orderJSON);
//	int editor_id = Util.GetIntFromJSon("editor_id", orderJSON);
//
//	int uid = Util.GetIntFromJSon("uid", obj);
//
//	String userkey = "uid_" + uid;
//	String userInfo = _cache.GetString(nid, userkey);
//	if (userInfo != null) {
//	    JSONObject userInfoObj = new JSONObject(userInfo);
//	    JSONObject langpairs = Util.GetJSonFromJSon("langpairs",
//		    userInfoObj);
//
//	    JSONObject langpair = Util.GetJSonFromJSon(String.valueOf(pair_id),
//		    langpairs);
//	    JSONObject pushObj = new JSONObject();
//
//	    pushObj.put("sid", Util.GetStringFromJSon("sid", obj));
//	    String trantype = Util.GetStringFromJSon("trantype", obj);
//	    if (!"E".equalsIgnoreCase(trantype)) {
//		pushObj.put("aid", "newt");
//		pushObj.put("type", "T");
//	    } else {
//		pushObj.put("aid", "newe");
//		pushObj.put("type", "E");
//	    }
//	    pushObj.put("nid", String.valueOf(nid));
//	    pushObj.put("name", Util.GetStringFromJSon("nickname", userInfoObj));
//	    pushObj.put("level", Util.GetStringFromJSon("level", userInfoObj));
//	    pushObj.put("words",
//		    Util.GetStringFromJSon("word_count", userInfoObj));
//	    pushObj.put("head", Util.GetStringFromJSon("head", userInfoObj));
//	    int normal_order_number = Util.String2Int(Util.GetStringFromJSon(
//		    "normal_order_number", userInfoObj));
//	    int total_order_number = Util.String2Int(Util.GetStringFromJSon(
//		    "total_order_number", userInfoObj));
//	    int credit = 0;
//	    if (total_order_number <= 0
//		    || normal_order_number >= total_order_number) {
//		credit = 100;
//	    } else {
//		credit = (int) Math.rint((double) normal_order_number * 100
//			/ total_order_number);
//	    }
//
//	    pushObj.put("credit", credit);
//	    int effective_word_count = Util.String2Int(Util.GetStringFromJSon(
//		    "effective_word_count", langpair));
//	    int pair_word_count = Util.String2Int(Util.GetStringFromJSon(
//		    "pair_word_count", langpair));
//
//	    int accuracy = 0;
//	    if (pair_word_count <= 0 || effective_word_count >= pair_word_count) {
//		accuracy = 100;
//	    } else {
//		accuracy = (int) Math.rint((double) effective_word_count * 100
//			/ pair_word_count);
//	    }
//	    pushObj.put("accuracy", accuracy);
//	    _pushServer.Report(pushObj);
//	}
//
//    }
    
    //用户提交订单，删除相关缓存
    private int DeleteOrder(String ordercode, JSONObject obj){
	try {
	    int uid = Util.GetIntFromJSon("uid", obj);
	    int onid = Util.GetIntFromJSon("fnid", obj);
	    String myOrderListKey = Util.GetMyOrderListKey(uid);
	    //删除本人的已抢订单
	    _cache.srem(myOrderListKey, ordercode);
	    // 删除此订单可抢单议员
	    String key = Util.GetOrderTransListKey(ordercode);
	    _cache.DelString(key);
	    //为防止删除错误，再次删除
	    String member = onid + "|" + ordercode;
	    // 这个译员这个订单的T和E都去掉
	    String key_t = Util.GetOrderListTKey(uid);
	    _cache.srem(key_t, member);
	    String key_e = Util.GetOrderListEKey(uid);
	    _cache.srem(key_e, member);
	    return SendToPHP(obj, FailUtil.SUCCESS);
	} catch (Exception e) {
	    Log4j.error("remove relation error(" + ordercode
		    + ")",e);
	    return SendToPHP(obj, FailUtil.GetFailedMsg(
		    FailUtil.SERVICE_ORDERCENTER,
		    FailUtil.CLASS_ORDER_BOLT, 105));
	}
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
	String ordercode = tuple.getStringByField("ordercode");
	String content = tuple.getStringByField("content");
	JSONObject obj = new JSONObject(content);
	String aid = Util.GetStringFromJSon("aid", obj);
	int nid = Util.GetIntFromJSon("nid", obj);
	String method = Util.GetStringFromJSon("method", obj);
	Log4j.debug("orderbolt " + obj.toString());

	switch (aid) {
	case "order": {
	    // recom/order
	    switch (method) {
	    case "POST": {
		// 客户产生新订单
		Order(nid, ordercode, obj);
		break;
	    }
	    case "DELETE": {
		DeleteOrder(ordercode, obj);
		break;
	    }
	    case "PUT": {
		// 议员抢单
		reserveOrder(nid, ordercode, obj);
		break;
	    }
	    default:
		SendToPHP(obj, FailUtil.GetFailedMsg(
			FailUtil.SERVICE_ORDERCENTER,
			FailUtil.CLASS_ORDER_BOLT, 3));
		break;
	    }

	    break;
	}
	default:
	    SendToPHP(obj, FailUtil.GetFailedMsg(FailUtil.SERVICE_ORDERCENTER,
		    FailUtil.CLASS_ORDER_BOLT, 3));
	    break;
	}

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
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

}
