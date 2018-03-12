package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 * 申请人手机号距离申请日近0-30天活跃度数量<30
 * 并且申请人手机号距离申请日近0-180天活跃度数量<180
 **/
public class X_TENAN_A10 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("active_degree");
		boolean ret = false;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			if("call_day".equals(subJson.getString("app_point"))){
				JSONObject item = subJson.getJSONObject("item");
				int item_1m = item.getInteger("item_1m");
				int item_6m = item.getInteger("item_6m");
				return item_1m < 30 && item_6m <180;
			}
		}
		return ret;
	}

}
