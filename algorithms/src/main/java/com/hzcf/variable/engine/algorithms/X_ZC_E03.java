package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 * loanInfos 中 borrowState=1 并且
 * contractDate合同时间戳在6个月以内的统计
 **/
public class X_ZC_E03 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONObject subJson = json.getJSONObject("overdue");
		int ret = 0;
		int overdueTimes_90 = subJson.getIntValue("90overdueTimes");
		int overdueTimes_180 = subJson.getIntValue("180overdueTimes");
		ret = overdueTimes_90+overdueTimes_180;
		return ret;
	}
}

