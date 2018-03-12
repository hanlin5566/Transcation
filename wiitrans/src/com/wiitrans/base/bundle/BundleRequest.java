/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.bundle;

import java.util.concurrent.LinkedBlockingQueue;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class BundleRequest extends Thread {

	private LinkedBlockingQueue<String> _queue = new LinkedBlockingQueue<String>();
	private Client _client = null;

	public int SetClient(Client client) {
		int ret = Const.FAIL;

		if (_client != null) {
			_client.GetContext().close();
		}

		_client = client;
		ret = Const.SUCCESS;

		return ret;
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

	public int Push(String req) {
		int ret = Const.FAIL;

		try {

			if (req == null) {
				Log4j.error("BundleRequest.Push()  req is null. ");
			} else {
				_queue.put(req);
			}
			ret = Const.SUCCESS;

		} catch (InterruptedException e) {
			Log4j.error(e);
		}

		return ret;
	}

	public String Pop() {
		String req = null;

		try {
			req = _queue.take();

		} catch (InterruptedException e) {
			Log4j.error(e);
		}

		return req;
	}

	private int Request() {
		int ret = Const.FAIL;

		String msg = Pop();

		if (_client != null) {
			_client.Request(msg.getBytes());
		} else {
			Log4j.error("_client is null.");
		}

		return ret;
	}

	public void run() {
		while (true) {
			Request();
		}
	}
}
