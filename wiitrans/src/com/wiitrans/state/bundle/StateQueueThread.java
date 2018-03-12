package com.wiitrans.state.bundle;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.BundleRequest;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.bundle.IResponse;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.ProcLoginDAO;
import com.wiitrans.base.db.ProcTranslatorMsgDAO;
import com.wiitrans.base.db.TransGradeDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.TransGradeBean;
import com.wiitrans.base.db.model.TranslatorBean;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.FailUtil;
import com.wiitrans.base.misc.Util;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class StateQueueThread extends Thread {
	private IResponse _res = null;
	// private String _id = null;
	private BundleRequest _spout = null;
	private RedisCache _cache = null;
	// private Client _client = null;

	private String _msgURL = null;
	private String _roomURL = null;
	private String _recomURL = null;
	private String _operaURL = null;
	private String _stateURL = null;

	private int _timeout = 2;

	private LinkedBlockingQueue<JSONObject> _queue = new LinkedBlockingQueue<JSONObject>();

	private int _onlineListcount = 10;

	public StateQueueThread(BundleRequest spout) {
		_spout = spout;
	}

	public int SetResponse(IResponse res) {

		int ret = Const.FAIL;

		_res = res;
		ret = Const.SUCCESS;

		return ret;
	}

	// 语言对
	private void SetUser(int pair_id, int translator_id, boolean editor) {
		if (translator_id > 0) {
			String key = Util.GetRedisKey(pair_id);
			String member = String.valueOf(translator_id);
			if (editor) {
				member += "_E";
			}
			_cache.sadd(key, member);
		}
	}

	private void RemoveUser(int pair_id, int translator_id) {
		if (translator_id > 0) {
			String key = Util.GetRedisKey(pair_id);
			String member = String.valueOf(translator_id);
			_cache.srem(key, member, member + "_E");
		}
	}

	// 语言对 领域
	private void SetUserForIndustry(int pair_id, int industry_id,
			int translator_id, boolean editor) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForIndustry(pair_id, industry_id);
			String member = String.valueOf(translator_id);
			if (editor) {
				member += "_E";
			}
			_cache.sadd(key, member);
		}
	}

	private void RemoveUserForIndustry(int pair_id, int industry_id,
			int translator_id) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForIndustry(pair_id, industry_id);
			String member = String.valueOf(translator_id);
			_cache.srem(key, member, member + "_E");
		}
	}

	// 语言对 级别
	private void SetUserForGrade(int pair_id, int grade_id, int translator_id,
			boolean editor) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForGrade(pair_id, grade_id);
			String member = String.valueOf(translator_id);
			if (editor) {
				member += "_E";
			}
			_cache.sadd(key, member);
		}
	}

	private void RemoveUserForGrade(int pair_id, int grade_id, int translator_id) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForGrade(pair_id, grade_id);
			String member = String.valueOf(translator_id);
			_cache.srem(key, member, member + "_E");
		}
	}

	// 语言对 级别 领域
	private void SetUserForGradeIndustry(int pair_id, int grade_id,
			int industry_id, int translator_id, boolean editor) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForGradeIndustry(pair_id, grade_id,
					industry_id);
			String member = String.valueOf(translator_id);
			if (editor) {
				member += "_E";
			}
			_cache.sadd(key, member);
		}
	}

	private void RemoveUserForGradeIndustry(int pair_id, int grade_id,
			int industry_id, int translator_id) {
		if (translator_id > 0) {
			String key = Util.GetRedisKeyForGradeIndustry(pair_id, grade_id,
					industry_id);
			String member = String.valueOf(translator_id);
			_cache.srem(key, member, member + "_E");
		}
	}

	public int Start() {
		int ret = Const.FAIL;

		if (_cache == null) {
			_cache = new RedisCache();
			// 配置统一
			ret = _cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}
		WiitransConfig.getInstance(1);

		ConfigNode myNode = BundleConf.BUNDLE_Node.get(BundleConf.DEFAULT_NID);
		String url = "";
		if (myNode != null) {
			url = myNode.api;
		}
		_msgURL = url + "msg/newm/";
		_roomURL = url + "msg/roomnew/";
		_operaURL = url + "opera/useraction/";
		_stateURL = url + "state/user/";

		ConfigNode recomNode = BundleConf.BUNDLE_Node.get(BundleConf.RECOM_NID);
		if (recomNode != null) {
			_recomURL = recomNode.api;
		}
		_recomURL = _recomURL + "recom/user/";

		Log4j.log("          state-msgurl  = " + _msgURL);
		Log4j.log("          state-roomurl = " + _roomURL);
		Log4j.log("          state-recomurl= " + _recomURL);
		Log4j.log("          state-operaurl= " + _operaURL);
		Log4j.log("          state-stateurl= " + _stateURL);

		BundleParam param = WiitransConfig.getInstance(1).STATE;
		_onlineListcount = param.BUNDLE_ONLINE_SHOWCOUNT;

		TransGradeDAO dao = null;
		try {

			dao = new TransGradeDAO();
			dao.Init(true);
			// 初始化旧版本权限
			List<TransGradeBean> list = dao.SelectAll();
			if (list != null && list.size() > 0) {
				for (TransGradeBean transGradeBean : list) {
					this.SetUser(transGradeBean.pair_id,
							transGradeBean.translator_id, transGradeBean.editor);
					this.SetUserForGrade(transGradeBean.pair_id,
							transGradeBean.grade_id,
							transGradeBean.translator_id, transGradeBean.editor);
					String industry_ids = transGradeBean.industry_ids;
					if (industry_ids != null && industry_ids.length() > 0) {
						String[] industryIDs = industry_ids.split(",");
						if (industryIDs != null && industryIDs.length > 0) {
							for (String industryID : industryIDs) {
								if (industryID != null
										&& industryID.length() > 0) {
									this.SetUserForIndustry(
											transGradeBean.pair_id,
											Util.String2Int(industryID),
											transGradeBean.translator_id,
											transGradeBean.editor);
									this.SetUserForGradeIndustry(
											transGradeBean.pair_id,
											transGradeBean.grade_id,
											Util.String2Int(industryID),
											transGradeBean.translator_id,
											transGradeBean.editor);
								}
							}
						}
					}
				}
			}
			list = null;
			// 初始化新版本权限
			list = dao.SelectAllForGradeNew();
			if (list != null && list.size() > 0) {
				for (TransGradeBean transGradeBean : list) {
					this.SetUser(transGradeBean.pair_id,
							transGradeBean.translator_id, transGradeBean.editor);
					this.SetUserForGrade(transGradeBean.pair_id,
							transGradeBean.grade_id,
							transGradeBean.translator_id, transGradeBean.editor);

					this.SetUserForIndustry(transGradeBean.pair_id,
							transGradeBean.industry_id,
							transGradeBean.translator_id, transGradeBean.editor);
					this.SetUserForGradeIndustry(transGradeBean.pair_id,
							transGradeBean.grade_id,
							transGradeBean.industry_id,
							transGradeBean.translator_id, transGradeBean.editor);

				}
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		Log4j.log("state init finish. ");
		this.start();
		ret = Const.SUCCESS;

		return ret;
	}

	public int Stop() {
		int ret = Const.FAIL;

		return ret;
	}

	public void Push(JSONObject msg) {
		try {
			_queue.put(msg);
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	public JSONObject Pop() {
		JSONObject req = null;

		try {
			req = _queue.take();

		} catch (InterruptedException e) {
			Log4j.error(e);
		}

		return req;
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

	private JSONObject SaveUserInfo(JSONObject obj) {
		JSONObject userInfo = null;

		// 用户已经通过考核的语言对信息需要查询DB

		int uid = Util.GetIntFromJSon("uid", obj);
		int tzid = Util.GetIntFromJSon("tzid", obj);
		ProcTranslatorMsgDAO proctranslatormsgdao = null;
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("p_node_id", BundleConf.DEFAULT_NID);

			map.put("p_translator_id", uid);

			map.put("p_match_industry", BundleConf.BUNDLE_MATCH_INDUSTRY);
			proctranslatormsgdao = new ProcTranslatorMsgDAO();
			proctranslatormsgdao.Init(false);
			List<List<?>> list = proctranslatormsgdao.Select(map);
			proctranslatormsgdao.UnInit();
			if (list.size() == 3) {
				List<TranslatorBean> userlist = (List<TranslatorBean>) list
						.get(0);
				List<TransGradeBean> pairlist = (List<TransGradeBean>) list
						.get(1);

				List<OrderBean> orderlist = (List<OrderBean>) list.get(2);

				userInfo = new JSONObject();
				userInfo.put("sid", Util.GetStringFromJSon("sid", obj));
				userInfo.put("uid", String.valueOf(uid));

				if (userlist != null && userlist.size() > 0) {
					TranslatorBean translator = userlist.get(0);
					userInfo.put("level", translator.level);
					userInfo.put("email", translator.email);
					userInfo.put("nickname", translator.nickname);
					userInfo.put("normal_order_number",
							String.valueOf(translator.normal_order_number));
					userInfo.put("total_order_number",
							String.valueOf(translator.total_order_number));
					userInfo.put("word_count",
							String.valueOf(translator.word_count));
					userInfo.put("head", translator.head);
					userInfo.put("tzid", translator.time_zone_id);
				}
				int role_id = Util.GetIntFromJSon("role_id", obj);
				if (role_id == 2) {
					JSONObject langpairs = new JSONObject();
					JSONObject langpair = null;

					for (TransGradeBean transGradeBean : pairlist) {
						if (transGradeBean.pair_id > 0) {
							langpair = new JSONObject();
							langpair.put("grade_id",
									String.valueOf(transGradeBean.grade_id));
							langpair.put("slang_id",
									String.valueOf(transGradeBean.slang_id));
							langpair.put("tlang_id",
									String.valueOf(transGradeBean.tlang_id));
							langpair.put("editor",
									String.valueOf(transGradeBean.editor));
							langpair.put(
									"effective_word_count",
									String.valueOf(transGradeBean.effective_word_count));
							langpair.put("pair_word_count", String
									.valueOf(transGradeBean.pair_word_count));
							langpair.put("industry_ids",
									transGradeBean.industry_ids);
							langpair.put("industry_grade_editor",
									transGradeBean.industry_grade_editor);
							langpairs.put(
									String.valueOf(transGradeBean.pair_id),
									langpair);
						}
					}
					userInfo.put("langpairs", langpairs);
				}

			}

		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		} finally {
			if (proctranslatormsgdao != null) {
				proctranslatormsgdao.UnInit();
			}
		}

		return userInfo;
	}

	public int PhpLogin(JSONObject obj) {
		// int ret = Const.FAIL;
		String uid = Util.GetStringFromJSon("uid", obj);
		String sid = Util.GetStringFromJSon("sid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		int role_id = Util.GetIntFromJSon("role_id", obj);
		// 先添加缓存
		if (nid > 0 && uid != null) {
			if (nid != BundleConf.DEFAULT_NID) {
				SendToPHP(obj, FailUtil.GetFailedMsg(FailUtil.SERVICE_STATE,
						FailUtil.CLASS_STATE_QUEUE, FailUtil.FAIL_TYPE_ID));
				return Const.FAIL;
			}
			String userkey = "uid_" + uid;
			// 重新登录重新保存信息
			_cache.DelString(nid, userkey);
			String userInfoString = _cache.GetString(nid, userkey);
			JSONObject userInfo;

			if (userInfoString == null) {
				userInfo = SaveUserInfo(obj);
				if (userInfo != null) {
					_cache.SetString(nid, userkey, userInfo.toString());
				}
			}

			int login_time = Util.GetIntFromNow();

			JSONObject params = null;
			try {

				// 登录日志
				JSONObject json = new JSONObject();
				json.put("time", login_time);
				json.put("user", uid);
				json.put("action", "1");

				params = new JSONObject();
				params.put("sid", sid);
				params.put("uid", uid);
				params.put("nid", String.valueOf(nid));
				params.put("actionmsg", json.toString());
				new HttpSimulator(_operaURL).executeMethodTimeOut(
						params.toString(), _timeout);
			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			}
			ProcLoginDAO proclogindao = null;
			try {
				proclogindao = new ProcLoginDAO();
				proclogindao.Init(false);
				List<Integer> list = proclogindao.Login(Util.String2Int(uid));
				proclogindao.UnInit();

				int msgcount = 0;
				if (list.size() == 1) {
					Integer msgcountObj = (Integer) list.get(0);
					if (msgcountObj.intValue() > 0) {
						msgcount = msgcountObj.intValue();
						params = new JSONObject();
						params.put("sid", sid);
						params.put("uid", uid);
						params.put("nid", String.valueOf(nid));
						params.put("senduid", uid);
						params.put("sendnid", String.valueOf(nid));
						params.put("msgcount", String.valueOf(msgcount));

						new HttpSimulator(_msgURL).executeMethodTimeOut(
								params.toString(), _timeout);
					}
				}

			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			} finally {
				if (proclogindao != null) {
					proclogindao.UnInit();
				}
			}

			SendToPHP(obj, FailUtil.SUCCESS);

			return Const.SUCCESS;
		} else {
			SendToPHP(obj, FailUtil.GetFailedMsg(FailUtil.SERVICE_STATE,
					FailUtil.CLASS_STATE_QUEUE, FailUtil.FAIL_TYPE_ID));

			return Const.FAIL;
		}
	}

	private void DeleteTransGrade(int translator_id, JSONObject obj) {
		int pair_id = Util.GetIntFromJSon("pair_id", obj);
		if (pair_id > 0) {
			RemoveUser(pair_id, translator_id);

			int grade_id = Util.GetIntFromJSon("grade_id", obj);
			if (grade_id > 0) {
				RemoveUserForGrade(pair_id, grade_id, translator_id);
			}
			String industry_ids = Util.GetStringFromJSon("industry_ids", obj);
			if (industry_ids != null && industry_ids.length() > 0) {
				String[] industryIDs = industry_ids.split(",");
				if (industryIDs != null && industryIDs.length > 0) {
					for (String industryID : industryIDs) {
						int industry_id = Util.String2Int(industryID);
						if (industry_id > 0) {
							RemoveUserForIndustry(pair_id, industry_id,
									translator_id);
							if (grade_id > 0) {
								RemoveUserForGradeIndustry(pair_id, grade_id,
										industry_id, translator_id);
							}
						}
					}
				}
			}
		}
	}

	private int PhpTransGradeEdit(JSONObject obj) {
		int ret = Const.FAIL;
		String uid = Util.GetStringFromJSon("uid", obj);
		String sid = Util.GetStringFromJSon("sid", obj);
		String nid = Util.GetStringFromJSon("nid", obj);
		int translator_id = 0;
		try {
			translator_id = Util.GetIntFromJSon("translator_id", obj);
		} catch (Exception e) {
			Log4j.error(e);
		}

		if (uid != null && sid != null && translator_id > 0) {
			// 删除权限
			DeleteTransGrade(translator_id, obj);
			TransGradeDAO dao = null;
			try {

				dao = new TransGradeDAO();
				dao.Init(true);
				// 初始化旧版本权限
				List<TransGradeBean> list = dao.SelectByTransID(translator_id);
				if (list != null && list.size() > 0) {
					for (TransGradeBean transGradeBean : list) {
						this.SetUser(transGradeBean.pair_id,
								transGradeBean.translator_id,
								transGradeBean.editor);
						this.SetUserForGrade(transGradeBean.pair_id,
								transGradeBean.grade_id,
								transGradeBean.translator_id,
								transGradeBean.editor);
						String industry_ids = transGradeBean.industry_ids;
						if (industry_ids != null && industry_ids.length() > 0) {
							String[] industryIDs = industry_ids.split(",");
							if (industryIDs != null && industryIDs.length > 0) {
								for (String industryID : industryIDs) {
									if (industryID != null
											&& industryID.length() > 0) {
										this.SetUserForIndustry(
												transGradeBean.pair_id,
												Util.String2Int(industryID),
												transGradeBean.translator_id,
												transGradeBean.editor);
										this.SetUserForGradeIndustry(
												transGradeBean.pair_id,
												transGradeBean.grade_id,
												Util.String2Int(industryID),
												transGradeBean.translator_id,
												transGradeBean.editor);
									}
								}
							}
						}
					}
				}
				list = null;
				// 初始化新版本权限
				list = dao.SelectForGradeNewByTransID(translator_id);
				if (list != null && list.size() > 0) {
					for (TransGradeBean transGradeBean : list) {
						this.SetUser(transGradeBean.pair_id,
								transGradeBean.translator_id,
								transGradeBean.editor);
						this.SetUserForGrade(transGradeBean.pair_id,
								transGradeBean.grade_id,
								transGradeBean.translator_id,
								transGradeBean.editor);

						this.SetUserForIndustry(transGradeBean.pair_id,
								transGradeBean.industry_id,
								transGradeBean.translator_id,
								transGradeBean.editor);
						this.SetUserForGradeIndustry(transGradeBean.pair_id,
								transGradeBean.grade_id,
								transGradeBean.industry_id,
								transGradeBean.translator_id,
								transGradeBean.editor);

					}
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao != null) {
					dao.UnInit();
				}
			}
			SendToPHP(obj, "OK");
		} else {
			SendToPHP(obj, "FAILED");
		}
		return ret;
	}

	public int PhpLogout(JSONObject obj) {
		int ret = Const.FAIL;
		String uid = Util.GetStringFromJSon("uid", obj);
		String sid = Util.GetStringFromJSon("sid", obj);
		String nid = Util.GetStringFromJSon("nid", obj);
		int role_id = Util.GetIntFromJSon("role_id", obj);
		if (uid != null && sid != null && role_id >= 0) {

			JSONObject params = null;
			try {
				JSONObject json = new JSONObject();
				json.put("time", new Date().getTime() / 1000);
				json.put("user", uid);
				json.put("action", "2");

				params = new JSONObject();
				params.put("sid", sid);
				params.put("uid", uid);
				params.put("nid", nid);
				params.put("actionmsg", json.toString());
				new HttpSimulator(_operaURL).executeMethodTimeOut(
						params.toString(), _timeout);

			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			}

			// 再删除缓存
			if (uid != null) {
				String userkey = "uid_" + uid;
				_cache.DelString(Util.String2Int(nid), userkey);
			}

			SendToPHP(obj, "OK");
		} else {
			SendToPHP(obj, "FAILED");
		}
		return ret;
	}

	public int Request(JSONObject msg) {
		Log4j.log("state queuethread " + msg.toString());
		try {
			String aid = Util.GetStringFromJSon("aid", msg);
			String method = Util.GetStringFromJSon("method", msg);
			switch (aid) {
			case "user": {
				String uid = Util.GetStringFromJSon("uid", msg);
				String sid = Util.GetStringFromJSon("sid", msg);
				if (uid != null && uid.length() != 0 && sid != null
						&& sid.length() != 0) {
					switch (method) {
					case "POST": {
						PhpLogin(msg);
						break;
					}
					case "PUT": {
						PhpTransGradeEdit(msg);
						break;
					}
					case "DELETE": {
						PhpLogout(msg);
						break;
					}
					default:
						SendToPHP(msg, FailUtil.GetFailedMsg(
								FailUtil.SERVICE_STATE,
								FailUtil.CLASS_STATE_QUEUE,
								FailUtil.FAIL_TYPE_ID));
						break;
					}
				} else {
					Log4j.log("uid(" + uid + ") sid(" + sid + ") is error. ");
					SendToPHP(msg, FailUtil.GetFailedMsg(
							FailUtil.SERVICE_STATE, FailUtil.CLASS_STATE_QUEUE,
							FailUtil.FAIL_TYPE_ID));
				}
				break;
			}

			default:
				SendToPHP(msg, FailUtil.GetFailedMsg(FailUtil.SERVICE_STATE,
						FailUtil.CLASS_STATE_QUEUE, FailUtil.FAIL_TYPE_ID));
				break;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}

		return Const.SUCCESS;
	}

	public void run() {
		while (true) {
			try {

				JSONObject req = _queue.take();
				if (req != null) {
					Request(req);
				}

			} catch (Exception e) {
				Log4j.error(e);
			}
		}
	}
}