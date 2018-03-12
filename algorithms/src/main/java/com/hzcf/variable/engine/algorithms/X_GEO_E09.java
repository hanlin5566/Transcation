package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 **/
public class X_GEO_E09 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("RSL");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			JSONObject rs = subJson.getJSONObject("RS");
			String desc = rs.getString("desc");
			JSONObject jsonDesc = JSON.parseObject(desc);
			if(jsonDesc.getInteger("result_YQ_LJCS")!=null){
				ret = jsonDesc.getInteger("result_YQ_LJCS").intValue();
			}
		}
		return ret;
	}
}
