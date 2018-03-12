package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 * loanInfos 中 borrowState=1 并且
 * contractDate合同时间戳在6个月以内的统计
 **/
public class X_GEO_E08 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("RSL");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			JSONObject rs = subJson.getJSONObject("RS");
			String desc = rs.getString("desc");
			JSONObject jsonDesc = JSON.parseObject(desc);
			JSONObject tjxx = jsonDesc.getJSONObject("TJXX_180d");
			ret +=  tjxx.getString("regtimes_bank")!=null&&tjxx.getString("regtimes_bank").length()<=0?0:tjxx.getIntValue("regtimes_bank");
			ret += tjxx.getString("regtimes_nonbank")!=null&&tjxx.getString("regtimes_nonbank").length()<=0?0:tjxx.getIntValue("regtimes_nonbank");
		}
		return ret;
	}
}
