package com.wiitrans.analysis.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.interproc.ThreadUtility;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskCollector;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class ServiceSpout extends BaseRichSpout {

	SpoutOutputCollector _collector = null;
	TaskCollector _taskCollector = null;
	private TaskReportor _reportor = null;

	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {

		WiitransConfig.getInstance(0);

		_collector = collector;
		_taskCollector = new TaskCollector(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.ANALYSIS_BUNDLE_PORT, BundleConf.ANALYSIS_BUNDLE_ID);
		_taskCollector.Start();
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

	@Override
	public void nextTuple() {

		ArrayList<JSONObject> tasks = _taskCollector.GetTasks();
		if (tasks != null) {
			for (JSONObject task : tasks) {
				Log4j.debug("spout " + task.toString());
				String nid = Util.GetStringFromJSon("nid", task);
				if (nid != null) {
					_collector.emit(new Values(task.toString()));
				} else {
					SendToPHP(task, "FAILED");
					Log4j.warn("analysis spout nid is null. ");
				}
			}
		} else {
			ThreadUtility.Sleep(50);
		}
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		// ClientId FileId Language
		/*
		 * declarer.declare(new Fields("id", "fid", "lang"));
		 */
		declarer.declare(new Fields("task"));
	}

}