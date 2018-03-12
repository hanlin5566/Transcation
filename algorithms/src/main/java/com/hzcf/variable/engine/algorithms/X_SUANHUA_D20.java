package com.hzcf.variable.engine.algorithms;

import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年12月01日 
 **/
public class X_SUANHUA_D20 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("others");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			if(subJson.get("orgLostContact")!=null && subJson.get("orgLostContact").toString().length()>0){
				String date_str = subJson.get("orgLostContact").toString();
				long sDate = System.currentTimeMillis();
				long eDate = sdf.parse(date_str).getTime();
				int days = (int) ((sDate - eDate) / (1000*3600*24));
				if(days<= 360){
					ret++;
				}
			}
		}
		return ret;
	}

}
