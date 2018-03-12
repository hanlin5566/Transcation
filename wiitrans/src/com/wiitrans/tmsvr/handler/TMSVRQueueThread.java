package com.wiitrans.tmsvr.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.db.model.TMServiceTextBean;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.Levenshtein;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.tm.TMResult;
import com.wiitrans.base.tm.TMWord;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class TMSVRQueueThread extends Thread {
	private TaskReportor _repotor = null;
	private TMLanguage _lang = null;
	private LinkedBlockingQueue<JSONObject> _queue = new LinkedBlockingQueue<JSONObject>();

	public TMSVRQueueThread(TaskReportor repotor) {
		_repotor = repotor;
	}

	public void SetTMLang(TMLanguage lang) {
		_lang = lang;
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

	private int SendToPHP(JSONObject obj, String result) {
		JSONObject resObj = new JSONObject();
		resObj.put("result", result);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

		return _repotor.Report(resObj);
	}

	private int Invalid(String msg) {
		JSONObject obj = new JSONObject(msg);
		String id = Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj);
		JSONObject resObj = new JSONObject();
		resObj.put("result", "FAILED");
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID, id);
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		return _repotor.Report(resObj);
	}

	public int Request(JSONObject msg) {
		int ret = Const.FAIL;
		Log4j.log("tmsvr queuethread " + msg.toString());
		try {
			String aid = Util.GetStringFromJSon("aid", msg);
			String method = Util.GetStringFromJSon("method", msg);
			// String uid = Util.GetStringFromJSon("uid", msg);
			// String sid = Util.GetStringFromJSon("sid", msg);
			int tmid = Util.GetIntFromJSon("tmid", msg);
			if (tmid <= 0) {
				ret = Invalid(msg.toString());
			} else {

			}
			switch (aid) {
			case "analysis": {
				switch (method) {
				case "GET": {
					SearchText(tmid, msg);
					break;
				}
				case "DELETE": {
					System.exit(0);
					break;
				}
				default:
					break;
				}
				break;
			}
			case "manage": {
				switch (method) {
				case "GET": {
					GetTUListFromTM(tmid, msg);
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

			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	// 根据TUID组获得原句和翻译句子
	public JSONObject[] GetTextByTUIDs(Integer tmID, int[] tuids) {
		JSONObject[] tmsources = null;
		TMServiceDAO dao = null;
		try {
			tmsources = new JSONObject[tuids.length];
			if (tuids.length > 0) {
				dao = new TMServiceDAO();
				dao.Init();
				List<TMServiceTextBean> list = dao.SelectTextByTuIDs(tmID,
						tuids);
				for (int i = 0; i < tuids.length; i++) {
					for (TMServiceTextBean tmServiceTextBean : list) {
						if (tuids[i] == tmServiceTextBean.tu_id) {
							tmsources[i] = new JSONObject();
							tmsources[i]
									.put("source", tmServiceTextBean.source);
							tmsources[i]
									.put("target", tmServiceTextBean.target);
						}
					}
				}
				dao.UnInit();
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
			if (tmsources == null) {
				tmsources = new JSONObject[0];
			}
		}
		return tmsources;
	}

	private void GetTUListFromTM(int tmid, JSONObject msg) {

		int tuid = Util.GetIntFromJSon("tuid", msg);
		JSONObject content = new JSONObject();

		if (tuid <= 0) {
			tuid = 1;
		}

		int[] tuids = new int[10];
		for (int i = 0; i < tuids.length; i++) {
			tuids[i] = tuid + i;
		}

		JSONObject[] tmsources = this.GetTextByTUIDs(tmid, tuids);

		for (int i = 0; i < tuids.length; i++) {
			content.put(String.valueOf(i), tmsources[i]);
		}

		JSONObject resObj = new JSONObject();
		resObj.put("language_type", _lang.GetLanguageType().toString()
				.toLowerCase());
		resObj.put("result", "OK");
		resObj.put("content", content);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, msg));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, msg));
		_repotor.Report(resObj);
	}

	private void SearchText(int tmid, JSONObject msg) {
		String text = Util.GetStringFromJSon("text", msg);
		ArrayList<TMResult> list = _lang.SearchTMChunk(tmid, text);
		int tucount;
		JSONObject content = new JSONObject();
		if (list != null) {
			int pageNo = Util.GetIntFromJSon("pageno", msg);
			int countperpage = 10;
			int pagecount;

			BundleParam param = WiitransConfig.getInstance(2).TMSVR;

			if (param.BUNDLE_TM_LEVENSHTEIN
					&& _lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
				Levenshtein levenshtein = new Levenshtein();
				int[] tuids = new int[list.size()];
				for (int i = 0; i < list.size(); i++) {
					tuids[i] = list.get(i).tuID;
				}
				JSONObject[] tmsources = this.GetTextByTUIDs(tmid, tuids);
				String source;
				TMResult tu;

				long[] textIndex = this.AnalyseText(_lang, text);

				// 存储有效结果，匹配度必须超过75
				ArrayList<TMResult> resultList = new ArrayList<TMResult>();
				if (tmsources != null && tuids.length == tmsources.length) {
					for (int i = 0; i < tmsources.length; i++) {

						tu = list.get(i);
						if (tmsources[i] != null && tu != null) {
							tu.obj = tmsources[i];
							source = Util.GetStringFromJSon("source",
									tmsources[i]);
							if (source != null) {
								tu.similarity = levenshtein.Similarity(
										this.AnalyseText(_lang, source),
										textIndex);
								if (tu.similarity >= 75) {
									resultList.add(tu);
								}
							} else {
								tu.similarity = 0;
							}
						}
					}
				}
				// 计算匹配度的，需要匹配度排序，再跳转页面
				DetectTMLanguage.TMResultSortDistanceDesc(resultList);

				tucount = resultList.size();
				pagecount = (tucount + countperpage - 1) / countperpage;
				if (pagecount > 0) {
					if (pageNo < 0) {
						pageNo = 0;
					} else if (pageNo >= pagecount) {
						pageNo = pagecount - 1;
					}
					int first = pageNo * countperpage;
					int last = first + countperpage - 1;
					if (last >= tucount) {
						last = tucount - 1;
					}

					this.LogTUID(tuids);

					if (tmsources != null && tuids.length == tmsources.length) {
						for (int i = first; i <= last; i++) {
							content.put(String.valueOf(i), tmsources[i]);
						}
					}
				}
			} else {
				// 不计算匹配度的直接跳转页面
				tucount = list.size();
				pagecount = (tucount + countperpage - 1) / countperpage;
				if (pagecount > 0) {
					if (pageNo < 0) {
						pageNo = 0;
					} else if (pageNo >= pagecount) {
						pageNo = pagecount - 1;
					}
					int first = pageNo * countperpage;
					int last = first + countperpage - 1;
					if (last >= tucount) {
						last = tucount - 1;
					}
					List<TMResult> tmresultlist = list.subList(first, last + 1);
					JSONObject[] tmsources;
					if (_lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {

						int[] tuids = new int[tmresultlist.size()];
						for (int i = 0; i < tmresultlist.size(); i++) {
							tuids[i] = tmresultlist.get(i).tuID;
						}
						tmsources = this.GetTextByTUIDs(tmid, tuids);
						this.LogTUID(tuids);
						if (tmsources != null
								&& tuids.length == tmsources.length) {
							for (int i = 0; i < tmsources.length; i++) {
								content.put(String.valueOf(i), tmsources[i]);
							}
						}
					} else {
						tmsources = new JSONObject[tmresultlist.size()];
						for (int i = 0; i < tmresultlist.size(); i++) {
							tmsources[i] = new JSONObject();
							tmsources[i].put("source",
									tmresultlist.get(i).source);
							tmsources[i].put("target",
									tmresultlist.get(i).target);
							content.put(String.valueOf(i), tmsources[i]);
						}
					}
				}
			}

			JSONObject resObj = new JSONObject();
			resObj.put("language_type", _lang.GetLanguageType().toString()
					.toLowerCase());
			resObj.put("result", "OK");
			resObj.put("content", content);
			resObj.put("tucount", tucount);
			resObj.put("pagecount", pagecount);
			resObj.put("countperpage", countperpage);
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, msg));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, msg));
			_repotor.Report(resObj);

		} else {
			SendToPHP(msg, "FAILED");
			Log4j.error("tmsvr search result list is null");
		}
	}

	private long[] AnalyseText(TMLanguage lang, String text) {
		ArrayList<TMWord> textlist = lang.AnalyseWord(text.trim());
		ArrayList<Long> textIndexary = new ArrayList<Long>();

		for (TMWord tmWord : textlist) {
			if (tmWord != null && tmWord.isWord && tmWord.isAbled) {
				textIndexary.add(lang.GetHansonCodeByWord(tmWord.word));
			}
		}
		long[] result = new long[textIndexary.size()];
		for (int i = 0; i < textIndexary.size(); i++) {
			result[i] = textIndexary.get(i);
		}

		return result;
	}

	private void LogTUID(int[] tmIDs) {
		StringBuffer sb = new StringBuffer();
		if (tmIDs.length > 0) {
			sb.append(" tm id list ");
			for (int i : tmIDs) {
				sb.append(i).append(" ");
			}

			Log4j.log(sb.toString());
		}
	}
}
