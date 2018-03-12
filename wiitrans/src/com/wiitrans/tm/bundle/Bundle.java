package com.wiitrans.tm.bundle;

import io.netty.buffer.ByteBuf;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;

import org.json.JSONObject;

public class Bundle extends Thread implements IBundle, IServer {

	private IResponse _res = null;
	private Client _client = null;
	private String _id = null;
	private BundleRequest _spout = null;

	// private ICache _cache = null;

	@Override
	public String GetBundleId() {

		return BundleConf.TM_BUNDLE_ID;
	}

	@Override
	public int SetResponse(IResponse res) {

		int ret = Const.FAIL;

		_res = res;
		ret = Const.SUCCESS;

		return ret;
	}

	@Override
	public int Start() {

		int ret = Const.FAIL;

		ret = Init();
		if (Const.SUCCESS == ret) {
			this.start();
		}

		if (Const.SUCCESS == ret) {
			ret = _spout.Start();
		}

		return ret;
	}

	@Override
	public int Stop() {

		int ret = Const.FAIL;

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		_spout = new BundleRequest();

		// if (Const.SUCCESS == ret) {
		// if (_cache == null) {
		// _cache = new RedisCache();
		// // 配置统一
		// ret = _cache.Init(BundleConf.BUNDLE_REDIS_IP);
		// }
		// }

		ret = Const.SUCCESS;
		return ret;
	}

	private int Invalid(String msg) {
		int ret = Const.FAIL;

		JSONObject obj = new JSONObject(msg);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		JSONObject resObj = new JSONObject();
		resObj.put("result", "FAILED");
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		_res.Response(id, resObj.toString().getBytes());

		return ret;
	}

	@Override
	public int Request(String msg) {

		int ret = Const.FAIL;
		if (_client != null) {
			ret = _spout.Push(msg);
		} else {
			ret = Invalid(msg);
		}
		return ret;
	}

	@Override
	public int Request(JSONObject msg) {

		Log4j.log("tm bundle " + msg.toString());

		String aid = Util.GetStringFromJSon("aid", msg);
		String method = Util.GetStringFromJSon("method", msg);
		switch (aid) {
		case "analysis": {
			switch (method) {
			case "POST": {
				// Login(msg);
				break;
			}
			default:
				break;
			}

			break;
		}
		default:
			break;
		}

		return Request(msg.toString());
	}

	public int Response(Client client) {

		int ret = Const.FAIL;

		JSONObject obj = client.GetBundleInfoJSON();
		String state = obj.getString(Const.BUNDLE_INFO_STATE);
		String id = obj.getString(Const.BUNDLE_INFO_ID);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			String bid = obj.getString(Const.BUNDLE_INFO_BUNDLE_ID);
			// Registe bundle.
			if (0 == (bid.compareTo(BundleConf.TM_BUNDLE_ID))) {
				if (_client != null) {
					_client.GetContext().close();
				}

				_client = client;
				_id = bid;
				_spout.SetClient(client);

				Log4j.log("Bundle[" + _id + "] is actived.");
			} else {
				Log4j.error("Registe bundle[" + _id + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			String result = Util.GetStringFromJSon("result", obj);
			if (result != null) {
				switch (result) {
				case "1111111111": {
					break;
				}
				default: {
					// 回复消息到PHP
					if (_res != null) {
						_res.Response(id, obj.toString().getBytes());
					} else {
						Log4j.error("tm service bundle callback is null");
					}
					break;
				}
				}
			} else {
				Log4j.error("The report msg result is null.");
			}
			break;
		}
		default:
			Log4j.error("tm service bundle state[" + state + "] error");
			break;
		}

		return ret;
	}

	@Override
	public int NewClient(Client client) {

		return Response(client);
	}
	
	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
		return Const.NOT_IMPLEMENTED;
	}

	public void run() {
		BaseServer svr = new BaseServer();
		svr.SetPort(BundleConf.TM_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}
}
