package com.wiitrans.exam.bundle;

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
    private IResponse _res = null;
    private Client _client = null;
    private String _id = null;
    private BundleRequest _spout = null;

    @Override
    public int NewClient(Client client) {
	return Response(client);
    }

    @Override
    public int SetResponse(IResponse res) {
	int ret = Const.FAIL;

	_res = res;
	return ret;
    }

    private int Init() {
	int ret = Const.FAIL;
	_spout = new BundleRequest();
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

    private int Invalid(String exam) {
	int ret = Const.FAIL;

	JSONObject obj = new JSONObject(exam);
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
    public int Request(String exam) {

	int ret = Const.FAIL;
	if (_client != null) {
	    ret = _spout.Push(exam);
	} else {
	    ret = Invalid(exam);
	}
	return ret;
    }

    @Override
    public String GetBundleId() {
	return BundleConf.EXAM_BUNDLE_ID;
    }

    @Override
    public int Request(JSONObject exam) {
	Log4j.log("exam bundle " + exam.toString());
	return Request(exam.toString());
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
	    if (0 == (bid.compareTo(BundleConf.EXAM_BUNDLE_ID))) {
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
			Log4j.error("exam service bundle callback is null");
		    }
		    break;
		}
		}
	    } else {
		Log4j.error("The report exam result is null.");
	    }
	    break;
	}
	default:
	    Log4j.error("exam service bundle state[" + state + "] error");
	    break;
	}

	return ret;
    }

    public void run() {
	BaseServer svr = new BaseServer();
	svr.SetPort(BundleConf.EXAM_BUNDLE_PORT);
	svr.SetNewClientCallBack(this);
	svr.Run(false);
    }

    @Override
    public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
	return Const.NOT_IMPLEMENTED;
    }
}
