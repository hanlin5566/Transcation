package com.wiitrans.analysis.bolt;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.DictFactorDAO;
import com.wiitrans.base.db.DictRateDAO;
import com.wiitrans.base.db.model.DictFactorBean;
import com.wiitrans.base.db.model.DictRateBean;
import com.wiitrans.base.db.model.DictRateCurrencyBean;
import com.wiitrans.base.db.model.RateCurrencyBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.order.OrderExpectedTime;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.WiitransConfig;

public class CalculateMoneyBolt extends BaseBasicBolt {
	private HashMap<Integer, DictRateBean[]> _maptime = null;
	private HashMap<Integer, DictRateCurrencyBean[]> _mapcny = null;
	private HashMap<Integer, DictRateCurrencyBean[]> _mapusd = null;
	private HashMap<Integer, DictRateCurrencyBean[]> _mapeur = null;
	private TaskReportor _reportor = null;
	private DictFactorBean _factor = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		WiitransConfig.getInstance(0);

		DictRateDAO ratedao = null;
		try {
			ratedao = new DictRateDAO();
			ratedao.Init(true);
			List<DictRateBean> list = ratedao.SelectAll();
			List<DictRateCurrencyBean> listcny = ratedao.SelectCNY();
			List<DictRateCurrencyBean> listusd = ratedao.SelectUSD();
			List<DictRateCurrencyBean> listeur = ratedao.SelectEUR();
			ratedao.UnInit();
			_maptime = new HashMap<Integer, DictRateBean[]>();
			_mapcny = new HashMap<Integer, DictRateCurrencyBean[]>();
			_mapusd = new HashMap<Integer, DictRateCurrencyBean[]>();
			_mapeur = new HashMap<Integer, DictRateCurrencyBean[]>();

			DictRateBean[] rates;
			for (DictRateBean bean : list) {
				if (!_maptime.containsKey(bean.pair_id)) {
					_maptime.put(bean.pair_id, new DictRateBean[3]);
				}

				rates = _maptime.get(bean.pair_id);
				if (bean.price_level_id >= 1 && bean.price_level_id <= 3) {
					rates[bean.price_level_id - 1] = bean;
				}
			}

			DictRateCurrencyBean[] currencyrates;
			for (DictRateCurrencyBean bean : listcny) {
				if (!_mapcny.containsKey(bean.pair_id)) {
					_mapcny.put(bean.pair_id, new DictRateCurrencyBean[3]);
				}

				currencyrates = _mapcny.get(bean.pair_id);
				if (bean.price_level_id >= 1 && bean.price_level_id <= 3) {
					currencyrates[bean.price_level_id - 1] = bean;
				}
			}

			for (DictRateCurrencyBean bean : listusd) {
				if (!_mapusd.containsKey(bean.pair_id)) {
					_mapusd.put(bean.pair_id, new DictRateCurrencyBean[3]);
				}

				currencyrates = _mapusd.get(bean.pair_id);
				if (bean.price_level_id >= 1 && bean.price_level_id <= 3) {
					currencyrates[bean.price_level_id - 1] = bean;
				}
			}

			for (DictRateCurrencyBean bean : listeur) {
				if (!_mapeur.containsKey(bean.pair_id)) {
					_mapeur.put(bean.pair_id, new DictRateCurrencyBean[3]);
				}

				currencyrates = _mapeur.get(bean.pair_id);
				if (bean.price_level_id >= 1 && bean.price_level_id <= 3) {
					currencyrates[bean.price_level_id - 1] = bean;
				}
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (ratedao != null) {
				ratedao.UnInit();
			}
		}

		if (_factor == null) {
			DictFactorDAO factordao = null;
			try {
				factordao = new DictFactorDAO();
				factordao.Init(true);
				_factor = factordao.Select();
				factordao.UnInit();
			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			} finally {
				if (factordao != null) {
					factordao.UnInit();
				}
			}
		}

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.ANALYSIS_BUNDLE_PORT);
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

	public JSONObject CalculatePairMoney_Custo(JSONObject task,
			RateCurrencyBean rate, int wordcount, float factor) {
		JSONObject obj = new JSONObject();

		JSONObject first = new JSONObject();
		JSONObject second = new JSONObject();
		JSONObject third = new JSONObject();

		double trans;
		double serve;
		double expedite;
		double expeditedserve;

		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);

