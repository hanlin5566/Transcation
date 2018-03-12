package com.wiitrans.tmsvr.handler;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.TMDAO;
import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TMTaskCollector;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.TMLanguage;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class AnalysisHandler {
	private TMSVRQueueThread _queue = null;
	private TMLanguage _lang = null;
	private int _nid = 0;
	private int _tmid = 0;
	public String _path = null;
	private TMTaskCollector _taskCollector = null;
	private TaskReportor _reportor = null;

	public AnalysisHandler(int nid, int tmid) {
		WiitransConfig.getInstance(2);
		this._nid = nid;
		this._tmid = tmid;
	}

	public int Start() {
		int ret = Const.FAIL;

		if (_nid > 0 && _tmid > 0) {
			// AppConfig app = new AppConfig();
			// app.Parse(2);

			if (_path == null) {
				// BundleParam param = app._bundles.get("tmsvrTopo");
				BundleParam param = WiitransConfig.getInstance(2).TMSVR;
				_path = param.BUNDLE_TEMPFILE_PATH;
				Log4j.log("          tmsvr-tmxpath= " + _path);
			}

			_taskCollector = new TMTaskCollector(BundleConf.BUNDLE_REPORT_IP,
					BundleConf.TMSVR_BUNDLE_PORT, BundleConf.TMSVR_BUNDLE_ID,
					this._tmid);
			_taskCollector.Start();
			_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
					BundleConf.TMSVR_BUNDLE_PORT);
			_reportor.Start();
			_queue = new TMSVRQueueThread(_reportor);
			_queue.Start();

			ret = Const.SUCCESS;
		} else {
			Log4j.warn("tmsvr analysisHandler NID[" + _nid + "] <= 0 or TMID["
					+ _tmid + "] <= 0. ");
		}
		return ret;
	}

	public int AnalyseTM(int nid, int tmID) {
		int ret = Const.FAIL;
		TmxFileChunk tmxFile = null;
		TMDAO dao = null;
		TMServiceDAO tmservicedao = null;
		// TMLanguage lang = null;
		FastDFS fastdfs = null;
		try {
			if (nid == _nid && tmID == _tmid) {
				Log4j.info("tmsvr analysisHandler analyseTM[" + tmID
						+ "] start. ");

				dao = new TMDAO();
				dao.Init(true);
				TMBean tmbean;// = dao.SelectForTMID(tmid);

				if (nid == BundleConf.DEFAULT_NID) {
					tmbean = dao.SelectForTMID(tmID);
				} else {
					tmbean = dao.SelectForNodeTMID(tmID);
				}

				if (tmbean == null || tmbean.lang_country == null
						|| tmbean.tmx_file_id == null) {
					Log4j.error("tmsvr analysisHandler tm is null or tm is wrong. ");
					return ret;
				}
				if (tmbean.analyse) {
					Log4j.error("tmsvr analysisHandler tm is analysed. ");
					return ret;
				}
				_lang = DetectTMLanguage.Detect(tmbean.lang_country);
				if (_lang == null) {
					Log4j.error("tmsvr analysisHandler TMLanguage is null.");
					return ret;
				}

				String tmxName = _path + tmID + ".tmx";

				Log4j.info("tmsvr analysisHandler TM Download begin.");

				fastdfs = new FastDFS();
				fastdfs.Init();
				fastdfs.Download(tmbean.tmx_file_id, tmxName);
				fastdfs.UnInit();

				Log4j.info("tmsvr analysisHandler TM Download end.");

				// tmxName = _path + "39.tmx";

				File f = new File(tmxName);
				if (f.exists()) {
					long begintime = System.currentTimeMillis();
					long starttime = begintime;
					tmservicedao = new TMServiceDAO();
					tmservicedao.Init();
					tmservicedao.DropTMIndexIfExists(tmID);
					tmservicedao.CreateTMIndex(tmID);
					if (_lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
						tmservicedao.DropTMWordIfExists(tmID);
						tmservicedao.CreateTMWord(tmID);
						tmservicedao.DropTMTimeIfExists(tmID);
						tmservicedao.CreateTMTime(tmID);
					}
					tmservicedao.DropTMTextIfExists(tmID);
					tmservicedao.CreateTMText(tmID);

					long endtime = System.currentTimeMillis();
					Log4j.debug(String.format(
							"tmsvr analysisHandler tm %d create table %dms.",
							tmID, endtime - begintime));
					tmxFile = new TmxFileChunk();
					_lang.Init(tmxFile);
					// tmxFile.Init(null, tmxName, tmbean.lang_country, "");
					tmxFile.Init(tmxName, _path, "new", "", _lang, 1000, tmID);
					tmxFile.AnalyseTMXFile(0);
					if (_lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
						tmservicedao.CleanUpTM(tmID, 0);
					} else {
						tmservicedao.CleanUpTM(tmID, 1);
					}
					// tmservicedao.UnInit();
					// tmservicedao.Init();
					// tmservicedao.TruncateTime(tmID);
					// tmservicedao.ImportTime(tmID);
					tmservicedao.Commit();// 没有commit，time表不添加数据
					endtime = System.currentTimeMillis();
					Log4j.debug(String.format(
							"tmsvr analysisHandler tm %d analyseTMX %dms.",
							tmID, endtime - begintime));
					tmbean.analyse = true;
					if (nid == BundleConf.DEFAULT_NID) {
						dao.UpdateAnalyse(tmbean);
					} else {
						dao.UpdateAnalyseForNode(tmbean);
					}
					dao.Commit();
					dao.UnInit();
					Log4j.debug(String.format(
							"tmsvr analysisHandler tm %d analyse %dms.", tmID,
							endtime - starttime));
				} else {
					Log4j.error("tmsvr analysisHandler file(" + tmxName
							+ ") not exists.");
				}
			} else {
				Log4j.info("tmsvr analysisHandler NID[param:" + nid + " attr:"
						+ _nid + "] or TMID[param:" + tmID + " attr:" + _tmid
						+ "] error. ");
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
			if (tmservicedao != null) {
				tmservicedao.UnInit();
			}
			_taskCollector.UnRegister();
			Log4j.info("tmsvr analysisHandler analyseTM[" + tmID + "] stop. ");
			try {
				Thread.sleep(5000);
			} catch (Exception e2) {
				Log4j.error(e2);
			}

			System.exit(0);
		}

		ret = Const.SUCCESS;

		return ret;
	}

	public int InitTM(int nID, int tmID) {
		int ret = Const.FAIL;
		if (tmID == _tmid) {
			TMDAO dao = null;
			try {
				Log4j.info("tmsvr analysisHandler  InitTM[" + tmID
						+ "] start. ");
				dao = new TMDAO();
				dao.Init(true);
				TMBean tmbean;

				if (nID == BundleConf.DEFAULT_NID) {
					tmbean = dao.SelectForTMID(tmID);
				} else {
					tmbean = dao.SelectForNodeTMID(tmID);
				}

				if (tmbean == null || tmbean.lang_country == null
						|| tmbean.tmx_file_id == null) {
					Log4j.error("tmsvr analysisHandler tm is null or tm is wrong. ");
					return ret;
				}
				if (!tmbean.analyse) {
					Log4j.error("tmsvr analysisHandler tm is not analysed. ");
					return ret;
				}
				_lang = DetectTMLanguage.Detect(tmbean.lang_country);
				// _lang = new TMENUS();
				if (_lang == null) {
					Log4j.error("tmsvr analysisHandler TMLanguage is null.");
					return ret;
				}
				// ThreadUtility.Sleep(5000);
				if (_lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
					_lang.ReadTMChunk(tmID);
				}
				_queue.SetTMLang(_lang);
				Log4j.info("tmsvr analysisHandler InitTM[" + tmID + "] stop. ");
				ret = Const.SUCCESS;
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao != null) {
					dao.UnInit();
				}
			}
		} else {
			Log4j.info("tmsvr analysisHandler TMID[param:" + tmID + " attr:"
					+ _tmid + "] error. ");
		}
		return ret;
	}

	public int Run() {
		int ret = Const.FAIL;
		Log4j.log("tmsvr analysisHandler TM[" + _tmid + "] is running. ");
		while (true) {
			ArrayList<JSONObject> tasks = _taskCollector.TakeTasks();
			if (tasks != null) {
				// 将tasks投入到消息处理队列，类似state后增加的任务类处理方式
				for (JSONObject task : tasks) {
					Log4j.log("tmsvr analysisHandler " + task.toString());

					int tmID = Util.GetIntFromJSon("tmid", task);
					int nid = Util.GetIntFromJSon("nid", task);

					if (tmID > 0 && nid > 0) {
						if (tmID == _tmid) {
							String method = Util.GetStringFromJSon("method",
									task);
							if (method.equalsIgnoreCase("DELETE")) {
								_taskCollector.UnRegister();
								Log4j.info("tmsvr analysisHandler uninit TM["
										+ tmID + "] stop. ");
								try {
									Thread.sleep(5000);
								} catch (Exception e2) {
									Log4j.error(e2);
								}
							}

							_queue.Push(task);
						} else {
							SendToPHP(task, "FAILED");
							Log4j.info("tmsvr analysisHandler TMID[param:"
									+ tmID + " attr:" + _tmid + "] error. ");
						}
					} else {
						SendToPHP(task, "FAILED");
						Log4j.warn("tmsvr analysisHandler tmid or nid is null. ");
					}
				}
			} else {
				Log4j.log("AnalysisHandler Run tasks is null.");
				break;
			}
		}

		return ret;
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
}
