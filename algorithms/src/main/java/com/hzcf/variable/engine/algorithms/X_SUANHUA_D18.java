package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年12月01日 
 **/
public class X_SUANHUA_D18 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("others");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			Integer integer = subJson.getInteger("orgBlackList");
			ret+=integer==null?0:integer.intValue();
		}
		return ret;
	}

}
