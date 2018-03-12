package com.wiitrans.opt.bolt;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.hbase.HBaseTimeLogDAO;
import com.wiitrans.base.hbase.HBaseTimeRangeLogDAO;
import com.wiitrans.base.hbase.HBaseUtil;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.hbase.HbaseGradeTestDAO;
import com.wiitrans.base.hbase.HbaseOrderLogDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class PersistenceBolt extends BaseBasicBolt {

	// private NumberFormat _numberFormat5 = null;
	private TaskReportor _reportor = null;
	private NumberFormat _numberFormat8 = null;
	private int itoday = 0;
	private SimpleDateFormat _dateformat = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
//		AppConfig app = new AppConfig();
//		app.Parse();
		WiitransConfig.getInstance(0);
		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.OPERATION_BUNDLE_PORT);
		_reportor.Start();

		if (_numberFormat8 == null) {
			_numberFormat8 = NumberFormat.getInstance();
			_numberFormat8.setGroupingUsed(false);
			_numberFormat8.setMaximumIntegerDigits(8);
			_numberFormat8.setMinimumIntegerDigits(8);
		}

		if (_dateformat == null) {
			_dateformat = new SimpleDateFormat("yyyyMMdd");
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

	private int InsertAdminOrderLog(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO dao = null;

		try {
			// "sid":"df45545dfs","uid":"3","order_id":"6","opera":"1","ip":"192.168.9.111"
			String uid = Util.GetStringFromJSon("uid", obj);
			String order_id = Util.GetStringFromJSon("order_id", obj);
			String opera = Util.GetStringFromJSon("opera", obj);
			String ip = Util.GetStringFromJSon("ip", obj);
			String content = Util.GetStringFromJSon("content", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			int table_id = Util.GetIntFromJSon("table_id", obj);
			int rowcount = Util.GetIntFromJSon("rowcount", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("order_id", order_id);
			map.put("opera", opera);
			map.put("ip", ip);
			map.put("content", content);
			map.put("user_name", user_name);

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_ORDER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_ORDER_LOG_TABLE_PREFIX);
			dao.InsertLog(map, table_id, rowcount);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "addadminorderlog");
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

	private int InsertAdminUserLog(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO dao = null;

		try {

			String uid = Util.GetStringFromJSon("uid", obj);
			String opera = Util.GetStringFromJSon("opera", obj);
			String ip = Util.GetStringFromJSon("ip", obj);
			String content = Util.GetStringFromJSon("content", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			int table_id = Util.GetIntFromJSon("table_id", obj);
			int rowcount = Util.GetIntFromJSon("rowcount", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("opera", opera);
			map.put("ip", ip);
			map.put("content", content);
			map.put("user_name", user_name);

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_USER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_USER_LOG_TABLE_PREFIX);
			dao.InsertLog(map, table_id, rowcount);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "addadminuserlog");
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

	private int InsertAdminFinanceLog(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO dao = null;

		try {
			String uid = Util.GetStringFromJSon("uid", obj);
			String opera = Util.GetStringFromJSon("opera", obj);
			String ip = Util.GetStringFromJSon("ip", obj);
			String content = Util.GetStringFromJSon("content", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			int table_id = Util.GetIntFromJSon("table_id", obj);
			int rowcount = Util.GetIntFromJSon("rowcount", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("opera", opera);
			map.put("ip", ip);
			map.put("content", content);
			map.put("user_name", user_name);

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_FINANCE_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_FINANCE_LOG_TABLE_PREFIX);
			dao.InsertLog(map, table_id, rowcount);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "addadminfinancelog");
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

	private int InsertAdminOperateLog(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO dao = null;

		try {
			// "sid":"df45545dfs","uid":"3","order_id":"6","opera":"1","ip":"192.168.9.111"
			String uid = Util.GetStringFromJSon("uid", obj);
			String opera = Util.GetStringFromJSon("opera", obj);
			String ip = Util.GetStringFromJSon("ip", obj);
			String content = Util.GetStringFromJSon("content", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			int table_id = Util.GetIntFromJSon("table_id", obj);
			int rowcount = Util.GetIntFromJSon("rowcount", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("opera", opera);
			map.put("ip", ip);
			map.put("content", content);
			map.put("user_name", user_name);

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_OPERATE_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_OPERATE_LOG_TABLE_PREFIX);
			dao.InsertLog(map, table_id, rowcount);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "addadminoperatelog");
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

	private int InsertAdminSystemLog(JSONObject obj) {
		int ret = Const.FAIL;

		HBaseTimeLogDAO dao = null;

		try {
			// "sid":"df45545dfs","uid":"3","order_id":"6","opera":"1","ip":"192.168.9.111"
			String uid = Util.GetStringFromJSon("uid", obj);
			String opera = Util.GetStringFromJSon("opera", obj);
			String ip = Util.GetStringFromJSon("ip", obj);
			String content = Util.GetStringFromJSon("content", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			int table_id = Util.GetIntFromJSon("table_id", obj);
			int rowcount = Util.GetIntFromJSon("rowcount", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("opera", opera);
			map.put("ip", ip);
			map.put("content", content);
			map.put("user_name", user_name);

			dao = new HBaseTimeLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX,
					HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_TABLE_PREFIX);
			dao.InsertLog(map, table_id, rowcount);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
			resObj.put("result", "OK");
			resObj.put("operate", "addadminsystemlog");
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

	private int InsertGradeTestLog(JSONObject obj) {
		int ret = Const.FAIL;

		HbaseGradeTestDAO dao = null;

		try {

			String uid = Util.GetStringFromJSon("uid", obj);
			String translator_id = Util.GetStringFromJSon("translator_id", obj);
			String pair_id = Util.GetStringFromJSon("pair_id", obj);
			String grade = Util.GetStringFromJSon("grade", obj);
			String create_time = Util.GetStringFromJSon("create_time", obj);
			String industry = Util.GetStringFromJSon("industry", obj);
			String user_name = Util.GetStringFromJSon("user_name", obj);
			String type = Util.GetStringFromJSon("type", obj);
			String question_test = Util.GetStringFromJSon("question_test", obj);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("uid", uid);
			map.put("translator_id", translator_id);
			map.put("pair_id", pair_id);
			map.put("grade", grade);
			map.put("create_time", create_time);
			map.put("industry", industry);
			map.put("user_name", user_name);
			map.put("type", type);
			map.put("question_test", question_test);
			dao = new HbaseGradeTestDAO();
			dao.Init(true);
			dao.InsertLog(translator_id, map);
			Thread.currentThread().sleep(1);
			JSONObject resObj = new JSONObject();
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

	private int InsertOrderLog(JSONObject obj) {
		int ret = Const.FAIL;

		HbaseOrderLogDAO dao = null;

		try {
			// String uid = Util.GetStringFromJSon("uid", obj);
			String jsonstring = Util.GetStringFromJSon("ordermsg", obj);
			String ordercode = Util.GetStringFromJSon("ordercode", obj);

			dao = new HbaseOrderLogDAO();
			dao.Init(true);
			dao.SETTableName(HbaseConfig.HBASE_ORDER_LOG_COLUMN_FAMILY,
					HbaseConfig.HBASE_ORDER_LOG_TABLE_PREFIX);
			dao.InsertLog(jsonstring, ordercode);

			Thread.currentThread().sleep(1);

			ret = SendToPHP(obj, "OK");
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

	private int InsertUserActionLog(JSONObject obj) {
		int ret = Const.FAIL;
		HBaseTimeRangeLogDAO useractiondao = null;
		try {
			int uid = Util.GetIntFromJSon("uid", obj);
			String jsonstring = Util.GetStringFromJSon("actionmsg", obj);
			int act = Util.GetIntFromJSon("action", new JSONObject(jsonstring));
			if (act == 11) {
				ret = Const.SUCCESS;
			} else {
			    	useractiondao = new HBaseTimeRangeLogDAO();
				useractiondao.Init(true);
				useractiondao.SETTableName(
						HbaseConfig.HBASE_USER_ACTION_LOG_COLUMN_FAMILY,
						HbaseConfig.HBASE_USER_ACTION_LOG_TABLE_PREFIX);
				Date now = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(now);
				int inow = Integer.parseInt(_dateformat.format(now));
				if (inow > itoday) {
					itoday = inow;
					useractiondao.CreateTable(itoday);
				}

				String len8uid = _numberFormat8.format(uid);

				String hash2char = HBaseUtil.GetHash2FromString(len8uid);

				// String len8time = _numberFormat8.format(timestamp %
				// 86400000);

				String len8time = _numberFormat8.format(calendar
						.get(Calendar.HOUR_OF_DAY)
						* 3600000
						+ calendar.get(Calendar.MINUTE)
						* 60000
						+ calendar.get(Calendar.SECOND)
						* 1000
						+ calendar.get(Calendar.MILLISECOND));

				String rowkey = hash2char + len8uid + len8time;
				useractiondao.InsertLog(jsonstring, rowkey, now, itoday);

				Thread.currentThread().sleep(1);
			}
			ret = SendToPHP(obj, "OK");
		} catch (Exception e) {
			Log4j.error(e);
			ret = SendToPHP(obj, "FAILED");
		} finally {
		    if(useractiondao != null){
			useractiondao.UnInit();
		    }
		}

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String taskStr = tuple.getStringByField("content");
		JSONObject task = new JSONObject(taskStr);

		String aid = Util.GetStringFromJSon("aid", task);
		String method = Util.GetStringFromJSon("method", task);
		Log4j.debug("persistencebolt " + task.toString());

		SendToPHP(task, "OK");

		switch (aid) {
		case "adminorder": {
			switch (method) {
			case "POST": {
				InsertAdminOrderLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "adminuser": {
			switch (method) {
			case "POST": {
				InsertAdminUserLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "adminfinance": {
			switch (method) {
			case "POST": {
				InsertAdminFinanceLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "adminoperate": {
			switch (method) {
			case "POST": {
				InsertAdminOperateLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "adminsystem": {
			switch (method) {
			case "POST": {
				InsertAdminSystemLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "gradetest": {
			switch (method) {
			case "POST": {
				InsertGradeTestLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}

			break;
		}
		case "ordercycle": {
			switch (method) {
			case "POST": {
				InsertOrderLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}
			break;
		}
		case "useraction": {
			switch (method) {
			case "POST": {
				InsertUserActionLog(task);
				break;
			}
			default:
				SendToPHP(task, "FAILED");
				break;
			}
			break;
		}
		default:
			SendToPHP(task, "FAILED");
			break;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}

}
