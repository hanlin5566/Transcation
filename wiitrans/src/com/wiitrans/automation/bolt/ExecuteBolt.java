package com.wiitrans.automation.bolt;


import org.json.JSONObject;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.wiitrans.automation.logic.Logic;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;

/**
 * 负责具体执行任务逻辑
 * @author root
 *
 */
public class ExecuteBolt extends BaseBasicBolt {

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);
		Log4j.log("ExecuteBolt " + obj.toString());
		switch (method) {
		case "POST": {
			switch (aid) {
			case "newtask": {
				JSONObject param = obj.getJSONObject("param");
				String className = ""+param.get("className");
				try {
					Logic logic = (Logic) Class.forName(className).newInstance();
					logic.invoke(obj);
					//如果没有异常则 发送Storm成功消息到下一个Bolt
					obj.put("status", ""+Const.SUCCESS);
					obj.put("remarker","SUCCESS");
				} catch (Exception e) {
					Log4j.error("ExecuteBolt ERROR" + obj.toString()+" case:"+ e.fillInStackTrace());
					//如果有异常则 发送Storm失败消息到下一个Bolt
					obj.put("status", ""+Const.FAIL);
					obj.put("remarker", "ExecuteBolt ERROR" + obj.toString()+" case:"+ e.fillInStackTrace());
				}
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
		content = obj.toString();
		collector.emit(new Values(content));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}

}
