package com.wiitrans.state.bundle;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class LogInOutMonitorThread extends Thread {
	// private ConcurrentHashMap<String, UserLoginMsg> _userOnline = null;
	private ConcurrentHashMap<String, UserLoginMsg> _personalOnline = null;
	private ConcurrentHashMap<String, UserLoginMsg> _translatorOnline = null;
	private ConcurrentHashMap<String, UserLoginMsg> _businessOnline = null;
	private ConcurrentHashMap<String, UserLoginMsg> _mlvOnline = null;
	private String _stateURL = null;
	private int _stateTimeOut = 2;
	private int _timeout = 300;
	private int _cycle = 60;

	public LogInOutMonitorThread(
			ConcurrentHashMap<String, UserLoginMsg> personalOnline,
			ConcurrentHashMap<String, UserLoginMsg> translatorOnline,
			ConcurrentHashMap<String, UserLoginMsg> businessOnline,
			ConcurrentHashMap<String, UserLoginMsg> mlvOnline) {
		_personalOnline = personalOnline;
		_translatorOnline = translatorOnline;
		_businessOnline = businessOnline;
		_mlvOnline = mlvOnline;
	}

	public int Start() {
		int ret = Const.FAIL;

		ret = Init();

		if (Const.SUCCESS == ret) {
			this.start();
		}
		ret = Const.SUCCESS;

		return ret;
	}

	public int Stop() {
		int ret = Const.FAIL;

		// this.stop();

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		ConfigNode myNode = BundleConf.BUNDLE_Node.get(BundleConf.DEFAULT_NID);
		String url = "";
		if (myNode != null) {
			url = myNode.api;
		}
		_stateURL = url + "state/user/";

		// AppConfig app = new AppConfig();
		// app.ParseBundle();
		// BundleParam param = app._bundles.get("stateTopo");
		BundleParam param = WiitransConfig.getInstance(1).STATE;

		_cycle = param.BUNDLE_MONITOR_CYCLE;
		_timeout = param.BUNDLE_MONITOR_TIMEOUT;

		Log4j.log("          state-thread-stateurl= " + _stateURL);

		ret = Const.SUCCESS;

		return ret;
	}

	public void monitor(ConcurrentHashMap<String, UserLoginMsg> online,
			int role_id) {
		UserLoginMsg msg = null;
		String role = null;
		try {

			switch (role_id) {
			case 1:
				role = "perso-user";
				break;
			case 2:
				role = "trans-user";
				break;
			case 3:
				role = "busin-user";
				break;
			case 4:
				role = "mlv-user";
				break;
			default:
				role = "undef-user";
				break;
			}
			Set<String> set = online.keySet();
			for (String key : set) {
				msg = online.get(key);
				if (msg != null) {
					Log4j.info(role + " uid(" + msg._uid + ") sid(" + msg._sid
							+ ") login(" + msg._login + ") logout("
							+ msg._logout + ") now(" + Util.GetIntFromNow()
							+ ")");
					if (msg._login < msg._logout
							&& msg._logout + _timeout <= Util.GetIntFromNow()) {
						// System.out.println("超时" + msg._uid);
						Log4j.warn("timeout uid(" + msg._uid + ") sid("
								+ msg._sid + ")");

						JSONObject params = null;
						try {
							params = new JSONObject();
							params.put("sid", msg._sid);
							params.put("uid", msg._uid);
							params.put("nid", msg._node_id);
							params.put("role_id", String.valueOf(msg._role_id));
							params.put("method", "DELETE");
							new HttpSimulator(_stateURL).executeMethodTimeOut(
									params.toString(), _stateTimeOut);
						} catch (Exception e) {
							Log4j.error(e);
						}
					}
				}
			}
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	public void run() {
		while (true) {

			try {

				Thread.sleep(_cycle * 1000);

				Log4j.debug("LogInOutMonitorThread begin");

				monitor(_personalOnline, 1);
				monitor(_translatorOnline, 2);
				monitor(_businessOnline, 3);
				monitor(_mlvOnline, 4);

				Log4j.debug("LogInOutMonitorThread end");

			} catch (InterruptedException e) {
				Log4j.error("LogInOutMonitorThread run InterruptedException", e);
			} catch (Exception e) {
				Log4j.error("LogInOutMonitorThread run Exception", e);
			}
		}
	}

	// public int Logout(String sid, String uid, int role_id) {
	// int ret = Const.FAIL;
	//
	// return ret;
	// }
}
