package com.wiitrans.tmsvr.service;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.tmsvr.handler.AnalysisHandler;

public class TMService {

	public static void main(String[] args) {

		if (args != null && args.length == 3) {
//			AppConfig app = new AppConfig();
//			app.Parse(2);
			WiitransConfig.getInstance(2);
			// 解析输入参数
			int tnID = Util.String2Int(args[0]);
			int tmID = Util.String2Int(args[1]);
			String method = args[2];
			// 将输入参数传递给具体处理类
			Log4j.warn("tmsvr main NID[" + tnID + "] TMID[" + tmID + "]. ");
			switch (method) {
			case "POST": {
				AnalysisHandler handler = new AnalysisHandler(tnID, tmID);
				if (handler.Start() == Const.SUCCESS) {
					handler.AnalyseTM(tnID, tmID);
				}
				break;
			}
			case "PUT": {
				AnalysisHandler handler = new AnalysisHandler(tnID, tmID);
				if (handler.Start() == Const.SUCCESS) {
					handler.InitTM(tnID, tmID);
					handler.Run();
				}
				break;
			}
			default:
				break;
			}
		}
	}
}
