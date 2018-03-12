package com.wiitrans.recom.bundle;

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
import com.wiitrans.base.db.DictIndustryDAO;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.RecomOrderDAO;
import com.wiitrans.base.db.model.DictIndustryBean;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.RecomFileBean;
import com.wiitrans.base.db.model.RecomOrderBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import com.wiitrans.base.interproc.*;

public class Bundle extends Thread implements IBundle, IServer {

	private IResponse _res = null;
	private Client _client = null;
	private String _id = null;
	private BundleRequest _spout = null;
	private ICache _cache = null;
	private ReMatching _rematching = null;

	private List<OrderBean> _orderList = null;
	private int othersIndustryID = 11;

	@Override
	public String GetBundleId() {

		return BundleConf.RECOMMEND_BUNDLE_ID;
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

	private int InitOthersIndustryID() {
		int ret = Const.FAIL;
		DictIndustryDAO dao = null;
		try {
			dao = new DictIndustryDAO();
			dao.Init(false);
			DictIndustryBean bean = dao.SelectOthers();
			if (bean != null) {
				othersIndustryID = bean.industry_id;
				ret = Const.SUCCESS;
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int InitOnlineTranslator() {
		int ret = Const.FAIL;

		ret = Const.SUCCESS;

		return ret;
	}

	private int InitOrder() {
		int ret = Const.FAIL;
		List<OrderBean> list = null;
		if (_orderList == null) {
			OrderDAO orderdao = null;
			list = new ArrayList<OrderBean>();

			// AppConfig app = new AppConfig();
			// app.Parse(1);
			WiitransConfig.getInstance(1);
			Set<Integer> set = BundleConf.BUNDLE_Node.keySet();
			for (int nid : set) {
				JSONObject json = new JSONObject();
				json.put("nid", String.valueOf(nid));

				try {
					orderdao = new OrderDAO();
					orderdao.Init(false, json);
					for (OrderBean orderbean : orderdao.SelectOrdersByStatus()) {
						// if (orderbean.order_id != 1475) {
						// continue;
						// }
						orderbean.node_id = nid;
						list.add(orderbean);
					}

				} catch (Exception e) {
					Log4j.error(e);
				} finally {
					if (orderdao != null) {
						orderdao.UnInit();
					}
				}
			}
		} else {
			list = _orderList;
		}

		if (_client == null) {
			_orderList = list;
		} else {
			if (list != null) {
				// String nid = "1";
				for (OrderBean orderBean : list) {
					JSONObject obj = new JSONObject();
					obj.put("aid", "order");
					obj.put("method", "POST");
					obj.put("uid", String.valueOf(orderBean.customer_id));
					obj.put("nid", String.valueOf(orderBean.node_id));
					if (orderBean.price_level_id == 1) {
						obj.put("type", "normal");
					} else {
						obj.put("type", "vip");
					}
					obj.put("price_level",
							String.valueOf(orderBean.price_level_id));
					obj.put("ordercode", orderBean.code);
					obj.put("langpair", String.valueOf(orderBean.pair_id));
					obj.put("industry_id",
							String.valueOf(orderBean.industry_id));
					if (orderBean.translator_id > 0) {
						obj.put("translator",
								String.valueOf(orderBean.translator_id));
					}
					if (orderBean.editor_id > 0) {
						obj.put("editor", String.valueOf(orderBean.editor_id));
					}

					obj.put("match_type", String.valueOf(orderBean.match_type));
					if (orderBean.match_type >= 4) {
						obj.put("recom_t", orderBean.recom_t);
						if (orderBean.match_type == 6) {
							obj.put("recom_e", orderBean.recom_e);
						}
					}

					// 未抢单订单，storm启动时清空一次order缓存
					String orderkey = "order_" + orderBean.code;
					_cache.DelString(orderBean.node_id, orderkey);

					PostOrders(obj);
				}
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int Init() {
		int ret = Const.FAIL;

		_spout = new BundleRequest();
		_rematching = new ReMatching(_spout, this);
		// 改为storm注册后启动线程,并且线程先执行后休眠
		// _rematching.start();
		ret = InitOrder();
		if (Const.SUCCESS == ret) {
			ret = InitOthersIndustryID();
		}
		if (Const.SUCCESS == ret) {
			ret = InitOnlineTranslator();
		}
		if (Const.SUCCESS == ret) {
			if (_cache == null) {
				_cache = new RedisCache();
				// 配置统一
				ret = _cache.Init(BundleConf.BUNDLE_REDIS_IP);
			}
		}

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

	public int Login(JSONObject obj) {
		int ret = Const.FAIL;

		int uid = Util.GetIntFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		String userkey = "uid_" + uid;
		String userInfoString = _cache.GetString(nid, userkey);

		if (userInfoString != null) {
			JSONObject userInfo = new JSONObject(userInfoString);
			String langpair = "";
			JSONObject langpair_industry = new JSONObject();
			JSONObject langpair_industry_grade_editor = new JSONObject();
			int role_id = Util.GetIntFromJSon("role_id", obj);
			if (role_id == 2) {
				JSONObject langpairs = Util.GetJSonFromJSon("langpairs",
						userInfo);

				if (langpairs != null) {
					String[] names = JSONObject.getNames(langpairs);
					if (names != null && names.length > 0) {
						for (String name : names) {
							langpair += name + "|";

							JSONObject langpairJSON = Util.GetJSonFromJSon(
									name, langpairs);

							String industry_ids = Util.GetStringFromJSon(
									"industry_ids", langpairJSON);
							if (industry_ids != null
									&& industry_ids.length() > 0) {
								// langpair_industry.put(name, industry_ids);
								langpair_industry.put(name, industry_ids + "-"
										+ othersIndustryID);
							}
							String industry_grade_editor = Util
									.GetStringFromJSon("industry_grade_editor",
											langpairJSON);
							String grade_id = Util.GetStringFromJSon(
									"grade_id", langpairJSON);
							boolean editor = Util.String2Bool(Util
									.GetStringFromJSon("editor", langpairJSON));
							if (industry_grade_editor != null
									&& industry_grade_editor.length() > 0) {
								// langpair_industry.put(name, industry_ids);
								langpair_industry_grade_editor.put(name,
										industry_grade_editor + ","
												+ othersIndustryID + "-"
												+ grade_id + "-"
												+ (editor ? "1" : "0"));
							}
						}

					}
				}

				if (langpair.length() > 0) {
					langpair = langpair.substring(0, langpair.length() - 1);
				}
			}
			obj.put("langpair", langpair);
			obj.put("langpair_industry", langpair_industry);
			obj.put("langpair_industry_grade_editor",
					langpair_industry_grade_editor);
			obj.put("my", Util.GetJSonFromJSon("my", userInfo));
			obj.put("nt", Util.GetJSonFromJSon("nt", userInfo));
			obj.put("vt", Util.GetJSonFromJSon("vt", userInfo));
			obj.put("ve", Util.GetJSonFromJSon("ve", userInfo));
			// obj.put("rt", Util.GetJSonFromJSon("rt", userInfo));
			// obj.put("rte", Util.GetJSonFromJSon("rte", userInfo));
		}
		return ret;
	}

	public int Logout(JSONObject obj) {
		int ret = Const.FAIL;
		int uid = Util.GetIntFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		String userkey = "uid_" + uid;
		String userInfoString = _cache.GetString(nid, userkey);

		if (userInfoString != null) {
			JSONObject userInfo = new JSONObject(userInfoString);
			String langpair = "";
			JSONObject langpair_industry = new JSONObject();
			int role_id = Util.GetIntFromJSon("role_id", obj);
			if (role_id == 2) {
				JSONObject langpairs = Util.GetJSonFromJSon("langpairs",
						userInfo);

				if (langpairs != null) {
					String[] names = JSONObject.getNames(langpairs);
					if (names != null) {
						for (String name : names) {
							langpair += name + "|";

							JSONObject langpairJSON = Util.GetJSonFromJSon(
									name, langpairs);

							String industry_ids = Util.GetStringFromJSon(
									"industry_ids", langpairJSON);
							if (industry_ids != null) {
								langpair_industry.put(name, industry_ids);
							}
						}
					}
				}

				if (langpair.length() > 0) {
					langpair = langpair.substring(0, langpair.length() - 1);
				}
			}

			obj.put("langpair", langpair);
			obj.put("langpair_industry", langpair_industry);
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int GetOrders(JSONObject obj) {
		int ret = Const.FAIL;
		obj.put("langpair", "-1");
		ret = Const.SUCCESS;

		return ret;
	}

	private int PostOrders(JSONObject obj) {
		int ret = Const.FAIL;

		String type = Util.GetStringFromJSon("type", obj);
		String ordercode = Util.GetStringFromJSon("ordercode", obj);
		int nid = Util.GetIntFromJSon("nid", obj);

		if (type != null && ordercode != null) {
			String orderkey = "order_" + ordercode;

			// String sOrder = _cache.GetString(nid, orderkey);
			JSONObject jsOrder, jsFile;
			// if (sOrder == null || sOrder.trim().length() == 0) {

			RecomOrderDAO recomorderdao = null;
			try {
				JSONObject json = new JSONObject();
				json.put("nid", String.valueOf(nid));
				recomorderdao = new RecomOrderDAO();
				recomorderdao.Init(false, json);
				RecomOrderBean recomorder = recomorderdao.Select(ordercode);

				if (recomorder != null && recomorder.order_id > 0) {
					jsOrder = new JSONObject();
					jsOrder.put("order_id", recomorder.order_id);
					jsOrder.put("code", recomorder.code);
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
					obj.put("match_type", String.valueOf(recomorder.match_type));
					jsOrder.put("recom_t", recomorder.recom_t);
					obj.put("recom_t", recomorder.recom_t);
					jsOrder.put("recom_e", recomorder.recom_e);
					obj.put("recom_e", recomorder.recom_e);
					JSONObject jsFiles = new JSONObject();
					List<RecomFileBean> recomfileList = recomorderdao
							.SelectFiles(recomorder.order_id);
					recomorderdao.UnInit();

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
				// e.printStackTrace();
				Log4j.error(e);
			} finally {
				if (recomorderdao != null) {
					recomorderdao.UnInit();
				}
			}
			// }

			// 添加尚未完成的订单到ReMatching

			_rematching.Put(ordercode, obj);
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int DeleteOrders(JSONObject obj) {
		int ret = Const.FAIL;

		// String type = Util.GetStringFromJSon("type", obj);
		String ordercode = Util.GetStringFromJSon("ordercode", obj);
		String complete = Util.GetStringFromJSon("complete", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		if (ordercode != null && complete != null
				&& complete.equalsIgnoreCase("complete")) {
			String orderkey = "order_" + ordercode;
			String sOrder = _cache.GetString(nid, orderkey);

			if (sOrder != null && sOrder.trim().length() > 0) {
				_cache.DelString(nid, orderkey);
			}

			// 添加尚未完成的订单到ReMatching

			_rematching.Remove(orderkey);
		}

		ret = Const.SUCCESS;

		return ret;
	}

	@Override
	public int Request(JSONObject msg) {

		Log4j.log("recom bundle " + msg.toString());

		// 目前考虑只通知PHP有新订单，暂时不退送新订单内容，PHP通过Bundle请求来获取当前译员最新的抢单列表，未来可以根据实际运营状况进行调整
		String aid = Util.GetStringFromJSon("aid", msg);
		String method = Util.GetStringFromJSon("method", msg);
		switch (aid) {
		case "user": {
			switch (method) {
			case "POST": {
				Login(msg);
				break;
			}
			case "DELETE": {
				Logout(msg);
				break;
			}
			default:
				break;
			}

			break;
		}
		case "order": {
			switch (method) {
			case "GET": {
				GetOrders(msg);
				break;
			}
			case "POST": {
				// 订单对应redis数据，并添加recom_t recom_e参数
				PostOrders(msg);
				break;
			}
			case "DELETE": {
				DeleteOrders(msg);
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
		String state = obj.getString(Const.BUNDLE_INFO_STATE);
		String id = obj.getString(Const.BUNDLE_INFO_ID);

		switch (state) {
		case Const.BUNDLE_REGISTER: {
			String bid = obj.getString(Const.BUNDLE_INFO_BUNDLE_ID);
			// Registe bundle.
			if (0 == (bid.compareTo(BundleConf.RECOMMEND_BUNDLE_ID))) {
				if (_client != null) {
					_client.GetContext().close();
				}

				_client = client;
				_id = bid;
				_spout.SetClient(client);

				InitOrder();

				_rematching.start();

				Log4j.log("Bundle[" + _id + "] is actived.");
			} else {
				Log4j.error("Registe bundle[" + _id + "] is mismatch.");
			}

			break;
		}
		case Const.BUNDLE_REPORT: {
			// 订单匹配译员成功
			// 重新发送Storm消息到TranslatorBolt告知其删除锁定到在线译员订单列表中的临时订单
			// 该消息TranslatorBolt处理完毕即可，不需要往下转发到OrderBolt
			String result = Util.GetStringFromJSon("result", obj);
			if (result != null) {
				// TranslatorBolt = trans PUT normal
				switch (result) {
				case "syn": {
					String ordercode = Util.GetStringFromJSon("ordercode", obj);
					// 从ReMatching记录中清楚该完成的订单
					String complete = Util.GetStringFromJSon("Complete", obj);
					String tranType = Util.GetStringFromJSon("trantype", obj);
					if (complete.equalsIgnoreCase("complete")) {
						_rematching.Remove(ordercode);
					} else {
						switch (tranType) {
						case "T": {
							_rematching.SetTran(ordercode, true,
									Util.GetIntFromJSon("uid", obj));
							break;
						}
						case "E": {
							_rematching.SetTran(ordercode, false,
									Util.GetIntFromJSon("uid", obj));
							break;
						}
						default:
							break;
						}
					}
					String type = Util.GetStringFromJSon("type", obj);

					JSONObject req = new JSONObject();
					req.put("aid", Util.GetStringFromJSon("aid", obj));
					req.put("id", Util.GetStringFromJSon("id", obj));
					req.put("bid", Util.GetStringFromJSon("bid", obj));
					req.put("nid", Util.GetStringFromJSon("nid", obj));
					req.put("setuid", Util.GetStringFromJSon("setuid", obj));
					req.put("ordercode", ordercode);
					req.put("langpair", Util.GetStringFromJSon("langpair", obj));
					req.put("industry_id",
							Util.GetStringFromJSon("industry_id", obj));
					req.put("method", "SYN");
					req.put("type", type);
					req.put("trantype", tranType);
					Request(req);
					break;
				}
				default: {
					// 回复消息到PHP
					if (_res != null) {
						_res.Response(id, obj.toString().getBytes());
					} else {
						Log4j.error("recom service bundle callback is null");
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
			Log4j.error("recom service bundle state[" + state + "] error");
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
		svr.SetPort(BundleConf.RECOMMEND_BUNDLE_PORT);
		svr.SetNewClientCallBack(this);
		svr.Run(false);
	}
}
