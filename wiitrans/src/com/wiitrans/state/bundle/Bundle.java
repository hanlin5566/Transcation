package com.wiitrans.state.bundle;


import io.netty.buffer.ByteBuf;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;


public class Bundle extends Thread implements IBundle, IServer {

	private StateQueueThread _queue = null;

	private IResponse _res = null;
	private String _id = null;
	private BundleRequest _spout = null;
	private Client _client = null;

	@Override
	public String GetBundleId() {

		return BundleConf.STATE_BUNDLE_ID;
	}

	@Override
	public int SetResponse(IResponse res) {

		int ret = Const.FAIL;

		_res = res;
		_queue.SetResponse(res);

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

		return _spout.Start();
	}

	@Override
	public int Stop() {

		int ret = Const.FAIL;

		return ret;
	}

	private int Init() {
		// int ret = Const.FAIL;

		_spout = new BundleRequest();

		_queue = new StateQueueThread(_spout);

		_queue.Start();

		return Const.SUCCESS;
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

		Log4j.log("state bundle " + msg.toString());

		_queue.Push(msg);

		return Const.SUCCESS;
	}

	public int Response(Client client) {

		int ret = Const.FAIL;

		JSONObject jobj = client.GetBundleInfoJSON();
		String state = jobj.getString(Const.BUNDLE_INFO_STATE);
		String bid = jobj.getString(Const.BUNDLE_INFO_BUNDLE_ID);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			// Registe bundle.
			if (0 == (bid.compareTo(BundleConf.STATE_BUNDLE_ID))) {
				_id = bid;
				_spout.SetClient(client);
				_client = client;

				Log4j.log("Bundle[" + _id + "] is actived.");
			} else {
				Log4j.error("Registe bundle[" + _id + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			String id = jobj.getString(Const.BUNDLE_INFO_ID);
			// Report msg.
			if (_res != null) {
				_res.Response(id, jobj.toString().getBytes());
			} else {
				Log4j.error("state file service bundle callback is null");
			}

			break;
		}
		default:
			Log4j.error("state file service bundle state[" + state + "] error");
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
		svr.SetPort(BundleConf.STATE_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}
}
