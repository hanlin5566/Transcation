package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月30日 
 **/
public class X_SUANHUA_E05 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("record");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			JSONArray datas = subJson.getJSONArray("classification");
			for (Object subObj : datas) {
				JSONObject data = (JSONObject)subObj;
				JSONObject m3 = data.getJSONObject("M3");
				if( m3 != null){
					JSONObject other = m3.getJSONObject("other");
					String totalAmount = other.getString("totalAmount");
					totalAmount = totalAmount.substring(1, totalAmount.length());
					totalAmount = totalAmount.substring(0,totalAmount.length()-1);
					String[] split = totalAmount.split(",");
					for (int i = 0; i < split.length; i++) {
						ret+=Long.parseLong(split[i].trim());
					}
					JSONObject bank = m3.getJSONObject("bank");
					totalAmount = bank.getString("totalAmount");
					totalAmount = totalAmount.substring(1, totalAmount.length());
					totalAmount = totalAmount.substring(0,totalAmount.length()-1);
					split = totalAmount.split(",");
					for (int i = 0; i < split.length; i++) {
						ret+=Long.parseLong(split[i].trim());
					}
				}
				JSONObject m6 = data.getJSONObject("M6");
				if( m6 != null){
					JSONObject other = m6.getJSONObject("other");
					String totalAmount = other.getString("totalAmount");
					totalAmount = totalAmount.substring(1, totalAmount.length());
					totalAmount = totalAmount.substring(0,totalAmount.length()-1);
					String[] split = totalAmount.split(",");
					for (int i = 0; i < split.length; i++) {
						ret+=Long.parseLong(split[i].trim());
					}
					JSONObject bank = m6.getJSONObject("bank");
					totalAmount = bank.getString("totalAmount");
					totalAmount = totalAmount.substring(1, totalAmount.length());
					totalAmount = totalAmount.substring(0,totalAmount.length()-1);
					split = totalAmount.split(",");
					for (int i = 0; i < split.length; i++) {
						ret+=Long.parseLong(split[i].trim());
					}
				}
			}
		}
		return ret;
	}
}

