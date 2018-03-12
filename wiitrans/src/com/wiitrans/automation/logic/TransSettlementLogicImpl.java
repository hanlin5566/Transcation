package com.wiitrans.automation.logic;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.db.MessageDAO;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.ProcTransSettlementDAO;
import com.wiitrans.base.db.TranslatorDAO;
import com.wiitrans.base.db.TranslatorFinanceDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.TranslatorBean;
import com.wiitrans.base.db.model.TranslatorFinanceBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;

public class TransSettlementLogicImpl implements Logic {

	// public List<String> _targetURL;
	// public String _url;

	public TransSettlementLogicImpl() {
		Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
		ConfigNode node;
		for (Integer node_id : set) {
			node = BundleConf.BUNDLE_Node.get(node_id);
			// if (node != null) {
			// Log4j.info("node_id:" + node_id);
			// Log4j.info("mybatis:" + node.mybatis);
			// Log4j.info("api:" + node.api + "automation/newtask/");
			// Log4j.info("node_id_equals:"
			// + (node_id == BundleConf.DEFAULT_NID ? "true" : "false"));
			// }
		}
	}

	@Override
	public void invoke(JSONObject jsonObject) throws Exception {
		// 同步房间信息
		Log4j.log("start invoke TransSettlementLogicImpl");

		// 本节点操作
		this.Settlement(jsonObject);

		// 同步其他节点

		JSONObject newJSON = new JSONObject(jsonObject.toString());
		JSONObject paramJson = newJSON.getJSONObject("param");
		int nid = Util.GetIntFromJSon("nid", paramJson);
		if (nid == BundleConf.DEFAULT_NID) {
			newJSON.remove("id");// 移除ID，不沿用此连接，让其重新生成。
			String synctype = Util.GetStringFromJSon("synctype", paramJson);
			paramJson.put("synctype", "other");
			if (synctype != null && synctype.equalsIgnoreCase("local")) {
				Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
				for (Integer node_id : set) {
					if (node_id > 0 && node_id != BundleConf.DEFAULT_NID) {
						ConfigNode node = BundleConf.BUNDLE_Node.get(node_id);
						if (node != null && node.api != null
								&& node.api.trim().length() > 0) {
							Log4j.info("TransSettlementLogicImpl invoke"
									+ " node_id:" + node_id + " nid:" + nid
									+ " mybatis:" + node.mybatis + " api:"
									+ node.api + "automation/newtask/");
							// Log4j.info("node_id:" + node_id);
							// Log4j.info("nid:" + nid);
							// Log4j.info("mybatis:" + node.mybatis);
							// Log4j.info("api:" + node.api
							// + "automation/newtask/");
							// Log4j.info("node_id_equals:"
							// + (node_id == BundleConf.DEFAULT_NID ? "true"
							// : "false"));
							newJSON.put("nid", String.valueOf(nid));
							new HttpSimulator(node.api + "automation/newtask/")
									.executeMethodTimeOut(newJSON.toString(), 2);
						}
					}
				}
			}
		}
		Log4j.log("end invoke TransSettlementLogicImpl");
	}

