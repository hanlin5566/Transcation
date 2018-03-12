package com.wiitrans.tm.bolt;

import java.io.File;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.TMDAO;
import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.hbase.HbaseTMTUDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.FileAccess;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.Levenshtein;
//import com.wiitrans.base.tm.TMENUS;
//import com.wiitrans.base.tm.TMFRFR;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.tm.TMResult;
import com.wiitrans.base.tm.TMWord;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class AnalysisBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;
	private HashMap<Integer, TMLanguage> _tmlangMap = null;
	public String _path = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		// AppConfig app = new AppConfig();
		// app.Parse();

		WiitransConfig.getInstance(0);

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.TM_BUNDLE_PORT);
		_reportor.Start();

		if (_tmlangMap == null) {
			_tmlangMap = new HashMap<Integer, TMLanguage>();
		}

		if (_path == null) {
			// BundleParam param = app._bundles.get("tmTopo");
			BundleParam param = WiitransConfig.getInstance(0).TM;
			_path = param.BUNDLE_TEMPFILE_PATH;
			Log4j.log("          tm-tmxpath= " + _path);
		}

		if (_path != null) {
			String filename = _path + "tmx14.dtd";
			File file = new File(filename);
			if (!file.exists()) {
				try {
					FileAccess.Copy("/opt/wiitrans/conf/tmx14.dtd", filename);
				} catch (Exception e) {
					Log4j.error(e);
				}
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

		return _reportor.Report(resObj);
	}

	private int AnalysisTM(int tnid, int tmid) {
		int ret = Const.FAIL;

		TmxFile tmxFile = null;
		TMDAO dao = null;
		TMLanguage lang = null;
		FastDFS fastdfs = null;
		try {
			dao = new TMDAO();
			dao.Init(true);
			TMBean tmbean;// = dao.SelectForTMID(tmid);

			if (tnid == BundleConf.DEFAULT_NID) {
				tmbean = dao.SelectForTMID(tmid);
			} else {
				tmbean = dao.SelectForNodeTMID(tmid);
			}

			if (tmbean == null || tmbean.lang_country == null
					|| tmbean.tmx_file_id == null) {
				Log4j.error("tm is null or tm is wrong. ");
				return ret;
			}
			if (tmbean.analyse) {
				Log4j.error("tm is analysed. ");
				return ret;
			}
			lang = DetectTMLanguage.Detect(tmbean.lang_country);
			if (lang == null) {
				Log4j.error("TMLanguage is null.");
				return ret;
			}

			String tmxName = _path + tmid + ".tmx";

			fastdfs = new FastDFS();
			fastdfs.Init();
			fastdfs.Download(tmbean.tmx_file_id, tmxName);
			fastdfs.UnInit();

			// tmxName = _path + "39.tmx";

			File f = new File(tmxName);
			if (f.exists()) {
				tmxFile = new TmxFile();
				tmxFile.Init(null, tmxName, tmbean.lang_country, "");
				long begintime = System.currentTimeMillis();
				long starttime = begintime;
				if (Const.SUCCESS == tmxFile.ReadTMXFile()) {
					long endtime = System.currentTimeMillis();
					Log4j.debug(String.format("tmx%d read tmx %dms.", tmid,
							endtime - begintime));

					lang.Init(tmxFile, true);
					lang.Parse(tmid);
					lang.WriteTMText(tmid);

					begintime = endtime;
					endtime = System.currentTimeMillis();
					Log4j.debug(String.format(
							"tmx %d lang write tu text %dms.", tmid, endtime
									- begintime));

					tmxFile.UnInit();
					lang.WriteTM(tmid);
					lang.UnInit();

					begintime = endtime;
					endtime = System.currentTimeMillis();

					Log4j.debug(String.format(
							"tmx %d lang write tu index %dms.", tmid, endtime
									- begintime));

					tmbean.analyse = true;
					if (tnid == BundleConf.DEFAULT_NID) {
						dao.UpdateAnalyse(tmbean);
					} else {
						dao.UpdateAnalyseForNode(tmbean);
					}
					dao.Commit();
					dao.UnInit();
					Log4j.debug(String.format("tmx%d analysis %dms.", tmid,
							endtime - starttime));
					ret = Const.SUCCESS;
				} else {
					Log4j.error("tmx file(" + tmxName + ") read error.");
				}
			} else {
				Log4j.error("tmx file(" + tmxName + ") not exists.");
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (tmxFile != null) {
				tmxFile.UnInit();
			}
			if (dao != null) {
				dao.UnInit();
			}
			if (fastdfs != null) {
				fastdfs.UnInit();
			}
		}

		return ret;
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

	private int GetTM(int tmid, JSONObject obj) {
		int ret = Const.FAIL;
		HbaseTMTUDAO dao = null;
		try {
			String text = Util.GetStringFromJSon("text", obj);
			if (tmid > 0 && text != null && text.trim().length() > 0) {
				ArrayList<TMResult> list = null;
				TMLanguage lang = null;
				long starttime = System.currentTimeMillis();
				if (_tmlangMap.containsKey(tmid)) {
					lang = _tmlangMap.get(tmid);
					list = lang.SearchTM(text);
				} else {
					Log4j.error("tm(" + tmid + ") lang not exists");
				}
				int tucount;
				JSONObject content = new JSONObject();
				if (list != null && lang != null) {
					int pageNo = Util.GetIntFromJSon("pageno", obj);
					int countperpage = 10;
					int pagecount;

					// AppConfig app = new AppConfig();
					// app.Parse();
					// BundleParam param = app._bundles.get("tmTopo");
					BundleParam param = WiitransConfig.getInstance(0).TM;

					if (param.BUNDLE_TM_LEVENSHTEIN
							&& lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
						Levenshtein levenshtein = new Levenshtein();
						int[] tmids = new int[list.size()];
						for (int i = 0; i < list.size(); i++) {
							tmids[i] = list.get(i).tuID;
						}
						JSONObject[] tmsources = null;
						dao = new HbaseTMTUDAO();

						dao.Init(true);
						dao.SETTable(tmid, false);

						tmsources = dao.SearchTM(tmids);

						dao.UnInit();
						String source;
						TMResult tu;

						long[] textIndex = this.AnalyseText(lang, text);

						// 存储有效结果，匹配度必须超过75
						ArrayList<TMResult> resultList = new ArrayList<TMResult>();
						if (tmsources != null
								&& tmids.length == tmsources.length) {
							for (int i = 0; i < tmsources.length; i++) {

								tu = list.get(i);
								if (tmsources[i] != null && tu != null) {
									tu.obj = tmsources[i];
									source = Util.GetStringFromJSon("source",
											tmsources[i]);
									if (source != null) {
										tu.similarity = levenshtein.Similarity(
												this.AnalyseText(lang, source),
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

							this.LogTUID(tmids);

							if (tmsources != null
									&& tmids.length == tmsources.length) {
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
							List<TMResult> tmresultlist = list.subList(first,
									last + 1);
							int[] tmids = new int[tmresultlist.size()];
							for (int i = 0; i < tmresultlist.size(); i++) {
								tmids[i] = tmresultlist.get(i).tuID;
							}
							JSONObject[] tmsources = null;
							dao = new HbaseTMTUDAO();

							dao.Init(true);
							dao.SETTable(tmid, false);

							tmsources = dao.SearchTM(tmids);

							dao.UnInit();

							this.LogTUID(tmids);

							if (tmsources != null
									&& tmids.length == tmsources.length) {
								for (int i = 0; i < tmsources.length; i++) {
									content.put(String.valueOf(i), tmsources[i]);
								}
							}
						}
					}

					JSONObject resObj = new JSONObject();
					resObj.put("result", "OK");
					resObj.put("content", content);
					resObj.put("tucount", tucount);
					resObj.put("pagecount", pagecount);
					resObj.put("countperpage", countperpage);
					resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
					resObj.put(Const.BUNDLE_INFO_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
					resObj.put(Const.BUNDLE_INFO_ACTION_ID,
							Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID,
									obj));
					_reportor.Report(resObj);

				} else {
					SendToPHP(obj, "FAILED");
					Log4j.error("tm search result list is null");
				}
				long endtime = System.currentTimeMillis();
				Log4j.debug(String.format("tmx%d search %dms.", tmid, endtime
						- starttime));
			} else {
				SendToPHP(obj, "FAILED");
				Log4j.error("param is wrong");
			}
		} catch (Exception e) {
			Log4j.error(e);
			SendToPHP(obj, "FAILED");
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}

		return ret;
	}

	private int InitTM(int tnid, int tmid) {
		int ret = Const.FAIL;
		TMDAO dao = null;
		TMLanguage lang = null;
		try {
			if (!_tmlangMap.containsKey(tmid)) {
				dao = new TMDAO();
				dao.Init(true);
				TMBean tmbean;

				if (tnid == BundleConf.DEFAULT_NID) {
					tmbean = dao.SelectForTMID(tmid);
				} else {
					tmbean = dao.SelectForNodeTMID(tmid);
				}

				if (tmbean == null || tmbean.lang_country == null
						|| tmbean.tmx_file_id == null) {
					Log4j.error("tm is null or tm is wrong. ");
					return ret;
				}
				if (!tmbean.analyse) {
					Log4j.error("tm is not analysed. ");
					return ret;
				}
				lang = DetectTMLanguage.Detect(tmbean.lang_country);
				if (lang == null) {
					Log4j.error("TMLanguage is null.");
					return ret;
				}

				long starttime = System.currentTimeMillis();
				lang.ReadTM(tmid);
				_tmlangMap.put(tmid, lang);
				long endtime = System.currentTimeMillis();
				Log4j.debug(String.format("tmx%d init %dms.", tmid, endtime
						- starttime));
			}
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}

		return ret;
	}

	private int UnInitTM(int tmid) {
		int ret = Const.FAIL;
		try {
			TMLanguage lang;
			long starttime = System.currentTimeMillis();
			if (_tmlangMap.containsKey(tmid)) {
				lang = _tmlangMap.remove(tmid);
				lang.UnInit();
			}
			long endtime = System.currentTimeMillis();
			Log4j.debug(String.format("tmx%d uninit %dms.", tmid, endtime
					- starttime));
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String stmid = tuple.getStringByField("tmid");
		int tmid = Util.String2Int(stmid);
		String content = tuple.getStringByField("content");
		JSONObject obj = new JSONObject(content);
		String aid = Util.GetStringFromJSon("aid", obj);
		// String uid = Util.GetStringFromJSon("uid", obj);
		String method = Util.GetStringFromJSon("method", obj);
		int tnid = Util.GetIntFromJSon("tnid", obj);
		if (tnid <= 0) {
			tnid = BundleConf.DEFAULT_NID;
		}

		Log4j.log("analysisbolt " + obj.toString());

		switch (aid) {
		case "analysis": {
			// tm/analysis
			switch (method) {
			case "POST": {
				SendToPHP(obj, "OK");
				AnalysisTM(tnid, tmid);
				break;
			}
			case "GET": {
				GetTM(tmid, obj);
				break;
			}
			case "PUT": {
				SendToPHP(obj, "OK");
				InitTM(tnid, tmid);
				break;
			}
			case "DELETE": {
				SendToPHP(obj, "OK");
				UnInitTM(tmid);
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

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
	}
}
