package com.wiitrans.opt.bolt;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
//import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.hbase.HBaseTimeLogDAO;
import com.wiitrans.base.hbase.HBaseTimeRangeLogDAO;
import com.wiitrans.base.hbase.HBaseUtil;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.hbase.HbaseGradeTestDAO;
import com.wiitrans.base.hbase.HbaseOrderLogDAO;
//import com.wiitrans.base.hbase.HbaseRow;
import com.wiitrans.base.hbase.model.LogStat;
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
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class SearchBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;
	// private HBaseTimeRangeLogDAO _useractiondao = null;

	private NumberFormat _numberFormat8 = null;
	private SimpleDateFormat _simpledateformat = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.OPERATION_BUNDLE_PORT);
		_reportor.Start();

		if (_simpledateformat == null) {
			_simpledateformat = new SimpleDateFormat("yyyyMMdd");
		}

		if (_numberFormat8 == null) {
			_numberFormat8 = NumberFormat.getInstance();
			_numberFormat8.setGroupingUsed(false);
			_numberFormat8.setMaximumIntegerDigits(8);
			_numberFormat8.setMinimumIntegerDigits(8);
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

	private JSONObject SetLogStat(LogStat logstat) {
		JSONObject result = null;
		if (logstat != null) {
			result = new JSONObject();
			result.put("allrowcount", String.valueOf(logstat.allrowcount));
			result.put("rowcount", String.valueOf(logstat.rowcount));
			result.put("startrowkey", String.valueOf(logstat.startrowkey));
			result.put("endrowkey", String.valueOf(logstat.endrowkey));
			result.put("table_id", String.valueOf(logstat.table_id));
		}
		return result;
	}

	private int GetLogStat(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO orderlogdao = null;
		HBaseTimeLogDAO userlogdao = null;
		HBaseTimeLogDAO financelogdao = null;
		HBaseTimeLogDAO operatelogdao = null;
		HBaseTimeLogDAO systemlogdao = null;
		HbaseGradeTestDAO gradetestdao = null;
		HbaseOrderLogDAO ordercyclelogdao = null;
		try {

			orderlogdao = new HBaseTimeLogDAO();
			orderlogdao.Init(true);
			orderlogdao.SETTableName(HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_ORDER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_ORDER_LOG_TABLE_PREFIX);
			LogStat orderlogstat = orderlogdao.GetLogStat();
			JSONObject orderlog = SetLogStat(orderlogstat);

			userlogdao = new HBaseTimeLogDAO();
			userlogdao.Init(true);
			userlogdao.SETTableName(HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_USER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_USER_LOG_TABLE_PREFIX);
			LogStat userlogstat = userlogdao.GetLogStat();
			JSONObject userlog = SetLogStat(userlogstat);

			financelogdao = new HBaseTimeLogDAO();
			financelogdao.Init(true);
			financelogdao.SETTableName(
					HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_FINANCE_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_FINANCE_LOG_TABLE_PREFIX);
			LogStat financelogstat = financelogdao.GetLogStat();
			JSONObject financelog = SetLogStat(financelogstat);

			operatelogdao = new HBaseTimeLogDAO();
			operatelogdao.Init(true);
			operatelogdao.SETTableName(
					HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_OPERATE_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_OPERATE_LOG_TABLE_PREFIX);
			LogStat operatelogstat = operatelogdao.GetLogStat();
			JSONObject operatelog = SetLogStat(operatelogstat);

			systemlogdao = new HBaseTimeLogDAO();
			systemlogdao.Init(true);
			systemlogdao.SETTableName(HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_TABLE_PREFIX);
			LogStat systemlogstat = systemlogdao.GetLogStat();
			JSONObject systemlog = SetLogStat(systemlogstat);

			ordercyclelogdao = new HbaseOrderLogDAO();
			ordercyclelogdao.Init(true);
			ordercyclelogdao.SETTableName(
					HbaseConfig.HBASE_ORDER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ORDER_LOG_TABLE_PREFIX);
			ordercyclelogdao.CreateTables();

			gradetestdao = new HbaseGradeTestDAO();
			gradetestdao.Init(true);
			gradetestdao.CreateTable();

			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "initlog");
			if (orderlog != null) {
				resObj.put(HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX, orderlog);
			}
			if (userlog != null) {
				resObj.put(HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX, userlog);
			}
			if (financelog != null) {
				resObj.put(HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX,
						financelog);
			}
			if (operatelog != null) {
				resObj.put(HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX,
						operatelog);
			}
			if (systemlog != null) {
				resObj.put(HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX, systemlog);
			}

			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);

		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (orderlogdao != null) {
				orderlogdao.UnInit();
			}
			if (userlogdao != null) {
				userlogdao.UnInit();
			}
			if (financelogdao != null) {
				financelogdao.UnInit();
			}
			if (operatelogdao != null) {
				operatelogdao.UnInit();
			}
			if (systemlogdao != null) {
				systemlogdao.UnInit();
			}
			if (ordercyclelogdao != null) {
				ordercyclelogdao.UnInit();
			}

		}

		return ret;
	}

	private int SearchLogs(JSONObject obj, String log_index,
			String log_columnfamily, String log_tableprefix, Set<String> set) {
		int ret = Const.FAIL;
		HBaseTimeLogDAO dao = null;

		try {

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(log_index, log_columnfamily, log_tableprefix);
			int pageindex = Util.GetIntFromJSon("pageindex", obj);
			int pagerowcount = Util.GetIntFromJSon("pagecount", obj);
			int begin = Util.GetIntFromJSon("begin", obj);
			int end = Util.GetIntFromJSon("end", obj);

			JSONObject resObj = dao.SearchLogs(pageindex, pagerowcount, begin,
					end, set);
			resObj.put("result", "OK");
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);

		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int SearchGradeTestLogs(JSONObject obj, Set<String> set) {
		int ret = Const.FAIL;
		HbaseGradeTestDAO dao = null;

		try {
			String uid = Util.GetStringFromJSon("suid", obj);

			dao = new HbaseGradeTestDAO();
			dao.Init(true);

			JSONObject resObj = dao.SearchLogs(uid, set);
			resObj.put("result", "OK");
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);

		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int SearchLog(JSONObject obj, Set<String> set) {
		int ret = Const.FAIL;
		HbaseGradeTestDAO dao = null;

		try {

			String rowkey = Util.GetStringFromJSon("rowkey", obj);
			if (rowkey != null && rowkey.length() > 0) {
				dao = new HbaseGradeTestDAO();
				dao.Init(true);

				JSONObject resObj = dao.SearchLog(set, rowkey);
				resObj.put("result", "OK");
				resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
				resObj.put(Const.BUNDLE_INFO_ID,
						Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
				resObj.put(Const.BUNDLE_INFO_ACTION_ID, Util.GetStringFromJSon(
						Const.BUNDLE_INFO_ACTION_ID, obj));

				return _reportor.Report(resObj);
			} else {
				ret = SendToPHP(obj, "FAILED");
			}

		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int SearchOrderLogs(JSONObject obj) {
		int ret = Const.FAIL;
		HbaseOrderLogDAO dao = null;

		try {

			dao = new HbaseOrderLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ORDER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ORDER_LOG_TABLE_PREFIX);
			String ordercode = Util.GetStringFromJSon("ordercode", obj);

			JSONObject resObj = dao.SearchLogs(ordercode);
			resObj.put("result", "OK");
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);

		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int SearchUserActionLogs(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeRangeLogDAO useractiondao = null;
		Date today = null;
		Date startdate = null;
		Date enddate = null;
		try {
			today = _simpledateformat.parse(_simpledateformat
					.format(new Date()));
			startdate = today;
			enddate = today;

			startdate = _simpledateformat.parse(Util.GetStringFromJSon(
					"startdate", obj));
			enddate = _simpledateformat.parse(Util.GetStringFromJSon("enddate",
					obj));

		} catch (Exception e) {
			Log4j.error(e);
		}

		try {

			int suid = Util.GetIntFromJSon("suid", obj);

			if (startdate.after(today)) {
				startdate = today;
			}

			if (enddate.after(today)) {
				enddate = today;
			}

			if (startdate.after(enddate)) {
				ret = SendToPHP(obj, "FAILED");
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startdate);
				int day1 = calendar.get(Calendar.DAY_OF_YEAR);
				calendar.setTime(enddate);
				int day2 = calendar.get(Calendar.DAY_OF_YEAR);
				if (day2 - day1 > 7) {
					ret = SendToPHP(obj, "FAILED");
				} else {

					useractiondao = new HBaseTimeRangeLogDAO();
					useractiondao.Init(true);
					useractiondao.SETTableName(
							HbaseConfig.HBASE_USER_ACTION_LOG_COLUMN_FAMILY,
							HbaseConfig.HBASE_USER_ACTION_LOG_TABLE_PREFIX);

					String len8uid = _numberFormat8.format(suid);
					String hash2char = HBaseUtil.GetHash2FromString(len8uid);

					String startrowkey = hash2char + len8uid + "00000000";
					String endrowkey = hash2char + len8uid + "99999999";

					ArrayList<Integer> list = new ArrayList<Integer>();

					Calendar dd = Calendar.getInstance();
					dd.setTime(startdate);
					while (dd.getTime().getTime() <= enddate.getTime()) {
						list.add(Integer.parseInt(_simpledateformat.format(dd
								.getTime())));
						dd.add(Calendar.DAY_OF_YEAR, 1);
					}
					int pageindex = Util.GetIntFromJSon("pageindex", obj);
					int pagerowcount = Util.GetIntFromJSon("pagecount", obj);

					JSONObject resObj = useractiondao.SearchLogs(suid,
							startrowkey, endrowkey, list, pageindex,
							pagerowcount);
					resObj.put("result", "OK");
					resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
					resObj.put(Const.BUNDLE_INFO_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
					resObj.put(Const.BUNDLE_INFO_ACTION_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID,
									obj));

					return _reportor.Report(resObj);
				}
			}
		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
			if (useractiondao != null) {
				useractiondao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String taskStr = tuple.getStringByField("task");
		JSONObject task = new JSONObject(taskStr);

		String aid = task.getString("aid");
		String method = Util.GetStringFromJSon("method", task);
		Log4j.log("searchbolt " + task.toString());

		switch (aid) {
		case "adminorder": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("order_id");
				set.add("opera");
				set.add("ip");
				set.add("content");
				set.add("user_name");
				SearchLogs(task, HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX,
						HbaseConfig.HBASE_ADMIN_ORDER_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_ADMIN_ORDER_LOG_TABLE_PREFIX, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "adminuser": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("opera");
				set.add("ip");
				set.add("content");
				set.add("user_name");
				SearchLogs(task, HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX,
						HbaseConfig.HBASE_ADMIN_USER_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_ADMIN_USER_LOG_TABLE_PREFIX, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "adminfinance": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("opera");
				set.add("ip");
				set.add("content");
				set.add("user_name");
				SearchLogs(task, HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX,
						HbaseConfig.HBASE_ADMIN_FINANCE_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_ADMIN_FINANCE_LOG_TABLE_PREFIX, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "adminoperate": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("opera");
				set.add("ip");
				set.add("content");
				set.add("user_name");
				SearchLogs(task, HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX,
						HbaseConfig.HBASE_ADMIN_OPERATE_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_ADMIN_OPERATE_LOG_TABLE_PREFIX, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "adminsystem": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("opera");
				set.add("ip");
				set.add("content");
				set.add("user_name");
				SearchLogs(task, HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX,
						HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_TABLE_PREFIX, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "gradetest": {
			switch (method) {
			case "GET": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("translator_id");
				set.add("pair_id");
				set.add("grade");
				set.add("create_time");
				set.add("industry");
				set.add("type");
				set.add("user_name");
				// set.add("question_test");
				SearchGradeTestLogs(task, set);
				break;
			}
			case "PUT": {
				Set<String> set = new HashSet<String>();
				set.add("uid");
				set.add("translator_id");
				set.add("pair_id");
				set.add("grade");
				set.add("create_time");
				set.add("industry");
				set.add("type");
				set.add("user_name");
				set.add("question_test");
				// set.add("question_test");
				SearchLog(task, set);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}

			break;
		}
		case "logstat": {
			switch (method) {
			case "POST": {
				GetLogStat(task);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}
			break;
		}
		case "ordercycle": {
			switch (method) {
			case "POST": {
				collector.emit(new Values(task.toString()));
				break;
			}
			case "GET": {
				SearchOrderLogs(task);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
				break;
			}
			break;
		}
		case "useraction": {
			switch (method) {
			case "POST": {
				collector.emit(new Values(task.toString()));
				break;
			}
			case "GET": {
				SearchUserActionLogs(task);
				break;
			}
			default:
				collector.emit(new Values(task.toString()));
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
		declarer.declare(new Fields("content"));
	}
}
