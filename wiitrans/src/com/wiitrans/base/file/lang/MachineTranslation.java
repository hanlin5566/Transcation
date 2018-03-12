package com.wiitrans.base.file.lang;

import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleMT;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.log.Log4j;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class MachineTranslation {
	private static class UTF8PostMethod extends PostMethod {
		// 将url转换为utf8编码
		public UTF8PostMethod(String url) {
			super(url);
		}

		@Override
		public String getRequestCharSet() {
			return "UTF-8";
		}
	}

	public enum TRANS_TYPE {
		E2C, C2E
	}

	public static void main(String[] args) {
		try {

			MachineTranslation machine = new MachineTranslation(TRANS_TYPE.C2E);
			String aa = machine.run("真心英雄");
			System.out.print(aa);
			aa = machine.run("不知到怎么办好了啊。");
			System.out.print(aa);
		} catch (Exception e) {
		}
	}

	private HttpClient _client = null;
	// private PostMethod _method = null;
	private HashMap<TRANS_TYPE, BundleMT> _map = null;
	private String url = null;
	private String from = null;
	private String to = null;
	public boolean _usable = false;

	public MachineTranslation(TRANS_TYPE type) {
		_client = new HttpClient();
		_map = new HashMap<TRANS_TYPE, BundleMT>();
		if (_map.size() == 0) {
			// AppConfig app = new AppConfig();
			// app.ParseBundle();
			// BundleParam bundel = app._bundles.get("termTopo");
			BundleParam bundle = WiitransConfig.getInstance(0).TERM;
			for (BundleMT mt : bundle.BUNDLE_MACHINE_TRANSLATION) {
				switch (mt.name.toLowerCase()) {
				case "e2c": {
					if (!mt.use) {
						_usable = false;
						return;
					} else if (!_map.containsKey(TRANS_TYPE.E2C)) {
						_map.put(TRANS_TYPE.E2C, mt);
					}
					break;
				}
				case "c2e": {
					if (!mt.use) {
						_usable = false;
						return;
					} else if (!_map.containsKey(TRANS_TYPE.C2E)) {
						_map.put(TRANS_TYPE.C2E, mt);
					}
					break;
				}
				default:
					break;
				}
			}
		}
		HttpConnectionManagerParams managerParams = _client
				.getHttpConnectionManager().getParams();
		BundleMT mt = null;
		PostMethod method = null;
		switch (type) {
		case E2C: {
			if (_map.containsKey(TRANS_TYPE.E2C)) {
				mt = _map.get(TRANS_TYPE.E2C);
			}

			if (mt == null) {
				this.url = "http://192.168.9.220:1518/NiuTransServer/translateE2C";
				managerParams.setConnectionTimeout(3000);
			} else {
				this.url = mt.url;
				managerParams.setConnectionTimeout(mt.timeout * 1000);
			}
			method = new UTF8PostMethod(this.url);

			this.from = "english";
			this.to = "chinese";

			method.setParameter("from", from);
			method.setParameter("to", to);
			method.setParameter("src_text", "Hello");

			break;
		}
		case C2E: {
			if (_map.containsKey(TRANS_TYPE.C2E)) {
				mt = _map.get(TRANS_TYPE.C2E);
			}

			if (mt == null) {
				this.url = "http://192.168.9.220:1517/NiuTransServer/translateC2E";
				managerParams.setConnectionTimeout(3000);
			} else {
				this.url = mt.url;
				managerParams.setConnectionTimeout(mt.timeout * 1000);
			}

			method = new UTF8PostMethod(this.url);

			this.from = "chinese";
			this.to = "english";
			method.setParameter("from", this.from);
			method.setParameter("to", this.to);
			method.setParameter("src_text", "你好");

			break;
		}
		default:
			managerParams.setConnectionTimeout(3000);
			break;
		}

		try {
			_client.executeMethod(method);
			_usable = true;
		} catch (Exception e) {
			Log4j.error(e);
			method.releaseConnection();
		}
	}

	// public void ReleaseConnection() {
	// if (_method != null) {
	// _method.releaseConnection();
	// }
	// }

	public String run(String textString) throws Exception {
		String result = "";
		if (_client != null) {
			PostMethod method = null;
			try {

				method = new UTF8PostMethod(this.url);

				method.setParameter("from", this.from);
				method.setParameter("to", this.to);

				method.setParameter("src_text", textString);
				_client.executeMethod(method);

				String str = method.getResponseBodyAsString();
				JSONObject json = new JSONObject(str);

				if (str.indexOf("tgt_text") != -1) {
					result = json.get("tgt_text").toString();
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
		return result;
	}
}
