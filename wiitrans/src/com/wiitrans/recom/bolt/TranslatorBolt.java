package com.wiitrans.recom.bolt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.DictIndustryDAO;
import com.wiitrans.base.db.model.DictIndustryBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.order.Order;
import com.wiitrans.base.order.Order.TYPE;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.translator.Translator;
import com.wiitrans.base.translator.TranslatorGrade;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

// 以语言对为Field存储当前测试通过的译员，及该译员可以申请或抢的订单
public class TranslatorBolt extends BaseBasicBolt {

	// 会话ID，译员信息
	public HashMap<String, Translator> _translators = new HashMap<String, Translator>();
	private TaskReportor _reportor = null;
	private ICache _cache = null;
	private TaskReportor _pushServer = null;
	// 订单列表参数
	// public int _maxWaitOrderSize = 2;
	// public int _maxGetOrderSize = 2;
	public int _maxOrderListSize = 10;
	private int othersIndustryID = 11;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

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

		// BundleParam param = app._bundles.get("recomTopo");
		BundleParam param = WiitransConfig.getInstance(0).RECOM;

		_maxOrderListSize = param.BUNDLE_TRANSLATOR_ORDER_MAXCOUNT;

		if (_maxOrderListSize <= 0) {
			_maxOrderListSize = 10;
		}

