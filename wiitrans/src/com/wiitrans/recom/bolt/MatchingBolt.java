package com.wiitrans.recom.bolt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

//以语言对为Field存储当前测试通过的译员，及该译员可以申请或抢的订单
public class MatchingBolt extends BaseBasicBolt {
	// private TaskReportor _reportor = null;
	// 语言对，译员组
	private HashMap<String, HashSet<String>> _transGroup = new HashMap<String, HashSet<String>>();

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
	}

	private int Login(String langpair, JSONObject obj, String uid) {
		int ret = Const.FAIL;
		JSONObject langpair_industry = Util.GetJSonFromJSon(
				"langpair_industry", obj);

		if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
			String industry_ids = Util.GetStringFromJSon(langpair,
					langpair_industry);
			if (industry_ids != null) {
				for (String industry_id : industry_ids.split("-")) {
					Login_match(langpair + "_" + industry_id, obj, uid);
				}
			}
		} else {
			Login_match(langpair, obj, uid);
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int Login_match(String match, JSONObject obj, String uid) {
		int ret = Const.FAIL;

		HashSet<String> transHashSet = null;
		if (!_transGroup.containsKey(match)) {
			// 添加新对应语言对到语言对组
			transHashSet = new HashSet<String>();
			_transGroup.put(match, transHashSet);
		} else {
			transHashSet = _transGroup.get(match);
		}
		// 添加新登入译员到对应语言对译员组
		transHashSet.add(uid);

		ret = Const.SUCCESS;

		return ret;
	}

	private int Logout(String langpair, JSONObject obj, String uid) {
		int ret = Const.FAIL;
		JSONObject langpair_industry = Util.GetJSonFromJSon(
				"langpair_industry", obj);
		if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
			String industry_ids = Util.GetStringFromJSon(langpair,
					langpair_industry);
			if (industry_ids != null) {
				for (String industry_id : industry_ids.split("-")) {
					Logout_match(langpair + "_" + industry_id, obj, uid);
				}
			}
		} else {
			Logout_match(langpair, obj, uid);
		}
		return ret;
	}

	private int Logout_match(String match, JSONObject obj, String uid) {
		int ret = Const.FAIL;

		// 从语言对译员组里删除译员信息
		if (_transGroup.containsKey(match)) {
			HashSet<String> trans = _transGroup.get(match);

			if (trans.contains(uid)) {
				trans.remove(uid);
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int Order(String langpair, JSONObject obj, String content,
			BasicOutputCollector collector) {
		int ret = Const.FAIL;
		int match_type = Util.GetIntFromJSon("match_type", obj);
		// 新抢单订单
		if (match_type >= 4 && match_type <= 6) {

			String recom_t = Util.GetStringFromJSon("recom_t", obj);
			if (recom_t != null && recom_t.trim().length() > 0) {
				String[] uids = recom_t.trim().split(",");
				if (uids != null && uids.length > 0) {
					for (String uidrecom : uids) {
						collector.emit(new Values(uidrecom, content));
					}
				}
			}
			String recom_e = Util.GetStringFromJSon("recom_e", obj);
			if (recom_e != null && recom_e.trim().length() > 0) {
				String[] uids = recom_e.trim().split(",");
				if (uids != null && uids.length > 0) {
					for (String uidrecom : uids) {
						collector.emit(new Values(uidrecom, content));
					}
				}
			}

		} else if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
			String industry_id = Util.GetStringFromJSon("industry_id", obj);
			Order_match(langpair + "_" + industry_id, obj, content, collector);
		} else {
			Order_match(langpair, obj, content, collector);
		}
		return ret;
	}

	private int Order_match(String match, JSONObject obj, String content,
			BasicOutputCollector collector) {
		int ret = Const.FAIL;

		// 发送给符合语言对的译员Bolt
		// 目前没有特殊算法，只是发给当前语言对包括的译员，未来考虑根据喜好和历史信誉度等进行译员匹配
		if (_transGroup.containsKey(match)) {
			HashSet<String> trans = _transGroup.get(match);

			if (!trans.isEmpty()) {
				// 发送给匹配成功的译员Bolt进行处理
				for (String uid : trans) {
					collector.emit(new Values(uid, content));
				}

				ret = Const.SUCCESS;

			} else {
				Log4j.warn("The login translator of this match " + match
						+ " is empty.");
			}
		} else {
			Log4j.warn("Don't have this match " + match + " in TransBolt.");
		}

		return ret;
	}

	private int SynOrder(String match, JSONObject obj, String content,
			BasicOutputCollector collector) {
		int ret = Const.FAIL;

		// 发送给符合语言对的译员Bolt
		// 目前没有特殊算法，只是发给当前语言对包括的译员，未来考虑根据喜好和历史信誉度等进行译员匹配
		if (_transGroup.containsKey(match)) {
			HashSet<String> trans = _transGroup.get(match);

			if (!trans.isEmpty()) {
				// 发送给匹配成功的译员Bolt进行处理
				for (String uid : trans) {
					collector.emit(new Values(uid, content));
				}

				ret = Const.SUCCESS;

			} else {
				Log4j.warn("The login translator of this match " + match
						+ " is empty.");
			}
		} else {
			Log4j.warn("Don't have this match " + match + " in TransBolt.");
		}

		return ret;
	}

	private int DeleteOrder(String langpair, JSONObject obj, String content,
			BasicOutputCollector collector) {
		int ret = Const.FAIL;
		int match_type = Util.GetIntFromJSon("match_type", obj);
		if (match_type >= 4 && match_type <= 6) {
			String delrecom = Util.GetStringFromJSon("delrecom", obj);
			if (delrecom != null && delrecom.trim().length() > 0) {
				String[] uids = delrecom.trim().split(",");
				if (uids != null && uids.length > 0) {
					for (String uidrecom : uids) {
						collector.emit(new Values(uidrecom, content));
					}
				}
			}

		} else if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
			String industry_id = Util.GetStringFromJSon("industry_id", obj);
			DeleteOrder_match(langpair + "_" + industry_id, obj, content,
					collector);
		} else {
			DeleteOrder_match(langpair, obj, content, collector);
		}

		return ret;
	}

	private int DeleteOrder_match(String match, JSONObject obj, String content,
			BasicOutputCollector collector) {
		int ret = Const.FAIL;

		// String complete = Util.GetStringFromJSon("complete", obj);
		String uid = Util.GetStringFromJSon("uid", obj);
		if (_transGroup.containsKey(match)) {
			HashSet<String> trans = _transGroup.get(match);

			if (!trans.isEmpty()) {

				if (trans.contains(uid)) {
					collector.emit(new Values(uid, content));
				}

				ret = Const.SUCCESS;

			}
		}

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		String langpair = tuple.getStringByField("langpair");
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		// String sid = Util.GetStringFromJSon("sid", obj);
		String uid = Util.GetStringFromJSon("uid", obj);
		// String nid = Util.GetStringFromJSon("nid", obj);
		String method = Util.GetStringFromJSon("method", obj);

		Log4j.debug("matchingbolt langpair:" + langpair + " json:"
				+ obj.toString());

		switch (aid) {
		case "user": {
			// recom/user
			switch (method) {
			case "POST": {
				// 译员登入
				Login(langpair, obj, uid);
				break;
			}
			case "DELETE": {
				// 译员登出
				Logout(langpair, obj, uid);
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
				Order(langpair, obj, content, collector);
				uid = "-1";
				break;

			}
			case "SYN": {
				if (BundleConf.BUNDLE_MATCH_INDUSTRY) {
					String industry_id = Util.GetStringFromJSon("industry_id",
							obj);
					SynOrder(langpair + "_" + industry_id, obj, content,
							collector);
				} else {
					SynOrder(langpair, obj, content, collector);
				}
				uid = "-1";
				break;
			}
			case "DELETE": {
				DeleteOrder(langpair, obj, content, collector);
				// uid = "-1";
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
		collector.emit(new Values(uid, content));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("uid", "content"));
	}

}
