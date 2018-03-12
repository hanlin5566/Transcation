package com.wiitrans.automation.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.db.TMDAO;
import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class AnalyseTMLogicImpl implements Logic {
	public List<String> _targetURL;

	public AnalyseTMLogicImpl() {
		WiitransConfig.getInstance(0);
		Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
		_targetURL = new ArrayList<String>();
		ConfigNode node;
		for (Integer node_id : set) {
			if (node_id != BundleConf.DEFAULT_NID) {
				node = BundleConf.BUNDLE_Node.get(node_id);
				if (node != null && node.api != null
						&& node.api.trim().length() > 0) {
					_targetURL.add(node.api + "automation/newtask/");
				}
			}
		}
	}

	@Override
	public void invoke(JSONObject jsonObject) throws Exception {
		// 同步房间信息
		Log4j.log("start invoke AnalyseTMLogicImpl");

		// System.out.println("default node:" + BundleConf.DEFAULT_NID);

		// 本节点操作
		this.AnalyseTM(jsonObject);

		// 同步其他节点

		JSONObject newJSON = new JSONObject(jsonObject.toString());
		JSONObject paramJson = newJSON.getJSONObject("param");
		int nid = Util.GetIntFromJSon("nid", paramJson);
		if (nid == BundleConf.DEFAULT_NID) {
			newJSON.remove("id");// 移除ID，不沿用此连接，让其重新生成。
			String synctype = Util.GetStringFromJSon("synctype", paramJson);
			paramJson.put("synctype", "other");
			if (synctype != null && synctype.equalsIgnoreCase("local")) {

				for (String url : _targetURL) {
					newJSON.put("nid", String.valueOf(nid));
					new HttpSimulator(url).executeMethodTimeOut(
							newJSON.toString(), 2);
				}
			}
		}
	}

	private void AnalyseTM(JSONObject jsonObject) throws Exception {
		JSONObject paramJson = jsonObject.getJSONObject("param");
		int nid = Util.GetIntFromJSon("nid", paramJson);
		int tmID = Util.GetIntFromJSon("tmid", paramJson);
		TMDAO dao = null;
		try {
			dao = new TMDAO();
			dao.Init(false);
			TMBean tmbean;
			if (nid == BundleConf.DEFAULT_NID) {
				tmbean = dao.SelectForTMID(tmID);
			} else {
				tmbean = dao.SelectForNodeTMID(tmID);
			}
			if (!tmbean.analyse) {
				JSONObject params = null;
				try {
					params = new JSONObject();
					params.put("sid", Util.GetStringFromJSon("sid", jsonObject));
					params.put("uid", Util.GetStringFromJSon("uid", jsonObject));
					params.put("nid", String.valueOf(nid));
					params.put("tnid", String.valueOf(nid));
					params.put("tmid", String.valueOf(tmID));

					// BundleParam bundleparam =
					// WiitransConfig.getInstance(0).AUTO;
					// String url = bundleparam.TMSVR_API;
					String url = BundleConf.BUNDLE_TMSVR_API + "analysis";
					Log4j.log("start invoke TMSVR_API " + url);

					params.put("method", "DELETE");
					new HttpSimulator(url).executeMethodTimeOut(
							params.toString(), 2);
					params.put("method", "POST");
					new HttpSimulator(url).executeMethodTimeOut(
							params.toString(), 2);
					// }

				} catch (Exception e) {
					Log4j.error(e);
				}
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
	}
}