		DictIndustryDAO dao = null;
		try {
			dao = new DictIndustryDAO();
			dao.Init(false);
			DictIndustryBean bean = dao.SelectOthers();
			if (bean != null) {
				othersIndustryID = bean.industry_id;
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
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

	private int Login(String uid, JSONObject obj) {
		int ret = Const.FAIL;

		try {
			Translator tran;
			if (!_translators.containsKey(uid)) {
				tran = new Translator();
				if (uid != null) {
					// 译员登入到DB或Redis中根据uid获取译员相关信息
					int nid = Util.GetIntFromJSon("nid", obj);
					String userkey = "uid_" + uid;
					String userInfo = _cache.GetString(nid, userkey);
					JSONObject userInfoObj = new JSONObject(userInfo);

					tran._uid = uid;

					String sid = Util.GetStringFromJSon("sid", userInfoObj);
					tran._sid = sid;

					tran._nid = String.valueOf(nid);

					if (tran.gradeeditor == null) {
						tran.gradeeditor = new HashMap<String, TranslatorGrade>();
					}

					JSONObject langpairs = Util.GetJSonFromJSon("langpairs",
							userInfoObj);
					JSONObject langpair_industry = Util.GetJSonFromJSon(
							"langpair_industry", obj);
					JSONObject langpair_industry_grade_editor = Util
							.GetJSonFromJSon("langpair_industry_grade_editor",
									obj);

					TranslatorGrade transGrade;
					String[] names = JSONObject.getNames(langpairs);
					if (names != null) {
						for (String name : names) {
							String[] industry_ids = Util.GetStringFromJSon(
									name, langpair_industry).split("-");
							String[] industry_grade_editors = Util
									.GetStringFromJSon(name,
											langpair_industry_grade_editor)
									.split(",");
							if (industry_ids.length != industry_grade_editors.length) {
								continue;
							}
							if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
								for (int i = 0; i < industry_ids.length; i++) {
									String industry_id = industry_ids[i];
									String key = name + "-" + industry_id;
									if (!tran.gradeeditor.containsKey(key)) {
										String[] industry_grade_editor = industry_grade_editors[i]
												.split("-");
										transGrade = new TranslatorGrade();
										transGrade.translator = Util
												.String2Int(uid);
										transGrade.industry_id = Util
												.String2Int(industry_grade_editor[0]);
										transGrade.grade_id = Util
												.String2Int(industry_grade_editor[1]);
										transGrade.editor = industry_grade_editor[2]
												.equals("1");
										tran.gradeeditor.put(key, transGrade);
									}
								}
							} else {
								String industry_id = industry_ids[industry_ids.length - 1];
								String key = name + "-" + industry_id;
								if (!tran.gradeeditor.containsKey(key)) {
									String[] industry_grade_editor = industry_grade_editors[industry_ids.length - 1]
											.split("-");
									transGrade = new TranslatorGrade();
									transGrade.translator = Util
											.String2Int(uid);
									transGrade.industry_id = Util
											.String2Int(industry_grade_editor[0]);
									transGrade.grade_id = Util
											.String2Int(industry_grade_editor[1]);
									transGrade.editor = industry_grade_editor[2]
											.equals("1");
									tran.gradeeditor.put(key, transGrade);
								}
							}
						}
					}
				}
				JSONObject my = Util.GetJSonFromJSon("my", obj);
				if (my != null) {
					Set<String> myset = my.keySet();
					if (myset != null && myset.size() > 0) {
						for (String code : myset) {
							if (code != null && code.trim().length() > 0) {
								if (!tran._myOrders.containsKey(code)) {
									Order order = new Order();
									order._code = code;
									order._nid = my.getString(code);
									tran._myOrders.put(code, order);
									tran._myOrderListCount++;
								}
							}
						}
					}
				}
				if (tran._myOrderListCount < this._maxOrderListSize) {
					JSONObject nt = Util.GetJSonFromJSon("nt", obj);
					if (nt != null) {
						Set<String> ntset = nt.keySet();
						if (ntset != null && ntset.size() > 0) {
							for (String code : ntset) {
								if (code != null && code.trim().length() > 0) {
									if (!tran._normalOrderList
											.containsKey(code)) {
										Order order = new Order();
										order._code = code;
										order._nid = nt.getString(code);
										tran._normalOrderList.put(code, order);
										tran._orderListCount++;
									}
								}
							}
						}
					}

					JSONObject vt = Util.GetJSonFromJSon("vt", obj);
					if (vt != null) {
						Set<String> vtset = vt.keySet();
						if (vtset != null && vtset.size() > 0) {
							for (String code : vtset) {
								if (code != null && code.trim().length() > 0) {
									if (!tran._VIPOrderList_t.containsKey(code)) {
										Order order = new Order();
										order._code = code;
										order._nid = vt.getString(code);
										tran._VIPOrderList_t.put(code, order);
										tran._orderListCount++;
									}
								}
							}
						}
					}

					JSONObject ve = Util.GetJSonFromJSon("ve", obj);
					if (ve != null) {
						Set<String> veset = ve.keySet();
						if (veset != null && veset.size() > 0) {
							for (String code : veset) {
								if (code != null && code.trim().length() > 0) {
									if (!tran._VIPOrderList_e.containsKey(code)) {
										Order order = new Order();
										order._code = code;
										order._nid = ve.getString(code);
										tran._VIPOrderList_e.put(code, order);
										tran._orderListCount++;
									}
								}
							}
						}
					}
				}

				_translators.put(uid, tran);
			} else {
				Log4j.log("translator(" + uid + ") is logined");
			}

			ret = Const.SUCCESS;
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		} finally {

		}

		if (Const.SUCCESS == ret) {
			return SendToPHP(obj, "OK");
		} else {
			return SendToPHP(obj, "FAILED");
		}

	}

	private int Logout(String uid, JSONObject obj) {

		if (_translators.containsKey(uid)) {
			_translators.remove(uid);
		} else {
			Log4j.log("translator(" + uid
					+ ")is not logined，don't need to logout");
		}

		return SendToPHP(obj, "OK");
	}

	private int Order(String uid, JSONObject obj) {
		int ret = Const.FAIL;

		// 从JSon获取Order信息
		Order order = new Order();
		String nid = Util.GetStringFromJSon("nid", obj);
		order._nid = nid;
		String type = Util.GetStringFromJSon("type", obj);
		String ordercode = Util.GetStringFromJSon("ordercode", obj);
		String langpair = Util.GetStringFromJSon("langpair", obj);

		try {
			int translator = Util.GetIntFromJSon("translator", obj);
			int editor = Util.GetIntFromJSon("editor", obj);

			if (Util.String2Int(langpair) > 0) {

				if (0 == type.compareToIgnoreCase("VIP")) {
					order._type = TYPE.VIP;
				} else {
					order._type = TYPE.NORMAL;
				}

				// 发送给符合的空闲译员
				// 目前没有特殊算法，只是发给当前尚未Fill的译员，未来考虑根据喜好和历史信誉度等进行译员匹配
				if (_translators.containsKey(uid)) {
					Translator tran = _translators.get(uid);
					int userID = Util.String2Int(uid);
					int match_type = Util.GetIntFromJSon("match_type", obj);
					String recom_t = Util.GetStringFromJSon("recom_t", obj);
					String recom_e = Util.GetStringFromJSon("recom_e", obj);
					String price_level = Util.GetStringFromJSon("price_level",
							obj);
					int industry_id = Util.GetIntFromJSon("industry_id", obj);
					if (!BundleConf.BUNDLE_MATCH_INDUSTRY) {
						industry_id = othersIndustryID;
					}
					String key = langpair + "-" + industry_id;
					if (tran.gradeeditor != null
							&& tran.gradeeditor.containsKey(key)) {
						TranslatorGrade grade = tran.gradeeditor.get(key);

						if (tran._myOrderListCount < _maxOrderListSize
								&& !tran._myOrders.containsKey(ordercode)) {

							switch (order._type) {
							case NORMAL: {
								if (grade != null
										&& Util.String2Int(price_level) <= grade.grade_id
										&& translator <= 0) {
									if (!tran._normalOrderList
											.containsKey(ordercode)) {
										if (match_type == 4
												&& !Util.FindInSet(userID,
														recom_t)) {
											// 该译员不在match服务推荐译员T中
											break;

										}

										tran._normalOrderList.put(ordercode,
												order);
										tran._orderListCount++;
										ret = Const.SUCCESS;

									}
								}
								break;
							}
							case VIP: {
								if (grade != null
										&& grade.editor
										&& Util.String2Int(price_level) <= grade.grade_id
										&& editor <= 0) {
									if (!tran._VIPOrderList_e
											.containsKey(ordercode)) {
										if (match_type == 6) {
											if (Util.FindInSet(userID, recom_e)) {
												// 该译员在match服务推荐译员E中
												tran._VIPOrderList_e.put(
														ordercode, order);
												tran._orderListCount++;
												ret = Const.SUCCESS;
											}
										} else {
											tran._VIPOrderList_e.put(ordercode,
													order);
											tran._orderListCount++;
											ret = Const.SUCCESS;
										}

									}
								}
								if (grade != null
										&& Util.String2Int(price_level) <= grade.grade_id
										&& translator <= 0) {
									if (!tran._VIPOrderList_t
											.containsKey(ordercode)) {
										if (match_type == 4
												&& !Util.FindInSet(userID,
														recom_t)) {
											// 该译员不在match服务推荐译员T中
											break;
										}
										tran._VIPOrderList_t.put(ordercode,
												order);
										tran._orderListCount++;
										ret = Const.SUCCESS;
									}
								}

								break;
							}
							default:
								break;
							}
							// 发送消息到PushServer，通知该译员有新的订单列表
							if (ret == Const.SUCCESS) {
								JSONObject pushObj = new JSONObject();
								pushObj.put("sid", tran._sid);
								pushObj.put("aid", "neworder");
								_pushServer.Report(pushObj);
							}
							ret = Const.SUCCESS;
						} else {
							Log4j.log("TranslatorBolt translator(" + uid
									+ ")order is full or order is received("
									+ ordercode + ")");
						}

					} else {
						Log4j.error("TranslatorBolt translator（" + uid
								+ ")havn't langpair(" + langpair
								+ ") authority");
					}

				} else {
					Log4j.error("TranslatorBolt not find the tran " + uid);
				}
			} else {
				Log4j.error("TranslatorBolt langpair is wrong");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		} finally {

		}

		return ret;
	}

	private int RequireOrder(String uid, JSONObject obj) {
		int ret = Const.FAIL;
		if (_translators.containsKey(uid)) {
			Translator tran = _translators.get(uid);
			if (tran._myOrderListCount >= this._maxOrderListSize) {
				SendToPHP(obj, "FAILED");
			} else {
				ret = Const.SUCCESS;
			}
		} else {
			SendToPHP(obj, "FAILED");
		}
		return ret;
	}

	private int DeleteOrder(String uid, JSONObject obj) {
		int ret = Const.FAIL;
		String ordercode = Util.GetStringFromJSon("ordercode", obj);
		String complete = Util.GetStringFromJSon("complete", obj);

		if (ordercode != null) {

			Translator tran = null;
			Map.Entry<String, Translator> entry;
			Iterator iter = _translators.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry<String, Translator>) iter.next();
				tran = entry.getValue();

				if (tran._myOrders.containsKey(ordercode)) {
					tran._myOrders.remove(ordercode);
					tran._myOrderListCount--;
				}

				if (tran._normalOrderList.containsKey(ordercode)) {
					tran._normalOrderList.remove(ordercode);
					tran._orderListCount--;
				}

				if (tran._VIPOrderList_t.containsKey(ordercode)) {
					tran._VIPOrderList_t.remove(ordercode);
					tran._orderListCount--;
				}

				if (complete != null && complete.equalsIgnoreCase("complete")) {
					if (tran._VIPOrderList_e.containsKey(ordercode)) {
						tran._VIPOrderList_e.remove(ordercode);
						tran._orderListCount--;
					}
				}

			}
		}

		return ret;
	}

