package com.wiitrans.automation.bolt;

import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.db.AutomationTaskDAO;
import com.wiitrans.base.db.model.AutomationTaskBean;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

// 1.Admin消息此处理器不许要处理
// 2.持久化Chat消息

public class ReportBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		if(BundleConf.SYNC_DATA_TEMPLATE.keySet().size() <= 0){
//			AppConfig app = new AppConfig();
//			app.Parse();
			WiitransConfig.getInstance(0);
		}
		if (_reportor == null) {
			_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
					BundleConf.AUTOMATION_BUNDLE_PORT);
			_reportor.Start();
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

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);
		switch (method) {
		case "POST": {
			switch (aid) {
			case "newtask": {
				//回写入DB任务状态
				AutomationTaskBean task = new AutomationTaskBean();
				task.setTask_id(Integer.parseInt(""+obj.getString("task_id")));
				task.setStatus(Integer.parseInt(""+obj.getString("status")));
				task.setRemaker(""+obj.getString("remarker"));
				AutomationTaskDAO taskDAO = new AutomationTaskDAO();
				taskDAO.Init(true);
				taskDAO.update(task);
				taskDAO.Commit();
				taskDAO.UnInit();
//				SendToPHP(obj, "STATUS:"+obj.getString("status")+" REMARKER:"+obj.getString("remarker")); 修改为收到请求后，存入DB立即返回
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
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {

	}
}
