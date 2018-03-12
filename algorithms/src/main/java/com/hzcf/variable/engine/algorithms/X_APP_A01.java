package com.hzcf.variable.engine.algorithms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日
 * 申请人身份证到期日距离申请日期
 * 2001.11.12-2020.11.12
 **/
public class X_APP_A01 extends AbstractAlgorithms{
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		String idCardValidDate = json.get("idCardValidDate").toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		Date sDate = new Date();
		Date eDate = sdf.parse(idCardValidDate.split("-")[1]);//身份证结束日
		Calendar cal = Calendar.getInstance();
		cal.setTime(sDate);
		int yearStart = cal.get(Calendar.YEAR);
		int monthStart = cal.get(Calendar.MONTH);
		cal.setTime(eDate);
		int yearEnd = cal.get(Calendar.YEAR);
		int monthEnd = cal.get(Calendar.MONTH);

        int result;
        if(yearStart == yearEnd) {
            result = monthStart - monthEnd;
        } else {
            result = 12*(yearStart - yearEnd) + monthStart - monthEnd;
        }
		
		return result;
	}
	
	

}
