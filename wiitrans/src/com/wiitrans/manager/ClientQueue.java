/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.manager;

import java.util.concurrent.LinkedBlockingQueue;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ClientQueue {
	
	private LinkedBlockingQueue<ResponseMeta> _queue = new LinkedBlockingQueue<ResponseMeta>();

	// Meta data.
	public class ResponseMeta {

		public String _clientId = null;
		public byte[] _res = null;

		public ResponseMeta(String clientId, byte[] res) {
			_clientId = clientId;
			_res = res;
		}
	}
	public int getSize(){
	   return _queue.size();
	}
	public int Push(String clientId, byte[] msg) {
		
		int ret = Const.FAIL;
		
		try {
			_queue.put(new ResponseMeta(clientId, msg));			
			ret = Const.SUCCESS;
			
		} catch (InterruptedException e) {
			Log4j.error(e);
		}
		
		return ret;
	}

	public ResponseMeta Pop() {
		
		ResponseMeta res = null;
		
		try {
			res = _queue.take();
			
		} catch (InterruptedException e) {
			Log4j.error(e);
		}
		
		return res;
	}
}
