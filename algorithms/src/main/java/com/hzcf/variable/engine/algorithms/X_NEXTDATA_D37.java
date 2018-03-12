package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 * loanInfos 中 borrowState=1 并且
 * contractDate合同时间戳在6个月以内的统计
 **/
public class X_NEXTDATA_D37 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("RSL");
		boolean ret = false;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			JSONObject rs = subJson.getJSONObject("RS");
			String desc = rs.getString("desc");
			JSONObject jsonDesc = JSON.parseObject(desc);
			if(jsonDesc.get("result_SX_LJCS")!=null && "null".equals(jsonDesc.getString("result_SX_LJCS"))){
				ret = true;
			}
		}
		return ret;
	}
}
