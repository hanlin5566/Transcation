package com.wiitrans.automation.bolt;

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
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.AutomationTaskDAO;
import com.wiitrans.base.db.model.AutomationTaskBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

@SuppressWarnings("serial")
public class CheckBolt extends BaseBasicBolt {

	private TaskReportor _reportor = null;
	private ICache _cache = null;

	@SuppressWarnings("rawtypes")
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

		if (_cache == null) {
			_cache = new RedisCache();
			_cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);

		Log4j.log("CheckBolt " + obj.toString());
		boolean isSend = false;
		switch (method) {
		case "POST": {
			switch (aid) {
			case "newtask": {
				// 验证任务是否已经执行完毕，如果完毕则不发往下个blot
				isSend = isFinished(obj);
				break;
			}
			default:
				isSend = true;
				break;
			}
			break;
		}
		default:
			break;
		}
		if (isSend) {
			// 发送Storm消息到下一个Bolt
			collector.emit(new Values(content));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}
	
	private boolean isFinished(JSONObject obj){
		boolean ret = false;
		try {
			AutomationTaskBean task = new AutomationTaskBean();
			task.setTask_id(Integer.parseInt(obj.getString("task_id")));
			AutomationTaskDAO taskDAO = new AutomationTaskDAO();
			taskDAO.Init(true);
			ret = taskDAO.isActive(task);
			taskDAO.UnInit();
		} catch (Exception e) {
			// TODO: handle exception
			Log4j.error("checkbolt error: [param] " + obj.toString() + "[case]"+ e.getMessage());
		}
		return ret;
	}
}
