package com.wiitrans.term.bolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.DictIndustryDAO;
import com.wiitrans.base.db.model.DictIndustryBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.term.Term;
import com.wiitrans.base.term.TermMeta;
import com.wiitrans.base.term.UpdateTerm;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

//Redis key cterm_语言对ID_术语名称  例如： cterm_1_中华 代表： 译员贡献术语_英中_术语名称为中华
//value 中存放的是JSON串，定义如下
//{'领域1':{
//'贡献译员uid1':{'termid':'112','解释':'china','用法':'i am china','备注':'comment','点赞译员uid':'1|12|33','点反对译员uid':'11|112|313'},
//'贡献译员uid2':{'termid':'112','解释':'china','用法':'i am china','备注':'comment','agree':{"uid1":"1","uid2":"-1","uid3":"1"}}
//}, 
//'领域2':... }
//"terms":{"互联网游戏":{"2":{"remark":"","usage":"","meaning":"考虑，琢磨","agree":{"3":"1"}}}}
public class PersistenceBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;
	private TermPersistence _persistence = null;
	private ICache _cache = null;
	private HashMap<String, String> _industryMap;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
//		AppConfig app = new AppConfig();
//		app.Parse();
		WiitransConfig.getInstance(0);

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.TERM_BUNDLE_PORT);

		_reportor.Start();

		if (_industryMap == null) {
			DictIndustryDAO dictindustrydao = null;
			try {
				dictindustrydao = new DictIndustryDAO();
				dictindustrydao.Init(true);
				List<DictIndustryBean> list = dictindustrydao.SelectAll();
				dictindustrydao.UnInit();
				_industryMap = new HashMap<String, String>();
				for (DictIndustryBean dictIndustryBean : list) {
					_industryMap.put(
							String.valueOf(dictIndustryBean.industry_id),
							dictIndustryBean.industry_name);
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dictindustrydao != null) {
					dictindustrydao.UnInit();
				}
			}

		}

		if (_cache == null) {
			_cache = new RedisCache();
			_cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}
		_persistence = new TermPersistence();
		_persistence.start();
	}

	private int SetAgree(JSONObject obj) {
		int result = Const.FAIL;
		String pair_id = Util.GetStringFromJSon("pair_id", obj);
		String industry_id = Util.GetStringFromJSon("industry_id", obj);
		String industry_name = null;
		if (industry_id == null) {
			industry_name = Util.GetStringFromJSon("industry_name", obj);
		} else {
			if (_industryMap.containsKey(industry_id)) {
				industry_name = _industryMap.get(industry_id);
			}
		}
		// String aid = Util.GetStringFromJSon("aid", obj);
		String term = Util.GetStringFromJSon("term", obj);
		term = term.toLowerCase();
		String termuid = Util.GetStringFromJSon("termuid", obj);
		String uid = Util.GetStringFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		String agree = Util.GetStringFromJSon("agree", obj);
		if (term != null && termuid != null && agree != null) {
			String term_cache_key = this.GetCacheKey(Util.String2Int(pair_id),
					term);
			try {

				if (industry_name != null) {
					String sterms = _cache.GetString(nid, term_cache_key);
					JSONObject termsJSON = null;
					if (sterms != null) {
						termsJSON = new JSONObject(sterms);

						if (termsJSON != null) {

							JSONObject industryTerms = Util.GetJSonFromJSon(
									industry_name, termsJSON);

							if (industryTerms != null) {

								JSONObject termJSON = Util.GetJSonFromJSon(
										termuid, industryTerms);
								if (termJSON != null) {
									JSONObject agreeObj = Util.GetJSonFromJSon(
											"agree", termJSON);
									if (agreeObj == null) {
										agreeObj = new JSONObject();
									}
									if (agree.equals("0")) {
										agreeObj.remove(uid);
									} else if (agree.equals("1")
											|| agree.equals("-1")) {
										agreeObj.put(uid, String.valueOf(agree));
									}

									termJSON.put("agree", agreeObj);

									_cache.SetString(nid, term_cache_key,
											termsJSON.toString());

									UpdateTerm updateTerm = new UpdateTerm();

									updateTerm._pairid = Util
											.String2Int(pair_id);
									updateTerm._industryId = Util
											.String2Int(industry_id);
									updateTerm._term = term; // 原文
									updateTerm._tid = Util.String2Int(uid); // 评价译员ID
									updateTerm._uid = Util.String2Int(termuid); // 创建译员ID
									updateTerm.agree = Util.String2Int(agree); // 点赞/反对

									_persistence.PushEvaTerm(updateTerm);

									result = Const.SUCCESS;

								} else {
									Log4j.log("术语(" + term + ")在industry("
											+ industry_name + ")中不存在译员("
											+ termuid + ")的贡献术语");
								}
							} else {
								Log4j.log("术语(" + term + ")在industry("
										+ industry_name + ")中不存在");
							}
						}
					} else {
						Log4j.log("在缓存中key为n" + nid + "_" + term_cache_key
								+ "不存在");
					}
				} else {
					Log4j.error("industry_name 不能为空");
				}
			} catch (Exception e) {
				Log4j.error(e);
			}
		}
		return result;
	}

	private int SetRedis(JSONObject obj) {
		int result = Const.FAIL;

		String pair_id = Util.GetStringFromJSon("pair_id", obj);
		String industry_id = Util.GetStringFromJSon("industry_id", obj);
		String industry_name = null;
		if (_industryMap.containsKey(industry_id)) {
			industry_name = _industryMap.get(industry_id);
		}
		// String aid = Util.GetStringFromJSon("aid", obj);
		String term = Util.GetStringFromJSon("term", obj);
		term = term.toLowerCase();
		String uid = Util.GetStringFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);

		String term_cache_key = this
				.GetCacheKey(Util.String2Int(pair_id), term);
		try {

			if (industry_name != null) {
				String sterms = _cache.GetString(nid, term_cache_key);
				JSONObject termsJSON = null;
				if (sterms != null) {
					termsJSON = new JSONObject(sterms);
				}

				if (termsJSON == null) {
					termsJSON = new JSONObject();
				}
				JSONObject industryTerms = Util.GetJSonFromJSon(industry_name,
						termsJSON);

				if (industryTerms == null) {
					industryTerms = new JSONObject();
					industryTerms.put("industry_id", industry_id);
					termsJSON.put(industry_name, industryTerms);
				}
				JSONObject termJSON = Util.GetJSonFromJSon(uid, industryTerms);
				if (termJSON == null) {
					termJSON = new JSONObject();
					industryTerms.put(uid, termJSON);

					String meaning = Util.GetStringFromJSon("meaning", obj);
					termJSON.put("meaning", meaning);
					String usage = Util.GetStringFromJSon("usage", obj);
					if (usage == null) {
						usage = "";
					}
					termJSON.put("usage", usage);
					String remark = Util.GetStringFromJSon("remark", obj);
					if (remark == null) {
						remark = "";
					}
					termJSON.put("remark", remark);

					_cache.SetString(nid, term_cache_key, termsJSON.toString());

					Term newTerm = new Term();
					newTerm._pair_id = Util.String2Int(pair_id);
					newTerm._industryId = Util.String2Int(industry_id);
					newTerm._term = term;
					newTerm._meta = new ArrayList<TermMeta>();

					TermMeta meta = new TermMeta();
					meta._contributorUid = Util.String2Int(uid);
					meta._meaning = meaning;
					meta._usage = usage;
					meta._remark = remark;
					newTerm._meta.add(meta);
					_persistence.PushNewTerm(newTerm);

					result = Const.SUCCESS;
				} else {
					Log4j.log("术语(" + term + ")在industry(" + industry_name
							+ ")中已存在译员(" + uid + ")的贡献术语");
				}
			} else {
				Log4j.error("industry_name 不能为空");
			}
		} catch (Exception e) {

		}
		return result;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		// 收到消息（新增译员贡献术语，译员贡献术语评价）
		// 1.同步更新Redis中的译员贡献术语库
		String task = tuple.getStringByField("task");
		JSONObject obj = new JSONObject(task);
		String aid = Util.GetStringFromJSon("aid", obj);

		Log4j.log("persistencebolt " + obj.toString());

		int result = Const.FAIL;

		switch (aid) {
		case "setterm": {
			// 预处理
			result = this.SetRedis(obj);
			break;
		}
		case "setagree": {
			// 设置赞同反对
			result = SetAgree(obj);
			break;
		}
		default:
			break;
		}

		// 2.将同步数据传递给_persistence
		// Term newTerm = null;
		// _persistence.PushNewTerm(newTerm);
		// UpdateTerm updateTerm = null;
		// _persistence.PushEvaTerm(updateTerm);

		JSONObject resObj = new JSONObject();

		if (Const.SUCCESS == result) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
		_reportor.Report(resObj);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("task"));
	}

	private String GetCacheKey(int pair_id, String term) {
		return String.format("cterm_%d_%s", pair_id, term);
	}
}
