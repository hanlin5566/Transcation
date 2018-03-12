package com.wiitrans.recom.spout;

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

	private SpoutOutputCollector _collector = null;
	private TaskCollector _taskCollector = null;
	private TaskReportor _reportor = null;

	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {

		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

		_collector = collector;
		_taskCollector = new TaskCollector(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.RECOMMEND_BUNDLE_PORT,
				BundleConf.RECOMMEND_BUNDLE_ID);
		_taskCollector.Start();
		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.RECOMMEND_BUNDLE_PORT);
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
				String langPairObj = Util.GetStringFromJSon("langpair", task);
				if (nid != null && langPairObj != null) {
					String[] langpairs = langPairObj.split("\\|");
					for (String langpair : langpairs) {
						if (langpair != null) {
							_collector.emit(new Values(langpair, task
									.toString()));
						} else {
							SendToPHP(task, "FAILED");
							Log4j.warn("recom spout langpair is null. ");
						}
					}
				} else {
					SendToPHP(task, "FAILED");
					Log4j.warn("recom spout nid is null or langpairs is null. ");
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

		declarer.declare(new Fields("langpair", "content"));
	}

}
