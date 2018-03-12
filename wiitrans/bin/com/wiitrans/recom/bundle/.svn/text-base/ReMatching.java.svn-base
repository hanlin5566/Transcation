package com.wiitrans.recom.bundle;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class ReMatching extends Thread {

	private ConcurrentHashMap<String, JSONObject> _orders = new ConcurrentHashMap<String, JSONObject>();
	private BundleRequest _spout = null;

	public ReMatching(BundleRequest spout, Bundle bundle) {
		_spout = spout;
	}

	public int Start() {
		int ret = Const.FAIL;

		this.start();
		ret = Const.SUCCESS;

		return ret;
	}

	public int Stop() {
		int ret = Const.FAIL;

		return ret;
	}

	public void Put(String ordercode, JSONObject obj) {
		_orders.put(ordercode, obj);
	}

	public void Remove(String ordercode) {
		_orders.remove(ordercode);
	}

	public void SetTran(String ordercode, boolean isTranslator, int userID) {
		if (_orders.containsKey(ordercode)) {
			JSONObject obj = _orders.get(ordercode);
			if (obj != null) {
				if (isTranslator) {
					obj.put("translator", String.valueOf(userID));
				} else {
					obj.put("editor", String.valueOf(userID));
				}
			}
		}
	}

	public int Request(JSONObject obj) {
		return _spout.Push(obj.toString());
	}

	public void run() {

		// AppConfig app = new AppConfig();
		// app.Parse();
		//
		// BundleParam param = app._bundles.get("recomTopo");
		BundleParam param = WiitransConfig.getInstance(1).RECOM;

		int cycle = param.BUNDLE_ORDER_SYN_CYCLE * 1000;
		if (cycle < 30000) {
			cycle = 30000;
		}

		// 启动后立刻补单一次，可以让以前的订单在orderbolt中存在。
		OrderSyn();

		if (param.BUNDLE_ORDER_SYN) {
			while (true) {

				// 30s
				try {
					sleep(cycle);
				} catch (Exception e) {
					Log4j.error(e);
				} finally {

				}

				OrderSyn();
			}
		}
	}

	private int OrderSyn() {
		int ret = Const.FAIL;

		try {
			// sleep(30000000);

			Collection<JSONObject> orders = _orders.values();

			// 30秒一次订单确认

			for (JSONObject order : orders) {
				Request(order);
			}

		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		} finally {

		}
		return ret;
	}
}
