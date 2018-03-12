package com.wiitrans.base.task;

import org.json.JSONObject;

import com.wiitrans.base.misc.Const;

public class TMTaskCollector extends TaskCollector {
	private int _tmID = 0;

	public TMTaskCollector(String ip, int port, String bundleId, int tmID) {
		super(ip, port, bundleId);
		_tmID = tmID;
	}

	public int Register() {
		int ret = Const.FAIL;

		if (_server != null) {
			JSONObject obj = new JSONObject();
			obj.put(Const.BUNDLE_INFO_BUNDLE_ID, _bundleId);
			obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REGISTER);
			obj.put(Const.BUNDLE_INFO_SESSION_ID, "");
			obj.put(Const.BUNDLE_INFO_ID, "");
			obj.put("tmid", String.valueOf(_tmID));

			_server.Response(obj.toString().getBytes());

			ret = Const.SUCCESS;
		}

		return ret;
	}

	public int UnRegister() {
		int ret = Const.FAIL;

		if (_server != null) {
			JSONObject obj = new JSONObject();
			obj.put(Const.BUNDLE_INFO_BUNDLE_ID, _bundleId);
			obj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_UNREGISTER);
			obj.put(Const.BUNDLE_INFO_SESSION_ID, "");
			obj.put(Const.BUNDLE_INFO_ID, "");
			obj.put("tmid", String.valueOf(_tmID));

			_server.Response(obj.toString().getBytes());

			ret = Const.SUCCESS;
		}

		return ret;
	}
}