		trans = rate.rate_first * wordcount / 1000.0;
		serve = trans * _factor.customer_service_fee;
		expedite = trans * _factor.expedited_fee;
		expeditedserve = (trans + expedite) * _factor.customer_service_fee;
		first.put("trans", df.format(trans * factor));
		first.put("serve", df.format(serve * factor));
		first.put("expedite", df.format(expedite * factor));
		first.put("expeditedserve", df.format(expeditedserve * factor));

		trans = rate.rate_second * wordcount / 1000.0;
		serve = trans * _factor.customer_service_fee;
		expedite = trans * _factor.expedited_fee;
		expeditedserve = (trans + expedite) * _factor.customer_service_fee;
		second.put("trans", df.format(trans * factor));
		second.put("serve", df.format(serve * factor));
		second.put("expedite", df.format(expedite * factor));
		second.put("expeditedserve", df.format(expeditedserve * factor));

		trans = rate.rate_third * wordcount / 1000.0;
		serve = trans * _factor.customer_service_fee;
		expedite = trans * _factor.expedited_fee;
		expeditedserve = (trans + expedite) * _factor.customer_service_fee;
		third.put("trans", df.format(trans * factor));
		third.put("serve", df.format(serve * factor));
		third.put("expedite", df.format(expedite * factor));
		third.put("expeditedserve", df.format(expeditedserve * factor));

		obj.put("first", first);
		obj.put("second", second);
		obj.put("third", third);