	private int GetOrder(String uid, JSONObject obj) {
		int ret = Const.FAIL;

		if (_translators.containsKey(uid)) {
			Translator tran = _translators.get(uid);

			// 整理推荐抢单返回给PHP
			Map.Entry<String, Order> entry;

			JSONObject orders = new JSONObject();

			String normalorders = "";
			Iterator iter = tran._normalOrderList.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry<String, Order>) iter.next();
				normalorders += entry.getValue()._nid + "|" + entry.getKey()
						+ " ";
			}

			String viplorders_t = "";
			iter = tran._VIPOrderList_t.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry<String, Order>) iter.next();
				viplorders_t += entry.getValue()._nid + "|" + entry.getKey()
						+ " ";
			}

			String viplorders_e = "";
			iter = tran._VIPOrderList_e.entrySet().iterator();
			while (iter.hasNext()) {
				entry = (Map.Entry<String, Order>) iter.next();
				viplorders_e += entry.getValue()._nid + "|" + entry.getKey()
						+ " ";
			}

			orders.put("normal", normalorders);
			orders.put("vip_t", viplorders_t);
			orders.put("vip_e", viplorders_e);
			orders.put("myordercount", tran._myOrderListCount);

			obj.put("orders", orders);
			obj.put("result", "OK");
			obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			obj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			obj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(obj);
		} else {
			Log4j.log("TranslatorBolt translator doesn't login，can't get orders");
			ret = SendToPHP(obj, "FAILED");
		}

		return ret;
	}

	private int OnlineUsers(JSONObject obj) {
		int ret = Const.FAIL;

		JSONObject users = new JSONObject();
		JSONObject user = new JSONObject();
		Translator tran = null;
		Set<String> uids = _translators.keySet();
		for (String uid : uids) {
			tran = null;
			tran = _translators.get(uid);
			user.put("mycount", tran._myOrderListCount);
			user.put("count", tran._orderListCount);
			user.put("mapsize", tran._myOrders.size());
			StringBuffer ordercode = new StringBuffer();
			if (tran._myOrders.size() > 0) {
				Set<String> ordercodes = tran._myOrders.keySet();
				for (String code : ordercodes) {
					ordercode.append(code).append(" ");
				}
				user.put("code", ordercode.toString());
			} else {
				user.put("code", "");
			}

			users.put("user" + String.valueOf(uid), user);
		}

		users.put("usercount", _translators.size());

		JSONObject result = new JSONObject();
		result.put("users", users);
		result.put("result", "OK");
		result.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		result.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		result.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		ret = _reportor.Report(result);

		return ret;
	}

	private int SynTrans(String uid, JSONObject obj, TYPE type) {
		int ret = Const.FAIL;

		// 该译员被客户选为订单接单人
		if (_translators.containsKey(uid)) {
			Translator tran = _translators.get(uid);
			String setuid = Util.GetStringFromJSon("setuid", obj);

			boolean isSelected = false;
			if (tran._uid.equals(setuid)) {
				isSelected = true;
			}

			String ordercode = Util.GetStringFromJSon("ordercode", obj);

			switch (type) {
			case VIP: {
				String trantype = Util.GetStringFromJSon("trantype", obj);
				if (trantype.equals("T")) {
					if (tran._VIPOrderList_t.containsKey(ordercode)) {
						if (isSelected) {
							tran._myOrders.put(ordercode,
									tran._VIPOrderList_t.remove(ordercode));
							tran._myOrderListCount++;
							tran._orderListCount--;
							if (tran._VIPOrderList_e.containsKey(ordercode)) {
								tran._VIPOrderList_e.remove(ordercode);
								tran._orderListCount--;
							}
						} else {
							tran._VIPOrderList_t.remove(ordercode);
							tran._orderListCount--;
						}

					}

					// 回复PHP，抢单成功
					ret = SendToPHP(obj, "OK");

				} else if (trantype.equals("E")) {
					if (tran._VIPOrderList_e.containsKey(ordercode)) {
						if (isSelected) {
							tran._myOrders.put(ordercode,
									tran._VIPOrderList_e.remove(ordercode));
							tran._myOrderListCount++;
							tran._orderListCount--;
							if (tran._VIPOrderList_t.containsKey(ordercode)) {
								tran._VIPOrderList_t.remove(ordercode);
								tran._orderListCount--;
							}
						} else {
							tran._VIPOrderList_e.remove(ordercode);
							tran._orderListCount--;
						}

					}

					// 回复PHP，抢单成功
					ret = SendToPHP(obj, "OK");
					// ret = Const.SUCCESS;
				} else {
					ret = SendToPHP(obj, "FAILED");
				}
				break;
			}
			case NORMAL: {
				if (tran._normalOrderList.containsKey(ordercode)) {
					if (isSelected) {
						tran._myOrders.put(ordercode,
								tran._normalOrderList.remove(ordercode));
						tran._myOrderListCount++;
					} else {
						tran._normalOrderList.remove(ordercode);
					}
					tran._orderListCount--;

					// 回复PHP，抢单成功
					ret = SendToPHP(obj, "OK");
					// ret = Const.SUCCESS;
				}
				break;
			}
			default:
				ret = SendToPHP(obj, "FAILED");
				break;
			}

			if (isSelected) {
				if (tran._myOrderListCount >= this._maxOrderListSize) {
					tran._orderListCount = 0;
					tran._normalOrderList.clear();
					tran._VIPOrderList_t.clear();
					tran._VIPOrderList_e.clear();

				}
			}
		} else {
			Log4j.error("TranslatorBolt don't exist the translator " + uid);
		}

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		String uid = tuple.getStringByField("uid");
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);
		String type = Util.GetStringFromJSon("type", obj);
		boolean isSend = true;

		Log4j.debug("TranslatorBolt uid:" + uid + " json:" + obj.toString());

		if (uid == null) {
			Log4j.log("translatorbolt uid is null");
			SendToPHP(obj, "FAILED");
		} else {

			// 在这里uid为-1代表该uid未登录，再matchingbolt中已经校验
			if (0 != uid.compareTo("-1")) {
				switch (aid) {
				case "user": {
					// recom/user
					switch (method) {
					case "POST": {
						// 译员登入
						Login(uid, obj);
						break;
					}
					case "GET": {
						// 译员获取最新抢单列表
						OnlineUsers(obj);
						isSend = false;
						break;
					}
					case "DELETE": {
						// 译员登出
						Logout(uid, obj);
						break;
					}
					default:
						break;
					}

					break;
				}
				case "order": {
					// recom/order
					switch (method) {
					case "POST": {
						// 新抢单订单
						Order(uid, obj);
						break;
					}
					case "PUT": {
						// 判断译员是否已经抢单数量超过最大数
						if (RequireOrder(uid, obj) != Const.SUCCESS) {
							isSend = false;
						}
						break;
					}
					case "DELETE": {
						// 删除该译员下的该订单
						DeleteOrder(uid, obj);
						break;
					}
					case "GET": {
						// 译员获取最新抢单列表
						GetOrder(uid, obj);
						break;
					}
					case "SYN": {
						// 抢单成功后执行，用来删除译员身上的已经被抢普通订单
						switch (type) {
						case "normal": {
							// 译员抢单成功，取消uid译员中该订单信息
							SynTrans(uid, obj, TYPE.NORMAL);
							isSend = false;
							break;
						}
						// 抢单成功后执行，用来删除译员身上的已经被抢TE订单
						case "vip": {
							// 译员抢单成功，取消uid译员中该订单信息
							SynTrans(uid, obj, TYPE.VIP);
							isSend = false;
							break;
						}
						default:
							break;
						}
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

			// 发送Storm消息到下一个Bolt
			String ordercode = Util.GetStringFromJSon("ordercode", obj);
			if (isSend && (ordercode != null)) {
				collector.emit(new Values(ordercode, content));
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("ordercode", "content"));
	}

}
