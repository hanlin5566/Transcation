package com.wiitrans.tmsvr.bundle;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.db.TMDAO;
import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

import org.json.JSONObject;

public class Bundle extends Thread implements IBundle, IServer {

	public class TMClient {
		public Client _client = null;
		public String _id = null;
		public BundleRequest _spout = null;
	}

	private TMAddQueueThread _queue = null;

	// 回复到PHP
	private IResponse _res = null;

	// 对应一个TM-Java实例（进程）
	private ConcurrentHashMap<String /* TM id */, TMClient> _tmClient = null;

	private String _command = null;

	// private Client _client = null;
	// private String _id = null;
	// private BundleRequest _spout = null;

	@Override
	public String GetBundleId() {

		return BundleConf.TMSVR_BUNDLE_ID;
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

		BundleParam param = WiitransConfig.getInstance(1).TMSVR;
		_command = param.TMSVR_COMMAND;
		Log4j.log("          tmsvr-command  = " + _command);

		_queue = new TMAddQueueThread();
		_queue.Start();

		return ret;
	}

	@Override
	public int Stop() {

		int ret = Const.FAIL;

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		_tmClient = new ConcurrentHashMap<String, TMClient>();
		ret = Const.SUCCESS;
		return ret;
	}

	private int SendToPHP(JSONObject obj, String result) {
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		_res.Response(id, resObj.toString().getBytes());
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

		// Nothing to do.

		// if (_client != null) {
		// ret = _spout.Push(msg);
		// } else {
		// ret = Invalid(msg);
		// }
		return ret;
	}

