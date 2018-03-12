package com.wiitrans.frag.bundle;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class FragPersistence extends Thread {

	private LinkedBlockingQueue<String> _queue = new LinkedBlockingQueue<String>();
	private BundleRequest _spout = null;

	public FragPersistence(BundleRequest spout) {
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

	public void Push(String fid) {
		try {
			_queue.put(fid);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}
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

	public int Request(String fid) {

		JSONObject obj = new JSONObject();
		// 发送文件保存到fdfs消息。

		return _spout.Push(obj.toString());
	}

	public void run() {
		while (true) {
			try {
				// 600s
				sleep(600000);

				HashSet<String> files = new HashSet<String>();
				while (!_queue.isEmpty()) {
					files.add(_queue.poll());
					sleep(5);
				}

				for (String fid : files) {
					Request(fid);
					sleep(10000); // 此处为了保证不引起消息风暴。
				}

			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			}
		}
	}
}
