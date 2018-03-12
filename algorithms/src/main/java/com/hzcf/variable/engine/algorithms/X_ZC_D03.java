package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年12月01日 
 **/
public class X_ZC_D03 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("riskItems");
		boolean ret = false;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			if(10 == subJson.getIntValue("riskTypeCode")){
				ret = true;
				break;
			}
		}
		return ret;
	}

}