	private void Settlement(JSONObject jsonObject) throws Exception {
		Log4j.info("TransSettlementLogicImpl Settlement json:" + jsonObject);
		int o_error = 100;
		ProcTransSettlementDAO procTransSettlementdao = null;
		OrderDAO orderdao = null;
		TranslatorFinanceDAO tfdao_t = null;
		TranslatorFinanceDAO tfdao_e = null;
		MessageDAO msgdao_t = null;
		MessageDAO msgdao_e = null;
		try {
			JSONObject paramJson = jsonObject.getJSONObject("param");
			int nid = Util.GetIntFromJSon("nid", paramJson);
			String code = Util.GetStringFromJSon("code", paramJson);

			orderdao = new OrderDAO();

			orderdao.Init(true);
			OrderBean order = null;
			if (nid == BundleConf.DEFAULT_NID) {
				orderdao.UpdateStatus(order);
				orderdao.Commit();
				order = orderdao.Select(code);
			} else {
				orderdao.UpdateStatusForNode(order);
				orderdao.Commit();
				order = orderdao.SelectForNode(code);
			}
			if (order != null && order.status == 40) {

				procTransSettlementdao = new ProcTransSettlementDAO();

				procTransSettlementdao.Init(true);
				HashMap map = new HashMap<String, Object>();
				map.put("p_node_id", nid);
				map.put("p_code", code);
				map.put("o_error", o_error);
				procTransSettlementdao.Settlement(map);
				procTransSettlementdao.UnInit();

				if (map.containsKey("o_error")) {
					o_error = (Integer) map.get("o_error");
					Log4j.info("TransSettlementLogicImpl Settlement pr o_error:"
							+ o_error);
					if (o_error > 0) {
						Log4j.error("proc error:o_error = " + o_error);
						throw new Exception(
								"TransSettle proc error! o_error = " + o_error);
					} else {

						order = new OrderBean();
						order.code = code;
						order.status = 50;

						if (nid == BundleConf.DEFAULT_NID) {
							orderdao.UpdateStatus(order);
							orderdao.Commit();
							order = orderdao.Select(code);
						} else {
							orderdao.UpdateStatusForNode(order);
							orderdao.Commit();
							order = orderdao.SelectForNode(code);
						}
						orderdao.UnInit();

						if (order != null) {
							ConfigNode node = BundleConf.BUNDLE_Node
									.get(BundleConf.DEFAULT_NID);
							String url = node.api + "msg/sysMsgCount/";
							if (order.tnid == BundleConf.DEFAULT_NID) {
								TranslatorFinanceBean tfbean = new TranslatorFinanceBean();

								tfbean.translator_id = order.translator_id;
								tfbean.type = true;
								tfbean.currency_id = order.currency_id;
								tfbean.income = order.actual_money_t;
								tfbean.expense = 0;
								tfbean.account = "";
								if (order.tnid == 1) {
									tfbean.remark = "订单：" + order.code + "翻译费";
								} else {
									tfbean.remark = "Order: " + order.code
											+ " Translation Fee";
								}
								tfbean.create_time = Util.GetIntFromNow();
								tfdao_t = new TranslatorFinanceDAO();
								tfdao_t.Init(false);
								tfdao_t.Insert(tfbean);
								tfdao_t.Commit();
								tfdao_t.UnInit();

								// MessageUserBean msg = new MessageUserBean();
								// msg.user_id = order.translator_id;
								// msg.message = GetMsg(order, order.tnid, true,
								// order.translator_id);
								// msgdao_t = new MessageDAO();
								// msgdao_t.Init(false);
								// msgdao_t.Insert(msg);
								// msgdao_t.Commit();
								// msgdao_t.UnInit();

								JSONObject object = new JSONObject();
								object.put("sid", Util.GetStringFromJSon("sid",
										paramJson));
								object.put("uid",
										String.valueOf(order.translator_id));
								object.put("senduid",
										String.valueOf(order.translator_id));
								object.put("aid", "sysMsgCount");
								// String url = _url + "msg/sysMsgCount/";
								object.put("nid", String.valueOf(nid));
								object.put("tnid", String.valueOf(nid));
								object.put("datetype", String.valueOf(3));
								object.put(
										"message",
										GetMsg(order, order.tnid, true,
												order.translator_id));
								new HttpSimulator(url).executeMethodTimeOut(
										object.toString(), 2);

							}

							if (order.enid == BundleConf.DEFAULT_NID) {
								TranslatorFinanceBean tfbean = new TranslatorFinanceBean();

								tfbean.translator_id = order.editor_id;
								tfbean.type = true;
								tfbean.currency_id = order.currency_id;
								tfbean.income = order.actual_money_e;
								tfbean.expense = 0;
								tfbean.account = "";
								if (order.enid == 1) {
									tfbean.remark = "订单：" + order.code + "翻译费";
								} else {
									tfbean.remark = "Order: " + order.code
											+ " Translation Fee";
								}
								tfbean.create_time = Util.GetIntFromNow();
								tfdao_e = new TranslatorFinanceDAO();
								tfdao_e.Init(false);
								tfdao_e.Insert(tfbean);
								tfdao_e.Commit();
								tfdao_e.UnInit();

								// MessageUserBean msg = new MessageUserBean();
								// msg.user_id = order.editor_id;
								// msg.message = GetMsg(order, order.enid,
								// false,
								// order.editor_id);
								// msgdao_e = new MessageDAO();
								// msgdao_e.Init(false);
								// msgdao_e.Insert(msg);
								// msgdao_e.Commit();
								// msgdao_e.UnInit();

								JSONObject object = new JSONObject();
								object.put("sid", Util.GetStringFromJSon("sid",
										paramJson));
								object.put("uid",
										String.valueOf(order.editor_id));
								object.put("senduid",
										String.valueOf(order.editor_id));
								object.put("aid", "sysMsgCount");
								// String url = _url + "msg/sysMsgCount/";
								object.put("nid", String.valueOf(nid));
								object.put("tnid", String.valueOf(nid));
								object.put("datetype", String.valueOf(3));
								object.put(
										"message",
										GetMsg(order, order.enid, false,
												order.editor_id));
								new HttpSimulator(url).executeMethodTimeOut(
										object.toString(), 2);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (procTransSettlementdao != null) {
				procTransSettlementdao.UnInit();
			}
			if (orderdao != null) {
				orderdao.UnInit();
			}
			if (tfdao_t != null) {
				tfdao_t.UnInit();
			}
			if (tfdao_e != null) {
				tfdao_e.UnInit();
			}
			if (msgdao_t != null) {
				msgdao_t.UnInit();
			}
			if (msgdao_e != null) {
				msgdao_e.UnInit();
			}
		}
	}

	public String GetMsg(OrderBean order, int nid, boolean isTranslator,
			int transID) {
		TranslatorDAO transdao = null;
		try {
			transdao = new TranslatorDAO();
			transdao.Init(false);
			TranslatorBean bean = transdao.Select(transID);
			transdao.UnInit();
			StringBuffer msg;
			if (bean != null) {
				DecimalFormat df = new DecimalFormat("0.00");
				df.setRoundingMode(RoundingMode.FLOOR);
				if (nid == 1) {
					msg = new StringBuffer();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
					msg.append("编号为 ").append(order.code).append(" 的订单薪酬");
					if (order.currency_id == 1) {
						msg.append("¥");
					} else if (order.currency_id == 2) {
						msg.append("$");
					} else if (order.currency_id == 3) {
						msg.append("€");
					}
					if (isTranslator) {
						msg.append(df.format(order.actual_money_t));
					} else {
						msg.append(df.format(order.actual_money_e));
					}
					msg.append("已于")
							// .append(sdf
							// .format(Calendar.getInstance().getTime()))
							.append("%s")
							.append("转入<a style=\"color:#069;\" href=\"/purse\">您的钱包</a>，钱包金额为");
					if (bean.aggregate_money_usd > 0) {
						msg.append("$").append(df.format(bean.money_usd))
								.append("/");
					}
					if (bean.aggregate_money > 0) {
						msg.append("¥").append(df.format(bean.money))
								.append("/");
					}
					if (bean.aggregate_money_eur > 0) {
						msg.append("€").append(df.format(bean.money_eur))
								.append("/");
					}
					if (msg.charAt(msg.length() - 1) == '/') {
						msg.deleteCharAt(msg.length() - 1);
					}

					msg.append("。");
				} else {
					msg = new StringBuffer();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
					msg.append("The order number ").append(order.code)
							.append(" with payment amount ");
					if (order.currency_id == 1) {
						msg.append("¥");
					} else if (order.currency_id == 2) {
						msg.append("$");
					} else if (order.currency_id == 3) {
						msg.append("€");
					}
					if (isTranslator) {
						msg.append(df.format(order.actual_money_t));
					} else {
						msg.append(df.format(order.actual_money_e));
					}
					msg.append(" has been transferred on ")
							// .append(sdf
							// .format(Calendar.getInstance().getTime()))
							.append("%s")
							.append(" to <a style=\"color:#069;\" href=\"/purse\">your wallet</a>, the current balance is ");
					if (bean.aggregate_money_usd > 0) {
						msg.append("$").append(df.format(bean.money_usd))
								.append("/");
					}
					if (bean.aggregate_money > 0) {
						msg.append("¥").append(df.format(bean.money))
								.append("/");
					}
					if (bean.aggregate_money_eur > 0) {
						msg.append("€").append(df.format(bean.money_eur))
								.append("/");
					}
					if (msg.charAt(msg.length() - 1) == '/') {
						msg.deleteCharAt(msg.length() - 1);
					}

					msg.append(". ");
				}
				return msg.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
			// return null;
		} finally {
			if (transdao != null) {
				transdao.UnInit();
			}
		}

	}
}
