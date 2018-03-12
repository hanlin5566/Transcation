package com.wiitrans.oc.bundle;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.IBundle;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.RecomOrderDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.RecomFileBean;
import com.wiitrans.base.db.model.RecomOrderBean;
import com.wiitrans.base.interproc.BaseServer;
import com.wiitrans.base.interproc.Client;
import com.wiitrans.base.interproc.IServer;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.WiitransConfig;

public class Bundle extends Thread implements IBundle, IServer {

	private IResponse _res = null;
	private String _bid = null;
	private BundleRequest _spout = null;
	private Client _client = null;
	private RedisCache _cache = null;

	private List<OrderBean> _orderList = null;

	@Override
	public String GetBundleId() {
		return BundleConf.ORDERCENTER_BUNDLE_ID;
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

		_spout = new BundleRequest();
		if (_cache == null) {
			_cache = new RedisCache();
			// 配置统一
			ret = _cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}
		ret = InitOrdersCache();

		return ret;
	}

	// 初始化所有进行中订单信息
	private int InitOrdersCache() {
		int ret = Const.FAIL;
		List<OrderBean> list = null;
		if (_orderList == null) {
			OrderDAO orderdao = null;
			list = new ArrayList<OrderBean>();

			WiitransConfig.getInstance(1);
			Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
			// for (int nid : set) {
			int nid = BundleConf.DEFAULT_NID;
			JSONObject json = new JSONObject();
			json.put("nid", String.valueOf(nid));

			try {
				orderdao = new OrderDAO();
				orderdao.Init(false, json);
				List<OrderBean> orderListForNode = orderdao
						.SelectOrdersByStatus();
				for (OrderBean orderbean : orderListForNode) {
					// if (orderbean.order_id != 1825) {
					// continue;
					// }
					orderbean.node_id = nid;
					list.add(orderbean);
				}
				// 查询已经被抢的进行中订单，挂到myorderlist
				List<OrderBean> reservedOrderList = orderdao
						.SelectReservedOrder();
				for (OrderBean orderbean : reservedOrderList) {
					String orderCode = orderbean.code;
					if (orderbean.translator_id != 0) {
						String key = Util
								.GetMyOrderListKey(orderbean.translator_id);
						_cache.sadd(key, orderCode);
					}
					if (orderbean.editor_id != 0) {
						String key = Util
								.GetMyOrderListKey(orderbean.editor_id);
						_cache.sadd(key, orderCode);
					}
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (orderdao != null) {
					orderdao.UnInit();
				}
			}
		} else {
			list = _orderList;
		}
		if (_client == null) {
			_orderList = list;
		} else {
			if (list != null) {
				RecomOrderDAO recomorderdao = null;
				try {
					recomorderdao = new RecomOrderDAO();
					recomorderdao.Init(false);

					for (OrderBean orderBean : list) {
						InitOrderCache(orderBean.node_id, orderBean.code,
								recomorderdao);
						if (orderBean.match_type < 99) {
							JSONObject obj = new JSONObject();
							// 模拟请求
							obj.put("aid", "order");
							obj.put("method", "POST");
							obj.put("uid",
									String.valueOf(orderBean.customer_id));
							obj.put("nid", String.valueOf(orderBean.node_id));
							// obj.put("match_type",
							// String.valueOf(orderBean.match_type));
							obj.put("ordercode", orderBean.code);
							_spout.Push(obj.toString());
						}
					}
				} catch (Exception e) {
					Log4j.error(e);
				} finally {
					if (recomorderdao != null) {
						recomorderdao.UnInit();
					}
				}
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}

	// 加载订单信息到redis
	private int InitOrderCache(int nid, String ordercode, RecomOrderDAO dao) {
		int ret = Const.FAIL;

		if (nid > 0 && ordercode != null) {
			String orderkey = "order_" + ordercode;
			// String sOrder = _cache.GetString(nid, orderkey);
			JSONObject jsOrder, jsFile;

			RecomOrderDAO recomorderdao = null;
			try {
				if (dao == null) {
					JSONObject json = new JSONObject();
					json.put("nid", String.valueOf(nid));
					recomorderdao = new RecomOrderDAO();
					recomorderdao.Init(false, json);
				} else {
					recomorderdao = dao;
				}
				RecomOrderBean recomorder = recomorderdao.Select(ordercode);
				if (recomorder != null && recomorder.order_id > 0) {
					jsOrder = new JSONObject();
					jsOrder.put("order_id", String.valueOf(recomorder.order_id));
					jsOrder.put("onid", String.valueOf(nid));
					jsOrder.put("code", recomorder.code);
					jsOrder.put("translator_id", recomorder.translator_id);
					jsOrder.put("editor_id", recomorder.editor_id);
					jsOrder.put("pair_id", recomorder.pair_id);
					jsOrder.put("price_level_id", recomorder.price_level_id);
					jsOrder.put("level", recomorder.level);
					jsOrder.put("sname", recomorder.sname);
					jsOrder.put("tname", recomorder.tname);
					jsOrder.put("industry", recomorder.industry);
					jsOrder.put("industry_cname", recomorder.industry_cname);
					jsOrder.put("industry_ename", recomorder.industry_ename);
					jsOrder.put("industry_id", recomorder.industry_id);
					jsOrder.put("word_count", recomorder.word_count);
					jsOrder.put("analyse_word_count",
							recomorder.analyse_word_count);
					jsOrder.put("currency_id", recomorder.currency_id);
					jsOrder.put("delivery_time", recomorder.delivery_time);
					jsOrder.put("delivery_time_t", recomorder.delivery_time_t);
					jsOrder.put("delivery_time_e", recomorder.delivery_time_e);
					jsOrder.put("need_time", recomorder.need_time);
					jsOrder.put("money_t", recomorder.money_t);
					jsOrder.put("money_e", recomorder.money_e);
					jsOrder.put("description", recomorder.description);
					jsOrder.put("match_type", recomorder.match_type);
					jsOrder.put("change_type", recomorder.change_type);
					jsOrder.put("custo_rate", recomorder.custo_rate);
					// jsOrder.put("recom_t", recomorder.recom_t);
					// jsOrder.put("recom_e", recomorder.recom_e);
					int match_type = Util.String2Int(recomorder.match_type);
					if (match_type == 4) {
						String recom_t = recomorderdao
								.SelectRecomT(recomorder.order_id);
						jsOrder.put("recom_t", recom_t);
					} else if (match_type == 6) {
						String recom_t = recomorderdao
								.SelectRecomT(recomorder.order_id);
						jsOrder.put("recom_t", recom_t);
						String recom_e = recomorderdao
								.SelectRecomE(recomorder.order_id);
						jsOrder.put("recom_e", recom_e);
					} else if (match_type == 7) {
						String team = recomorderdao.SelectTeam(
								Util.String2Int(recomorder.customer_id),
								Util.String2Int(recomorder.pair_id));
						jsOrder.put("team", team);
					}

					JSONObject jsFiles = new JSONObject();
					List<RecomFileBean> recomfileList = recomorderdao
							.SelectFiles(recomorder.order_id);
					if (dao == null) {
						recomorderdao.UnInit();
					}

					RecomFileBean file;
					for (int i = 0; i < recomfileList.size(); i++) {
						jsFile = new JSONObject();
						file = recomfileList.get(i);
						jsFile.put("order_id", file.order_id);
						jsFile.put("name", file.name);
						jsFile.put("word_count", file.word_count);
						jsFile.put("analyse_word_count",
								file.analyse_word_count);
						jsFile.put("preview", file.preview);
						jsFiles.put(String.valueOf(file.file_id), jsFile);
					}

					jsOrder.put("file", jsFiles);

					_cache.DelString(nid, orderkey);
					_cache.SetString(nid, orderkey, jsOrder.toString());
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao == null && recomorderdao != null) {
					recomorderdao.UnInit();
				}
			}
			// }
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
	public int Request(JSONObject msg) {

		Log4j.log("oc bundle " + msg.toString());

		String aid = Util.GetStringFromJSon("aid", msg);
		String method = Util.GetStringFromJSon("method", msg);
		int nid = Util.GetIntFromJSon("nid", msg);
		String ordercode = Util.GetStringFromJSon("ordercode", msg);
		switch (aid) {
		case "order": {
			switch (method) {
			case "GET": {
				// GetOrders(msg);
				break;
			}
			case "POST": {
				// 加载订单信息到redis
				InitOrderCache(nid, ordercode, null);
				break;
			}
			case "DELETE": {
				// DeleteOrders(msg);
				// 临时添加，防止delete方法不返回结果，以后需要添加上订单删除操作
				// SendToPHP(msg, "FAILED");
				break;
			}
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
		String state = Util.GetStringFromJSon(Const.BUNDLE_INFO_STATE, obj);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		String aid = Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			// Registe bundle.
			String bid = Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID,
					obj);
			if (0 == (bid.compareTo(BundleConf.ORDERCENTER_BUNDLE_ID))) {
				_bid = bid;
				_spout.SetClient(client);
				_client = client;

				InitOrdersCache();

				Log4j.log("Bundle[" + _bid + "] is actived.");

			} else {
				Log4j.error("Registe bundle[" + _bid + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			if (id == null) {
				return ret;
			}
			switch (aid) {
			case "order": {
				if (_res != null) {
					_res.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("oc service bundle callback is null");
				}

				break;
			}

			default:
				ret = Const.SUCCESS;
				if (_res != null) {
					_res.Response(id, obj.toString().getBytes());
				} else {
					Log4j.error("oc service bundle callback is null");
				}
				break;
			}

			break;
		}
		default:
			Log4j.error("oc service bundle state[" + state + "] error");

			if (_res != null) {
				_res.Response(id, obj.toString().getBytes());
			}
			break;
		}

		return ret;
	}

	@Override
	public int NewClient(Client client) {

		return Response(client);
	}

	public void run() {
		BaseServer svr = new BaseServer();
		svr.SetPort(BundleConf.ORDERCENTER_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}

	@Override
	public int SetContent(String clientId, ByteBuf content, boolean isSplit) {
		return Const.NOT_IMPLEMENTED;
	}
}
