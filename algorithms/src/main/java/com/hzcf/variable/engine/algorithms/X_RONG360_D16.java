package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年12月01日 
 **/
public class X_RONG360_D16 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONArray jsonArray = JSON.parseArray(param);
		boolean ret = false;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			if(subJson.get("desc")!=null && subJson.get("desc").toString().length()>0){
				ret = true;
				break;
			}
		}
		return ret;
	}

}