	private int DropIfDeletable(int nid, int tmID, JSONObject msg) {
		int ret = Const.FAIL;

		if (tmID > 0) {
			TMServiceDAO tmservicedao = null;
			TMDAO dao = null;
			try {
				dao = new TMDAO();
				dao.Init(false);
				List<Integer> listint = dao.Deletable(new int[] { tmID });
				if (listint != null && listint.size() == 1) {
					SendToPHP(msg, "OK");
					if (_tmClient.containsKey(String.valueOf(tmID))) {
						TMClient tmClient = _tmClient.get(String.valueOf(tmID));
						if ((tmClient != null) && (tmClient._client != null)) {
							// 该tm进程已经启动直接发送命令参数即可
							JSONObject obj = new JSONObject(msg.toString());
							obj.put("tmid", String.valueOf(tmID));
							obj.put("aid", "analysis");
							obj.put("method", "DELETE");
							ret = tmClient._spout.Push(obj.toString());

							// 关闭需要5秒等待时间
							Thread.sleep(5000);
						}
					}
					tmservicedao = new TMServiceDAO();
					tmservicedao.Init();
					tmservicedao.DropTMIndexIfExists(tmID);

					// if (_lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
					tmservicedao.DropTMWordIfExists(tmID);

					tmservicedao.DropTMTimeIfExists(tmID);

					// }
					tmservicedao.DropTMTextIfExists(tmID);
					if (nid == BundleConf.DEFAULT_NID) {
						dao.Delete(tmID);
					} else {
						dao.DeleteForNode(tmID);
					}
					dao.Commit();
					ret = Const.SUCCESS;
				} else {
					String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID,
							msg);
					JSONObject resObj = new JSONObject();
					resObj.put("result", "FAILED");
					resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
					resObj.put(Const.BUNDLE_INFO_ID, id);
					resObj.put(Const.BUNDLE_INFO_ACTION_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID,
									msg));
					_res.Response(id, resObj.toString().getBytes());
				}

			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao != null) {
					dao.UnInit();
				}
				if (tmservicedao != null) {
					tmservicedao.UnInit();
				}
			}

		}
		return ret;
	}

	private int DeleteAllDeletable(JSONObject msg) {
		int ret = Const.FAIL;
		// TMClient tmClient = _tmClient.get(String.valueOf(tmid));
		ArrayList<Integer> tmIDList = new ArrayList<Integer>();
		int tmID;
		for (String tm_id : _tmClient.keySet()) {
			tmID = Util.String2Int(tm_id);
			if (tmID > 0) {
				tmIDList.add(tmID);
			}
		}
		if (tmIDList.size() > 0) {
			TMDAO dao = null;
			try {
				dao = new TMDAO();
				dao.Init(false);
				List<Integer> listint = dao.Deletable(Util
						.IntegerlistToAry(tmIDList));
				if (listint != null && listint.size() > 0) {
					for (Integer tmid : listint) {
						if (_tmClient.containsKey(String.valueOf(tmid))) {
							TMClient tmClient = _tmClient.get(String
									.valueOf(tmid));
							if ((tmClient != null)
									&& (tmClient._client != null)) {
								// 该tm进程已经启动直接发送命令参数即可
								JSONObject obj = new JSONObject(msg.toString());
								obj.put("tmid", String.valueOf(tmid));
								obj.put("aid", "analysis");
								ret = tmClient._spout.Push(obj.toString());
							}
						}
					}
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao != null) {
					dao.UnInit();
				}
			}
		}
		return ret;
	}

	@Override
	public int Request(JSONObject msg) {

		int ret = Const.FAIL;

		Log4j.log("tmsvr bundle " + msg.toString());

		String aid = Util.GetStringFromJSon("aid", msg);
		int tnid = Util.GetIntFromJSon("tnid", msg);

		if (tnid <= 0) {
			tnid = BundleConf.DEFAULT_NID;
		}
		String method = Util.GetStringFromJSon("method", msg);
		int tmid = Util.GetIntFromJSon("tmid", msg);
		if (tmid > 0) {
			TMClient tmClient = _tmClient.get(String.valueOf(tmid));
			switch (aid) {
			case "analysis": {
				switch (method) {
				case "POST": {
					// 分析这个tmsvr
					SendToPHP(msg, "OK");
					// Util.exeCmd("java -jar /opt/wiitrans/service/tmsvr.jar 1 "
					// + tmid + " POST");
					if ((tmClient == null) || (tmClient._client == null)) {
						String cmd = String
								.format(_command, tnid, tmid, "POST");
						Log4j.log("tmsvr bundle cmd:" + cmd);
						Util.exeCmd(cmd);
					}
					break;
				}
				case "GET": {
					// 查询
					if ((tmClient != null) && (tmClient._client != null)) {
						// 该tm进程已经启动直接发送命令参数即可
						ret = tmClient._spout.Push(msg.toString());
					} else {
						ret = Invalid(msg.toString());
					}
					break;
				}
				case "PUT": {
					// 打开tmsvr进程
					SendToPHP(msg, "OK");
					// Util.exeCmd("java -jar /opt/wiitrans/service/tmsvr.jar 1 "
					// + tmid + " PUT");
					if ((tmClient == null) || (tmClient._client == null)) {
						String cmd = String.format(_command, tnid, tmid, "PUT");
						Log4j.log("tmsvr bundle cmd:" + cmd);
						Util.exeCmd(cmd);
					}
					break;
				}
				case "DELETE": {
					// 直接注销，关闭该tmsvr的进程
					SendToPHP(msg, "OK");
					if ((tmClient != null) && (tmClient._client != null)) {
						ret = tmClient._spout.Push(msg.toString());
					} else {
						ret = Invalid(msg.toString());
					}
					break;
				}
				default:
					ret = Invalid(msg.toString());
					break;
				}
				break;
			}
			case "manage": {
				switch (method) {
				case "POST": {
					// TM可以注销则注销且删除
					this.DropIfDeletable(tnid, tmid, msg);
					break;
				}
				case "PUT": {
					// 更新TM
					SendToPHP(msg, "OK");
					_queue.Push(msg);
					break;
				}
				case "GET": {
					// 查询10条记录
					if ((tmClient != null) && (tmClient._client != null)) {
						// 该tm进程已经启动直接发送命令参数即可
						ret = tmClient._spout.Push(msg.toString());
					} else {
						ret = Invalid(msg.toString());
					}
					break;
				}
				case "DELETE": {
					// 注销所有可以注销的【没有进行中的订单使用】
					SendToPHP(msg, "OK");
					this.DeleteAllDeletable(msg);
					break;
				}
				default:
					ret = Invalid(msg.toString());
					break;
				}
				break;
			}
			default:
				ret = Invalid(msg.toString());
				break;
			}
		}

		return ret;

		// return Request(msg.toString());
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
			if (0 == (bid.compareTo(BundleConf.TMSVR_BUNDLE_ID))) {

				String tmid = Util.GetStringFromJSon("tmid", obj);// obj.getString("tmid");
				if (tmid == null) {
					Log4j.error("Registe bundle[" + bid + "] tmid is null.");
				} else {
					TMClient tmClient = new TMClient();
					tmClient._client = client;
					tmClient._id = bid;
					tmClient._spout = new BundleRequest();
					tmClient._spout.SetClient(client);
					tmClient._spout.start();

					if (_tmClient.containsKey(tmid)) {
						TMClient tmClientOld = _tmClient.get(tmid);
						if (tmClientOld != null) {
							tmClientOld._client.GetContext().close();
							tmClientOld._client = null;
							tmClientOld._id = null;
							tmClientOld._spout = null;
							tmClientOld = null;
						}
					}
					_tmClient.put(tmid, tmClient);

					Log4j.log("Bundle[" + bid + "][" + tmid + "] is actived.");
				}
			} else {
				Log4j.error("Registe bundle[" + bid + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			String result = Util.GetStringFromJSon("result", obj);
			if (result != null) {
				// 回复消息到PHP
				if (_res != null) {
					_res.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("tm service bundle callback is null");
				}
			} else {
				Log4j.error("The report msg result is null.");
			}
			break;
		}
		case Const.BUNDLE_UNREGISTER: {
			String bid = obj.getString(Const.BUNDLE_INFO_BUNDLE_ID);
			if (0 == (bid.compareTo(BundleConf.TMSVR_BUNDLE_ID))) {

				String tmid = Util.GetStringFromJSon("tmid", obj);

				if (tmid == null) {
					Log4j.error("Registe bundle[" + bid + "] tmid is null.");
				} else if (_tmClient.containsKey(tmid)) {
					TMClient tmClientOld = _tmClient.remove(tmid);
					tmClientOld._client.GetContext().close();
					tmClientOld._client = null;
					tmClientOld._id = null;
					tmClientOld._spout = null;
					tmClientOld = null;

					Log4j.log("Bundle[" + bid + "][" + tmid + "] is deactived.");
				} else {
					Log4j.log("Bundle[" + bid + "][" + tmid
							+ "] is not in tmclient map.");
				}
			} else {
				Log4j.error("UnRegiste bundle[" + bid + "] is mismatch.");
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
		svr.SetPort(BundleConf.TMSVR_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}
}