		return obj;
	}

	public JSONObject CalculatePairMoney(JSONObject task,
			DictRateCurrencyBean[] rate, int wordcount, float factor) {
		JSONObject obj = new JSONObject();

		JSONObject first = new JSONObject();
		JSONObject second = new JSONObject();
		JSONObject third = new JSONObject();
		for (DictRateCurrencyBean dictRateCurrencyBean : rate) {
			double trans;
			double serve;
			double expedite;
			double expeditedserve;
			trans = dictRateCurrencyBean.rate
					* dictRateCurrencyBean.rate_factor * wordcount / 100000.0;
			serve = trans * _factor.customer_service_fee;
			expedite = trans * _factor.expedited_fee;
			expeditedserve = (trans + expedite) * _factor.customer_service_fee;
			DecimalFormat df = new DecimalFormat("0.00");
			df.setRoundingMode(RoundingMode.HALF_UP);
			if (dictRateCurrencyBean.price_level_id == 1) {
				first.put("trans", df.format(trans * factor));
				first.put("serve", df.format(serve * factor));
				first.put("expedite", df.format(expedite * factor));
				first.put("expeditedserve", df.format(expeditedserve * factor));
			} else if (dictRateCurrencyBean.price_level_id == 2) {
				second.put("trans", df.format(trans * factor));
				second.put("serve", df.format(serve * factor));
				second.put("expedite", df.format(expedite * factor));
				second.put("expeditedserve", df.format(expeditedserve * factor));
			} else if (dictRateCurrencyBean.price_level_id == 3) {
				third.put("trans", df.format(trans * factor));
				third.put("serve", df.format(serve * factor));
				third.put("expedite", df.format(expedite * factor));
				third.put("expeditedserve", df.format(expeditedserve * factor));
			} else {
				continue;
			}
			obj.put("first", first);
			obj.put("second", second);
			obj.put("third", third);
		}
		return obj;
	}

	public JSONObject CalculatePairTime(JSONObject task, DictRateBean[] rate,
			int wordcount) {
		JSONObject obj = new JSONObject();

		JSONObject first = new JSONObject();
		JSONObject second = new JSONObject();
		JSONObject third = new JSONObject();
		OrderExpectedTime orderexpectedtime = new OrderExpectedTime();
		for (DictRateBean dictRateBean : rate) {
			int needtime;
			int needtime_t;
			String cutofftime;
			String cutofftime_t;
			String cutofftime_e;
			int expediteneedtime;
			int expediteneedtime_t;
			String expeditecutofftime;
			String expeditecutofftime_t;
			String expeditecutofftime_e;

			Calendar manual_start_time1 = Calendar.getInstance();
			Calendar manual_start_time2 = Calendar.getInstance();
			try {
				String s_manual_start_time = Util.GetStringFromJSon(
						"manual_start_time", task);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = sdf.parse(s_manual_start_time);
				manual_start_time1.setTime(date);
				manual_start_time2.setTime(date);
			} catch (Exception e) {
				// e.printStackTrace();
				manual_start_time1 = null;
				manual_start_time2 = null;
			}

			Calendar now1, now2;
			if (manual_start_time1 == null) {
				now1 = Calendar.getInstance();
			} else {
				now1 = (Calendar) manual_start_time1.clone();
			}
			if (manual_start_time2 == null) {
				now2 = Calendar.getInstance();
			} else {
				now2 = (Calendar) manual_start_time2.clone();
			}
			// 分钟
			if (wordcount == 0) {
				needtime = 0;
				cutofftime = "";
				cutofftime_t = "";
				cutofftime_e = "";
			} else if (dictRateBean.word_per_hour_t > 0
					&& dictRateBean.price_level_id == 1) {

				needtime = (int) Math.ceil(wordcount * 60.0
						/ dictRateBean.word_per_hour_t
						+ dictRateBean.buffer_time_t);

				cutofftime = orderexpectedtime.GetCutoffTimeForString_new(
						(Calendar) now1.clone(), needtime);
				cutofftime_t = cutofftime;
				cutofftime_e = "";
			} else if (dictRateBean.word_per_hour_t > 0
					&& dictRateBean.word_per_hour_e > 0
					&& dictRateBean.price_level_id > 1) {
				needtime = (int) Math.ceil(wordcount * 60.0
						/ dictRateBean.word_per_hour_t + wordcount * 60.0
						/ dictRateBean.word_per_hour_e
						+ dictRateBean.buffer_time_t
						+ dictRateBean.buffer_time_e);

				cutofftime = orderexpectedtime.GetCutoffTimeForString_new(
						(Calendar) now1.clone(), needtime);

				needtime_t = (int) Math.ceil(wordcount * 60.0
						/ dictRateBean.word_per_hour_t
						+ dictRateBean.buffer_time_t);

				cutofftime_t = orderexpectedtime.GetCutoffTimeForString_new(
						(Calendar) now1.clone(), needtime_t);
				cutofftime_e = cutofftime;
			} else {
				needtime = 0;

				cutofftime = "";
				cutofftime_t = "";
				cutofftime_e = "";
			}

			// 分钟
			if (wordcount == 0) {
				expediteneedtime = 0;
				// message += "wordcount=0 ";
				expeditecutofftime = "";
				expeditecutofftime_t = "";
				expeditecutofftime_e = "";
			} else if (dictRateBean.word_per_hour_t_s > 0
					&& dictRateBean.price_level_id == 1) {
				expediteneedtime = (int) Math.ceil(wordcount * 60.0
						/ dictRateBean.word_per_hour_t_s
						+ dictRateBean.buffer_time_t
						* dictRateBean.buffer_time_t_factor_s / 100);
				expeditecutofftime = orderexpectedtime
						.GetCutoffTimeForString_new((Calendar) now2.clone(),
								expediteneedtime);
				expeditecutofftime_t = expeditecutofftime;
				expeditecutofftime_e = "";
			} else if (dictRateBean.word_per_hour_t_s > 0
					&& dictRateBean.word_per_hour_e_s > 0
					&& dictRateBean.price_level_id > 1) {
				expediteneedtime = (int) Math.ceil(wordcount
						* 60.0
						/ dictRateBean.word_per_hour_t_s
						+ wordcount
						* 60.0
						/ dictRateBean.word_per_hour_e_s
						+ dictRateBean.buffer_time_t
						* dictRateBean.buffer_time_t_factor_s
						/ 100
						+ ((dictRateBean.price_level_id == 1) ? 0
								: dictRateBean.buffer_time_e
										* dictRateBean.buffer_time_e_factor_s
										/ 100));
				expeditecutofftime = orderexpectedtime
						.GetCutoffTimeForString_new((Calendar) now2.clone(),
								expediteneedtime);

				expediteneedtime_t = (int) Math.ceil(wordcount * 60.0
						/ dictRateBean.word_per_hour_t_s
						+ dictRateBean.buffer_time_t
						* dictRateBean.buffer_time_t_factor_s / 100);
				expeditecutofftime_t = orderexpectedtime
						.GetCutoffTimeForString_new((Calendar) now2.clone(),
								expediteneedtime_t);
				expeditecutofftime_e = expeditecutofftime;

			} else {
				expediteneedtime = 0;

				expeditecutofftime = "";
				expeditecutofftime_t = "";
				expeditecutofftime_e = "";
			}

			if (dictRateBean.price_level_id == 1) {
				first.put("needtime", needtime);
				first.put("cutofftime", cutofftime);
				first.put("cutofftime_t", cutofftime_t);
				first.put("cutofftime_e", cutofftime_e);
				first.put("expediteneedtime", expediteneedtime);
				first.put("expeditecutofftime", expeditecutofftime);
				first.put("expeditecutofftime_t", expeditecutofftime_t);
				first.put("expeditecutofftime_e", expeditecutofftime_e);
			} else if (dictRateBean.price_level_id == 2) {
				second.put("needtime", needtime);
				second.put("cutofftime", cutofftime);
				second.put("cutofftime_t", cutofftime_t);
				second.put("cutofftime_e", cutofftime_e);
				second.put("expediteneedtime", expediteneedtime);
				second.put("expeditecutofftime", expeditecutofftime);
				second.put("expeditecutofftime_t", expeditecutofftime_t);
				second.put("expeditecutofftime_e", expeditecutofftime_e);
			} else if (dictRateBean.price_level_id == 3) {
				third.put("needtime", needtime);
				third.put("cutofftime", cutofftime);
				third.put("cutofftime_t", cutofftime_t);
				third.put("cutofftime_e", cutofftime_e);
				third.put("expediteneedtime", expediteneedtime);
				third.put("expeditecutofftime", expeditecutofftime);
				third.put("expeditecutofftime_t", expeditecutofftime_t);
				third.put("expeditecutofftime_e", expeditecutofftime_e);
			} else {
				continue;
			}
			obj.put("first", first);
			obj.put("second", second);
			obj.put("third", third);
		}
		return obj;
	}

	public int Calculate(JSONObject task) {
		int ret = Const.FAIL;

		// String message = "";
		JSONObject obj = new JSONObject();
		if (_factor == null) {
			SendToPHP(task, "FAILED");
			return ret;
		}

		int pairid = Util.GetIntFromJSon("pairid", task);
		int wordcount = Util.GetIntFromJSon("wordcount", task);
		try {
			if (wordcount < 0) {
				wordcount = 0;
			}
			JSONObject result;
			if (pairid > 0) {
				int custo_rate = Util.GetIntFromJSon("custo_rate", task);
				if (_maptime.containsKey(pairid)) {
					result = this.CalculatePairTime(task, _maptime.get(pairid),
							wordcount);
					obj.put("time", result);
				}

				float factor = Util.GetFloatFromJSon("factor", task);
				if (factor <= 0) {
					factor = 1;
				}
				if (_mapcny.containsKey(pairid)) {
					int customer_id = Util.GetIntFromJSon("customer_id", task);

					if (custo_rate > 0 && customer_id > 0) {

						DictRateDAO ratedao = null;
						RateCurrencyBean rate = null;
						try {
							ratedao = new DictRateDAO();
							ratedao.Init(true);
							rate = ratedao.SelectRateCNY(customer_id, pairid);
						} catch (Exception e) {
							Log4j.error(e);
						} finally {
							if (ratedao != null) {
								ratedao.UnInit();
							}
						}
						if (rate == null) {
							result = this.CalculatePairMoney(task,
									_mapcny.get(pairid), wordcount, factor);
						} else {
							result = this.CalculatePairMoney_Custo(task, rate,
									wordcount, factor);
						}

					} else {
						result = this.CalculatePairMoney(task,
								_mapcny.get(pairid), wordcount, factor);
					}
					obj.put("cny", result);
				}
				if (_mapusd.containsKey(pairid)) {
					result = this.CalculatePairMoney(task, _mapusd.get(pairid),
							wordcount, factor);
					obj.put("usd", result);
				}
				if (_mapeur.containsKey(pairid)) {
					result = this.CalculatePairMoney(task, _mapeur.get(pairid),
							wordcount, factor);
					obj.put("eur", result);
				}
			}
			obj.put("result", "OK");
		} catch (Exception e) {
			obj.put("result", "FAILED");
			Log4j.error(e);
		}

		obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		obj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, task));
		obj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, task));
		obj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, task));

		_reportor.Report(obj);

		ret = Const.SUCCESS;

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector _collector) {
		String taskStr = tuple.getStringByField("task");
		JSONObject task = new JSONObject(taskStr);
		String aid = Util.GetStringFromJSon("aid", task);

		Log4j.log("calculatebolt " + task.toString());
		// 根据语言对及字数计算价格的bolt
		switch (aid) {
		case "money": {
			Calculate(task);
			break;
		}

		default:
			_collector.emit(new Values(task.toString()));
			break;
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("task"));
	}

}
