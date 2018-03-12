package com.wiitrans.analysis.bolt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.DictLangDAO;
import com.wiitrans.base.db.model.DictLangBean;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class SplitHandlerBolt extends BaseBasicBolt {

	private TaskReportor _reportor = null;
	public HashMap<Integer, Language> _languages = null;
	public String _path = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {

		WiitransConfig.getInstance(0);

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.ANALYSIS_BUNDLE_PORT);
		_reportor.Start();

		if (_path == null) {
			//BundleParam param = app._bundles.get("anlyTopo");
			BundleParam param = WiitransConfig.getInstance(0).ANALY;
			_path = param.BUNDLE_TEMPFILE_PATH;
		}

		if (_languages == null) {
			_languages = new HashMap<Integer, Language>();
			java.util.List<DictLangBean> list = null;

			DictLangDAO langdao = null;

			try {
				langdao = new DictLangDAO();
				langdao.Init(true);
				list = langdao.SelectAll();
				langdao.UnInit();
			} catch (Exception e) {
				// e.printStackTrace();
				Log4j.error(e);
			} finally {
				if (langdao != null) {
					langdao.UnInit();
				}
			}

			DetectLanguage dect = new DetectLanguage();
			Language lang;
			if (list != null) {
				for (DictLangBean dictLangBean : list) {
					lang = dect.Detect(dictLangBean.class_name);
					if (lang != null) {
						_languages.put(dictLangBean.language_id, lang);
					}
				}
			}
		}
	}

	public void Split(JSONObject json) {
		boolean flag = false;
		JSONObject resObj = new JSONObject();

		String fileid = json.getString("fid");
		String originalFileName = json.getString("fname");
		int lang = Util.GetIntFromJSon("lang", json);

		// String path = "/root/Desktop/fenxi/";
		FileUtil fileutil = new FileUtil();
		String ext = fileutil.GetExtFromFileName(fileid);
		if (ext != null) {

			String file_name = java.util.UUID.randomUUID().toString();
			// fileid.substring(0,index);

			if (_languages.containsKey(lang)) {

				Language language = _languages.get(lang);
				// fileid = "/root/Desktop/test.doc";

				BiliFileNoTag file = fileutil.GetBiliFileNoTagByExt(ext);
				if (file != null) {
					// file._tagId = "☂";

					String xmlName = _path + file_name + ".xml";
					String sourceName = _path + file_name + "_source" + "."
							+ ext;
					String targetName = _path + file_name + "_target" + "."
							+ ext;
					FastDFS fastdfs = null;
					try {

						fastdfs = new FastDFS();
						fastdfs.Init();
						fastdfs.Download(fileid, sourceName);

						file.Init(sourceName, targetName, xmlName,
								originalFileName, language, null);

						file.Parse();

						file.Save();

						String bilifileid = fastdfs.Upload(xmlName, "xml");
						fastdfs.UnInit();

						file.ParseBili();
						// 文件字数
						int count = file._wordCount;
						File temp = new File(sourceName);
						long len = temp.length();

						resObj.put("fileid", bilifileid);
						resObj.put("wordcount", String.valueOf(count));
						resObj.put("length", String.valueOf(len));
						flag = true;
					} catch (Exception e) {
						Log4j.error(e.getMessage());
					} finally {
						if (fastdfs != null) {
							fastdfs.UnInit();
						}
						if (file != null) {
							file.UnInit();
						}
					}
				} else {
					Log4j.warn(fileid + " the bilifile type of ext(" + ext
							+ ") not exists. ");
				}
			} else {
				Log4j.warn(fileid + " langID(" + lang + ") not exists. ");
			}
		} else {
			Log4j.warn(fileid + " ext not exists. ");
		}

		if (flag) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, json));
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, json));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, json));
		_reportor.Report(resObj);
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		String taskStr = tuple.getStringByField("task");
		JSONObject task = new JSONObject(taskStr);

		Log4j.log("splithandlerbolt " + task.toString());
		//根据语言对、文件系统的id、源文件类型生成双语文件并返回字数和大小的bolt
		this.Split(task);

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("id", "fid", "lang"));
	}

}
