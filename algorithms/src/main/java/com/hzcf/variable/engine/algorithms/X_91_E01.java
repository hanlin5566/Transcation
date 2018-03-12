package com.hzcf.variable.engine.algorithms;

import java.util.Calendar;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日 
 * loanInfos 中 borrowState=1 并且
 * contractDate合同时间戳在6个月以内的统计
 **/
public class X_91_E01 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		JSONArray jsonArray = (JSONArray) json.get("loanInfos");
		int ret = 0;
		for (Object obj : jsonArray) {
			JSONObject subJson = (JSONObject)obj;
			if(subJson.getInteger("borrowState") == 1){
				String contractDate_str = subJson.getString("contractDate");
				if(contractDate_str.length()<13){
					//补全十三位
					for (int i = contractDate_str.length(); i < 13; i++) {
						contractDate_str+="0";
					}
				}
				Date sDate = new Date();
				Date eDate = new Date(Long.valueOf(contractDate_str));
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(sDate);
				int yearStart = cal.get(Calendar.YEAR);
				int monthStart = cal.get(Calendar.MONTH);
				cal.setTime(eDate);
				int yearEnd = cal.get(Calendar.YEAR);
				int monthEnd = cal.get(Calendar.MONTH);
		        int month;
		        if(yearStart == yearEnd) {
		            month = monthStart - monthEnd;
		        } else {
		            month = 12*(yearStart - yearEnd) + monthStart - monthEnd;
		        }
		        if(month < 6){
		        	ret++;
		        }
			}
		}
		return ret;
	}
}
