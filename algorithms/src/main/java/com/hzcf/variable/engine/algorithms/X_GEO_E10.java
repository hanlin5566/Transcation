package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 **/
public class X_GEO_E10 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("RSL");
		boolean ret = false;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			JSONObject rs = subJson.getJSONObject("RS");
			String desc = rs.getString("desc");
			JSONObject jsonDesc = JSON.parseObject(desc);
			if(jsonDesc.getString("result_YQ_DQSC")!=null && jsonDesc.getString("result_YQ_DQSC").length()>0 && "null".equals(jsonDesc.getString("result_YQ_DQSC"))){
				String str = jsonDesc.getString("result_YQ_DQSC");
				ret = str.equals("M2");
			}
		}
		return ret;
	}
}
