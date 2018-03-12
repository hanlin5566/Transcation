package com.wiitrans.recom.bolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.order.Order;
import com.wiitrans.base.order.Order.TYPE;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.translator.Translator;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

// 以定位为Field存储当前临时订单，及已经申请该订单的译员信息
public class OrderBolt extends BaseBasicBolt {

	private HashMap<String, Order> _orders = new HashMap<String, Order>();
	private TaskReportor _reportor = null;
	private ICache _cache = null;
	private TaskReportor _pushServer = null;
	private HashMap<Integer, ConfigNode> _sync_url;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

//		AppConfig app = new AppConfig();
//		app.Parse();
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
				BundleConf.RECOMMEND_BUNDLE_PORT);
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

	private int NotifyBundle(String result, JSONObject obj, Order order,
			String setuid, boolean Complete) {
		// 通知Bundle订单匹配译员成功
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put("type", Util.GetStringFromJSon("type", obj));
		resObj.put("trantype", Util.GetStringFromJSon("trantype", obj));
		resObj.put("uid", Util.GetStringFromJSon("uid", obj));
		resObj.put("nid", Util.GetStringFromJSon("nid", obj));
		resObj.put("setuid", setuid);
		resObj.put("ordercode", order._code);
		resObj.put("langpair", String.valueOf(order._langpair));
		resObj.put("industry_id", String.valueOf(order._industry_id));
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put("Complete", Complete ? "Complete" : "");

		return _reportor.Report(resObj);
	}

	private int CompleteOrder(String ordercode, JSONObject obj) {
		int ret = Const.FAIL;

		// 将正式订单相关文件存入FastDFS
		// 将正式订单存入DB

		Order order = _orders.get(ordercode);
		switch (order._type) {
		case NORMAL: {
			if (order._translator != null) {
				// 移除匹配成功的正式订单
				_orders.remove(ordercode);
				ret = Const.SUCCESS;
			}
			break;
		}
		case VIP: {
			if ((order._translator != null) && (order._editor != null)) {
				// 移除匹配成功的正式订单
				_orders.remove(ordercode);
				ret = Const.SUCCESS;
			}
			break;
		}
		default:
			Log4j.error("OrderBolt order(" + ordercode
					+ ")type error，must be normal or vip");
			break;
		}

		return ret;
	}

	private int Order(String ordercode, JSONObject obj) {

		if (!_orders.containsKey(ordercode)) {
			Order order = new Order();
			order._sid = Util.GetStringFromJSon("sid", obj);
			order._uid = Util.GetStringFromJSon("uid", obj);
			order._nid = Util.GetStringFromJSon("nid", obj);
			String langpair = Util.GetStringFromJSon("langpair", obj);
			order._langpair = Util.String2Int(langpair);
			order._industry_id = Util.GetIntFromJSon("industry_id", obj);
			order._code = ordercode;

			int translator = Util.GetIntFromJSon("translator", obj);
			int editor = Util.GetIntFromJSon("editor", obj);

			if (translator > 0) {
				order._translator = new Translator();
				order._translator._uid = String.valueOf(translator);
			}

			// JSon数据转换到Order对象
			String type = Util.GetStringFromJSon("type", obj);
			switch (type) {
			case "vip": {
				order._type = TYPE.VIP;
				if (editor > 0) {
					order._editor = new Translator();
					order._editor._uid = String.valueOf(editor);
				}
				break;
			}
			case "normal": {
				order._type = TYPE.NORMAL;
				break;
			}
			default:
				Log4j.error("OrderBolt order(" + ordercode
						+ ")type error，must be normal or vip");
				break;
			}

			_orders.put(ordercode, order);

		} else {
			// Log4j.log("Order is exist " + ordercode);
		}

		return SendToPHP(obj, "OK");
	}

	private int DeleteOrder(String ordercode, JSONObject obj) {

		if (_orders.containsKey(ordercode)) {
			String complete = Util.GetStringFromJSon("complete", obj);

			if (complete != null) {
				if (complete.equalsIgnoreCase("complete")) {
					_orders.remove(ordercode);
				}
			}
		}

		return SendToPHP(obj, "OK");
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

	private int RequireOrderSetUser(String ordercode, Translator tran,
			Translator other, boolean isTranslator, Order order, String type) {
		int ret = Const.FAIL;

		if (other != null && other._uid.equals(tran._uid)) {
			return ret;
		}
		// 译员信息赋值
		if (isTranslator) {
			order._translator = tran;
		} else {
			order._editor = tran;
		}
		OrderBean orderBean = null;

		OrderDAO orderdao = null;
		try {
			JSONObject json = new JSONObject();
			// json.put("nid", tran._nid);
			json.put("nid", order._nid);
			orderdao = new OrderDAO();
			orderdao.Init(true, json);
			orderBean = orderdao.Select(ordercode);
			if (orderBean.status == 20) {
				if (isTranslator) {
					orderBean.translator_id = Util.String2Int(tran._uid);
					orderBean.tnid = Util.String2Int(tran._nid);
					orderdao.UpdateTrans(orderBean);
					orderdao.UpdateGetTimeT(orderBean.order_id);
					orderdao.Commit();
					ret = Const.SUCCESS;
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.node_id,
							"setTranslator");
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.tnid, "updateTranslator");
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.node_id,
							"updateOrderSearch");
				} else {
					orderBean.editor_id = Util.String2Int(tran._uid);
					orderBean.enid = Util.String2Int(tran._nid);
					orderdao.UpdateEditor(orderBean);
					orderdao.UpdateGetTimeE(orderBean.order_id);
					orderdao.Commit();
					ret = Const.SUCCESS;
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.node_id, "setEditor");
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.enid, "updateTranslator");
					SyncTrans(tran._uid, tran._sid, orderBean.order_id,
							orderBean.pair_id, orderBean.node_id,
							"updateOrderSearch");
				}

				if (type.equals("normal")) {
					if (orderBean.translator_id > 0) {
						orderdao.UpdateFileStatus(orderBean.order_id);
						orderdao.Commit();
						SyncFileStatus(tran._uid, tran._sid,
								orderBean.order_id, orderBean.node_id);
					}
				} else if (orderBean.translator_id > 0
						|| orderBean.editor_id > 0) {
					orderdao.UpdateFileStatus(orderBean.order_id);
					orderdao.Commit();
					SyncFileStatus(tran._uid, tran._sid, orderBean.order_id,
							orderBean.node_id);
				}
				ret = Const.SUCCESS;
			} else {
				Log4j.error("order(" + ordercode + ")status is wrong");
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

	private int RequireOrder(String ordercode, JSONObject obj, String uid,
			String type) {
		int ret = Const.FAIL;

		if (_orders.containsKey(ordercode)) {
			Order order = _orders.get(ordercode);

			Translator translator = null;
			Translator otherTranslator = null;
			boolean isTranslator = true;
			switch (type) {
			case "normal": {
				translator = order._translator;
				isTranslator = true;
				break;
			}
			case "vip": {
				String tranType = Util.GetStringFromJSon("trantype", obj);
				switch (tranType) {
				case "T": {
					translator = order._translator;
					otherTranslator = order._editor;
					isTranslator = true;
					break;
				}
				case "E": {
					translator = order._editor;
					otherTranslator = order._translator;
					isTranslator = false;
					break;
				}
				default:
					Log4j.error("OrderBolt order(" + ordercode
							+ ")type error，must be T or E");
					break;
				}
				break;
			}
			default:
				Log4j.error("OrderBolt order(" + ordercode
						+ ")type error，must be normal or vip");
				break;
			}

			if (null == translator) {
				// 抢单成功
				Translator tran = new Translator();
				// 根据译员uid到Redis获取译员信息
				int nid = Util.GetIntFromJSon("nid", obj);
				String userkey = "uid_" + uid;
				String userInfo = _cache.GetString(nid, userkey);
				if (userInfo != null) {
					JSONObject userInfoObj = new JSONObject(userInfo);
					JSONObject langpairs = Util.GetJSonFromJSon("langpairs",
							userInfoObj);

					if (langpairs.has(String.valueOf(order._langpair))) {

						String sid = Util.GetStringFromJSon("sid", userInfoObj);
						tran._uid = uid;
						tran._sid = sid;
						tran._nid = String.valueOf(nid);
						ret = RequireOrderSetUser(ordercode, tran,
								otherTranslator, isTranslator, order, type);
						if (Const.SUCCESS == ret) {
							// 通知Bundle译员抢单成功，Bundle通知TranslatorBolt，TranslatorBolt回复PHP。
							JSONObject langpair = Util.GetJSonFromJSon(
									String.valueOf(order._langpair), langpairs);
							JSONObject pushObj = new JSONObject();

							pushObj.put("sid", order._sid);
							if (isTranslator) {
								pushObj.put("aid", "newt");
								pushObj.put("type", "T");
							} else {
								pushObj.put("aid", "newe");
								pushObj.put("type", "E");
							}
							pushObj.put("nid", order._nid);
							pushObj.put("name", Util.GetStringFromJSon(
									"nickname", userInfoObj));
							pushObj.put("level", Util.GetStringFromJSon(
									"level", userInfoObj));
							pushObj.put("words", Util.GetStringFromJSon(
									"word_count", userInfoObj));
							pushObj.put("head",
									Util.GetStringFromJSon("head", userInfoObj));
							int normal_order_number = Util.String2Int(Util
									.GetStringFromJSon("normal_order_number",
											userInfoObj));
							int total_order_number = Util.String2Int(Util
									.GetStringFromJSon("total_order_number",
											userInfoObj));
							int credit = 0;
							if (total_order_number <= 0
									|| normal_order_number >= total_order_number) {
								credit = 100;
							} else {
								credit = (int) Math
										.rint((double) normal_order_number
												* 100 / total_order_number);
							}

							pushObj.put("credit", credit);
							int effective_word_count = Util.String2Int(Util
									.GetStringFromJSon("effective_word_count",
											langpair));
							int pair_word_count = Util.String2Int(Util
									.GetStringFromJSon("pair_word_count",
											langpair));

							int accuracy = 0;
							if (pair_word_count <= 0
									|| effective_word_count >= pair_word_count) {
								accuracy = 100;
							} else {
								accuracy = (int) Math
										.rint((double) effective_word_count
												* 100 / pair_word_count);
							}
							pushObj.put("accuracy", accuracy);
							_pushServer.Report(pushObj);

							ret = CompleteOrder(ordercode, obj);
							ret = NotifyBundle("syn", obj, order, uid,
									Const.SUCCESS == ret);

							if (Const.SUCCESS == ret) {
								// ret = SendToPHP(obj, "OK");
							} else {
								ret = SendToPHP(obj, "FAILED");
							}

						} else {
							ret = SendToPHP(obj, "FAILED");
						}

					} else {
						// 帐号没有该语言对权限
						ret = SendToPHP(obj, "FAILED");
					}
				} else {
					// 帐号未登录
					ret = SendToPHP(obj, "FAILED");
				}
			} else {
				// 抢单失败
				// 回复PHP，订单已被其他译员抢走
				Log4j.log("order(" + ordercode + ") has been robbed");
				ret = SendToPHP(obj, "FAILED");
			}

		} else {
			// 抢单失败
			// 回复PHP，订单已被其他译员抢走
			Log4j.log("order(" + ordercode + ") has been robbed");
			ret = SendToPHP(obj, "FAILED");
		}

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		String ordercode = tuple.getStringByField("ordercode");
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String uid = Util.GetStringFromJSon("uid", obj);
		String method = Util.GetStringFromJSon("method", obj);
		String type = Util.GetStringFromJSon("type", obj);

		Log4j.debug("orderbolt " + obj.toString());

		switch (aid) {
		case "order": {
			// recom/order
			switch (method) {
			case "POST": {
				// 客户产生新订单
				Order(ordercode, obj);
				break;
			}
			case "DELETE": {
				DeleteOrder(ordercode, obj);
				break;
			}
			case "PUT": {
				// 译员抢单
				RequireOrder(ordercode, obj, uid, type);
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
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}
