package com.wiitrans.msg.bolt;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.msg.wordfilter.FilteredResult;
import com.wiitrans.msg.wordfilter.WordFilterUtil;

// 过滤器负责过滤消息中的敏感内容

public class FilterBolt extends BaseBasicBolt {

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		String method = Util.GetStringFromJSon("method", obj);

		Log4j.log("filterbolt " + obj.toString());

		switch (method) {
		case "POST": {
			
			switch (aid) {
			case "login": {
				// 登录信息不过滤内容
				break;
			}
			case "newm": {
				// 信息推送不过滤内容
				break;
			}
			case "newmsg": {
				// 聊天消息过滤内容
				String msg = Util.GetStringFromJSon("msg", obj);
				FilteredResult result = WordFilterUtil.filterText(msg,'*');
				// TODO:mark:hlhu 未添加正则过滤（QQ号/电话。。。);
				// 1.过滤内容
				String filtermsg = result.getFilteredContent();
				obj.put("sourcemsg", Util.GetStringFromJSon("msg", obj));
				obj.put("msg", filtermsg);
				obj.put("isFilter", result.getBadWords().length()>0?"true":"false");
				obj.put("timestamp", ""+System.currentTimeMillis());
				// 2.表达式过滤QQ号，电话号
				Set<String> keys = BundleConf.FILTER_REGXS_MAP.keySet();
				for (String key : keys) {
					String value = BundleConf.FILTER_REGXS_MAP.get(key);
					Pattern regex = Pattern.compile(value);
					Matcher matcher = regex.matcher(filtermsg);
					//如果匹配到则替换
					if(matcher.find()){
						filtermsg = matcher.replaceFirst("********");
						obj.put("msg", filtermsg);
						obj.put("isFilter","true");
					}
				}
				// 3.将过滤后的内容进行转发
				//TODO:未加时间戳判断,防止将相同时间的聊天记录写入Hbase发生覆盖
				content = obj.toString();
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
		// 发送Storm消息到下一个Bolt
		collector.emit(new Values(content));

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}

}
