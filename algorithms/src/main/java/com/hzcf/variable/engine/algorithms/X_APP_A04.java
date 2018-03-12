package com.hzcf.variable.engine.algorithms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Create by hanlin on 2017年11月24日
 **/
public class X_APP_A04 extends AbstractAlgorithms {
	public Object execute(String param) throws Exception {
		JSONObject json = JSON.parseObject(param);
		String val = json.get("idCardAddress").toString();
		return ("西藏".contains(val) || "新疆".contains(val));
	}
}
