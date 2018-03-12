package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月30日 
 **/
public class X_EMAY_E04 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONArray jsonArray = JSON.parseArray(param);
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			String string = subJson.getString("type");
			if("emr009".equals(string)){
				JSONArray datas = subJson.getJSONArray("data");
				for (Object subObj : datas) {
					JSONObject data = (JSONObject)subObj;
					ret += data.getIntValue("REJECTIONTIME");
				}
			}
		}
		return ret;
	}
}

