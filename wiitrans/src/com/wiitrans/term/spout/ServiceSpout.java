package com.wiitrans.term.spout;

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
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ServiceSpout extends BaseRichSpout {
	private SpoutOutputCollector _collector = null;
	private TaskCollector _taskCollector = null;
	private TaskReportor _reportor = null;

	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		ArrayList<JSONObject> tasks = _taskCollector.GetTasks();
		if (tasks != null) {
			for (JSONObject task : tasks) {
				String pair_id = null;
				String nid = null;
				try {
					pair_id = Util.GetStringFromJSon("pair_id", task);
					nid = Util.GetStringFromJSon("nid", task);
				} catch (Exception e) {
				}
				Log4j.debug("spout " + task.toString());
				if (pair_id != null && nid != null) {
					_collector.emit(new Values(task.toString()));
				} else {
					Log4j.warn("term spout nid or pair_id is null. ");
					JSONObject resObj = new JSONObject();
					resObj.put("result", "FAILED");
					resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
					resObj.put(Const.BUNDLE_INFO_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, task));
					resObj.put(Const.BUNDLE_INFO_ACTION_ID, Util
							.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID,
									task));

					_reportor.Report(resObj);
				}
			}
		} else {

			ThreadUtility.Sleep(50);
		}
	}

	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

		_collector = collector;
		_taskCollector = new TaskCollector(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.TERM_BUNDLE_PORT, BundleConf.TERM_BUNDLE_ID);
		_taskCollector.Start();

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.TERM_BUNDLE_PORT);
		_reportor.Start();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("task"));
	}

}
