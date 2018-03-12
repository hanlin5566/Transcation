package com.wiitrans.opt.bundle;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.hbase.HbaseConfig;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.WiitransConfig;

public class Bundle extends Thread implements IBundle, IServer {

	private IResponse _res = null;
	private String _id = null;
	private BundleRequest _spout = null;
	private Client _client = null;

	private HashMap<String, HBaseIndex> _map = null;

	@Override
	public int NewClient(Client client) {
		return Response(client);
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

		return _spout.Start();
	}

	@Override
	public int Stop() {
		int ret = Const.FAIL;

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		// AppConfig app = new AppConfig();
		// ret = app.Parse(1);
		WiitransConfig.getInstance(1);

		_spout = new BundleRequest();

		if (_map == null) {
			_map = new HashMap<String, HBaseIndex>();
		}

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

	private int SendToPHP(JSONObject obj, String result) {
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		return _res.Response(id, resObj.toString().getBytes());
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
	public String GetBundleId() {
		return BundleConf.OPERATION_BUNDLE_ID;
	}

	private int InsertLog(JSONObject obj, String key) {
		int ret = Const.FAIL;

		SendToPHP(obj, "OK");

		HBaseIndex hbaseindex = null;

		if (_map.containsKey(key)) {
			hbaseindex = _map.get(key);
		} else {
			hbaseindex = new HBaseIndex();
			hbaseindex.table_id = 1;
			hbaseindex.rowcount = 0;
			_map.put(key, hbaseindex);
		}
		obj.put("table_id", String.valueOf(hbaseindex.table_id));
		obj.put("rowcount", String.valueOf(hbaseindex.rowcount));

		ret = Const.SUCCESS;

		return ret;
	}

	private int SearchLog(JSONObject obj, String key) {
		int ret = Const.FAIL;

		HBaseIndex hbaseindex = null;

		if (_map.containsKey(key)) {
			hbaseindex = _map.get(key);
			obj.put("table_id", String.valueOf(hbaseindex.table_id));
			obj.put("rowcount", String.valueOf(hbaseindex.rowcount));
			ret = Const.SUCCESS;
		}

		return ret;
	}

	@Override
	public int Request(JSONObject msg) {

		Log4j.log("opera bundle " + msg.toString());

		String uid = Util.GetStringFromJSon("uid", msg);
		String sid = Util.GetStringFromJSon("sid", msg);

		int ret = Const.FAIL;

		if (uid != null && uid.length() != 0 && sid != null
				&& sid.length() != 0) {

			String aid = Util.GetStringFromJSon("aid", msg);
			String method = Util.GetStringFromJSon("method", msg);

			switch (aid) {
			case "adminorder": {
				switch (method) {
				case "POST": {
					ret = InsertLog(msg,
							HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX);
					break;
				}
				case "GET": {
					ret = SearchLog(msg,
							HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "adminuser": {
				switch (method) {
				case "POST": {
					ret = InsertLog(msg, HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX);
					break;
				}
				case "GET": {
					ret = SearchLog(msg, HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "adminfinance": {
				switch (method) {
				case "POST": {
					ret = InsertLog(msg,
							HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX);
					break;
				}
				case "GET": {
					ret = SearchLog(msg,
							HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "adminoperate": {
				switch (method) {
				case "POST": {
					ret = InsertLog(msg,
							HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX);
					break;
				}
				case "GET": {
					ret = SearchLog(msg,
							HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "adminsystem": {
				switch (method) {
				case "POST": {
					ret = InsertLog(msg,
							HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX);
					break;
				}
				case "GET": {
					ret = SearchLog(msg,
							HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "gradetest": {
				switch (method) {
				case "POST": {
					ret = Const.SUCCESS;
					break;
				}
				case "GET": {
					ret = Const.SUCCESS;
					break;
				}
				case "PUT": {
					ret = Const.SUCCESS;
					break;
				}
				default:
					break;
				}
				break;
			}
			case "ordercycle": {
				switch (method) {
				case "POST": {
					SendToPHP(msg, "OK");
					ret = Const.SUCCESS;
					break;
				}
				case "GET": {
					ret = Const.SUCCESS;
					break;
				}
				// case "PUT": {
				// ret = Const.SUCCESS;
				// break;
				// }
				default:
					break;
				}
				break;
			}
			case "useraction": {
				switch (method) {
				case "POST": {
					SendToPHP(msg, "OK");
					ret = Const.SUCCESS;
					break;
				}
				case "GET": {
					ret = Const.SUCCESS;
					break;
				}
				default:
					break;
				}
				break;
			}
			case "refresh": {
				switch (method) {
				case "POST": {
					LogStat();
					SendToPHP(msg, "OK");
					ret = Const.SUCCESS;
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

		}

		if (ret == Const.SUCCESS) {
			return Request(msg.toString());
		} else {
			return Invalid(msg.toString());
		}
	}

	private void LogStat() {
		JSONObject req = new JSONObject();
		req.put("aid", "logstat");
		// req.put("id", "asdfasdf");
		req.put("bid", BundleConf.OPERATION_BUNDLE_ID);
		req.put("method", "POST");
		req.put("sid", "sid");
		req.put("uid", "uid");
		req.put("nid", String.valueOf(BundleConf.DEFAULT_NID));
		// Request(req);
		_spout.Push(req.toString());
	}

	public int Response(Client client) {

		int ret = Const.FAIL;

		JSONObject jobj = client.GetBundleInfoJSON();
		String state = jobj.getString(Const.BUNDLE_INFO_STATE);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			String bid = jobj.getString(Const.BUNDLE_INFO_BUNDLE_ID);
			// Registe bundle.
			if (0 == (bid.compareTo(BundleConf.OPERATION_BUNDLE_ID))) {
				_id = bid;
				_spout.SetClient(client);
				_client = client;

				Log4j.log("Bundle[" + _id + "] is actived.");

				LogStat();

				break;

			} else {
				Log4j.error("Registe bundle[" + _id + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {

			// Report msg.
			if (_res != null) {
				String result = Util.GetStringFromJSon("result", jobj);
				String operate = Util.GetStringFromJSon("operate", jobj);
				if (result != null && result.equals("OK")) {
					if (operate != null) {
						switch (operate) {
						case "initlog": {
							HBaseIndex orderhbaseindex = this
									.GetHBaseIndexFromJSON(
											jobj,
											HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX);
							if (orderhbaseindex != null) {
								_map.put(
										HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX,
										orderhbaseindex);
							}
							HBaseIndex userhbaseindex = this
									.GetHBaseIndexFromJSON(
											jobj,
											HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX);
							if (userhbaseindex != null) {
								_map.put(
										HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX,
										userhbaseindex);
							}
							HBaseIndex financehbaseindex = this
									.GetHBaseIndexFromJSON(
											jobj,
											HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX);
							if (financehbaseindex != null) {
								_map.put(
										HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX,
										financehbaseindex);
							}
							HBaseIndex operatehbaseindex = this
									.GetHBaseIndexFromJSON(
											jobj,
											HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX);
							if (operatehbaseindex != null) {
								_map.put(
										HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX,
										operatehbaseindex);
							}
							HBaseIndex systemhbaseindex = this
									.GetHBaseIndexFromJSON(
											jobj,
											HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX);
							if (systemhbaseindex != null) {
								_map.put(
										HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX,
										systemhbaseindex);
							}

							return Const.SUCCESS;
							// break;
						}
						case "addadminorderlog": {
							AddLog(HbaseConfig.HBASE_ADMIN_ORDER_LOG_INDEX);
							break;
						}
						case "addadminuserlog": {
							AddLog(HbaseConfig.HBASE_ADMIN_USER_LOG_INDEX);
							break;
						}
						case "addadminfinancelog": {
							AddLog(HbaseConfig.HBASE_ADMIN_FINANCE_LOG_INDEX);
							break;
						}
						case "addadminoperatelog": {
							AddLog(HbaseConfig.HBASE_ADMIN_OPERATE_LOG_INDEX);
							break;
						}
						case "addadminsystemlog": {
							AddLog(HbaseConfig.HBASE_ADMIN_SYSTEM_LOG_INDEX);
							break;
						}
						default:

							break;
						}
					}
				}
				String id = jobj.getString(Const.BUNDLE_INFO_ID);
				_res.Response(id, jobj.toString().getBytes());
			} else {
				Log4j.error("opera file service bundle callback is null");
			}

			break;
		}
		default:
			Log4j.error("operat file service bundle state[" + state + "] error");
			break;
		}

		return ret;
	}

	public void run() {
		BaseServer svr = new BaseServer();
		svr.SetPort(BundleConf.OPERATION_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}

	private HBaseIndex GetHBaseIndexFromJSON(JSONObject obj, String key) {
		HBaseIndex hbaseindex = null;
		JSONObject logstat = Util.GetJSonFromJSon(key, obj);
		if (logstat != null) {
			hbaseindex = new HBaseIndex();
			hbaseindex.allrowcount = Util
					.GetIntFromJSon("allrowcount", logstat);
			hbaseindex.rowcount = Util.GetIntFromJSon("rowcount", logstat);
			hbaseindex.startrowkey = Util.GetStringFromJSon("startrowkey",
					logstat);
			hbaseindex.endrowkey = Util.GetStringFromJSon("endrowkey", logstat);
			hbaseindex.table_id = Util.GetIntFromJSon("table_id", logstat);
		}
		return hbaseindex;
	}

	private void AddLog(String key) {
		HBaseIndex hbaseindex = null;
		if (_map.containsKey(key)) {
			hbaseindex = _map.get(key);
			if (hbaseindex.rowcount >= 1000 && hbaseindex.table_id >= 1) {
				hbaseindex.table_id = hbaseindex.table_id + 1;
				hbaseindex.rowcount = 1;
			} else {
				hbaseindex.rowcount = hbaseindex.rowcount + 1;
			}
			hbaseindex.allrowcount = hbaseindex.allrowcount + 1;
			_map.put(key, hbaseindex);
		} else {
			hbaseindex = new HBaseIndex();
			hbaseindex.table_id = 1;
			hbaseindex.rowcount = 1;
			hbaseindex.allrowcount = 1;
			_map.put(key, hbaseindex);
		}
	}

	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
		return Const.NOT_IMPLEMENTED;
	}
}
