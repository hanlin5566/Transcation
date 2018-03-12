package com.wiitrans.tmsvr.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.OrderFileDAO;
import com.wiitrans.base.db.TMDAO;
import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.db.model.TMBean;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.xml.WiitransConfig;

public class TMAddQueueThread extends Thread {
	private LinkedBlockingQueue<JSONObject> _queue = new LinkedBlockingQueue<JSONObject>();
	private String _path = null;

	public int Start() {
		int ret = Const.FAIL;
		WiitransConfig.getInstance(2);

		if (_path == null) {
			BundleParam param = WiitransConfig.getInstance(2).TMSVR;
			_path = param.BUNDLE_TEMPFILE_PATH;
			Log4j.log("          tmadd-tmxpath= " + _path);
		}

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

	public int AddTM(int order_id, int tmid) {
		int ret = Const.FAIL;
		OrderDAO orderdao = null;
		OrderFileDAO orderfiledao = null;
		TMDAO tmdao = null;
		TMServiceDAO tmservicedao = null;
		FastDFS fastdfs = null;

		try {
			orderdao = new OrderDAO();
			orderdao.Init(true);
			OrderBean order = orderdao.Select(order_id);
			orderdao.UnInit();
			if (order == null) {
				Log4j.log("tmadd queuethread order is null or order is not this node.");
				return ret;
			}
			tmdao = new TMDAO();
			tmdao.Init(true);
			TMBean tmbean = tmdao.SelectForTMID(tmid);
			tmdao.UnInit();
			if (tmbean == null) {
				Log4j.log("tmadd queuethread tm is null or tm is not this node.");
				return ret;
			}

			if (tmbean.lang_country == null) {
				Log4j.error("tmadd queuethread tm lang_country is wrong. ");
				return ret;
			}

			TMLanguage lang = DetectTMLanguage.Detect(tmbean.lang_country);
			if (lang == null) {
				Log4j.error("tmadd queuethread TMLanguage is null.");
				return ret;
			}
			List<OrderFileBean> orderFileList = null;

			orderfiledao = new OrderFileDAO();
			orderfiledao.Init(true);
			List<OrderFileBean> filelist = orderfiledao
					.SelectByOrderID(order_id);
			orderfiledao.UnInit();
			if (filelist == null || filelist.size() == 0) {
				Log4j.log("tmadd queuethread have not file. ");
				return ret;
			}

			orderFileList = new ArrayList<OrderFileBean>();
			for (OrderFileBean orderFileBean : filelist) {
				if (orderFileBean.tmx_file_id != null
						&& orderFileBean.tmx_file_id.trim().length() > 0) {
					orderFileList.add(orderFileBean);
				}
			}

			if (orderFileList == null || orderFileList.size() == 0) {
				Log4j.log("tmadd queuethread have not tmx file. ");
				return ret;
			}

			tmservicedao = new TMServiceDAO();
			tmservicedao.Init();

			for (OrderFileBean orderFileBean : orderFileList) {
				int index = tmservicedao.SelectTextMaxID(tmid);
				String prefix = "add";
				String suffix = "_file_" + orderFileBean.file_id;
				String tmxName = _path + prefix + tmid + suffix + ".tmx";

				fastdfs = new FastDFS();
				fastdfs.Init();
				fastdfs.Download(orderFileBean.tmx_file_id, tmxName);
				fastdfs.UnInit();

				File f = new File(tmxName);
				if (f.exists()) {
					TmxFileChunk tmxFile = new TmxFileChunk();
					lang.UnInit();
					lang.Init(tmxFile);
					// tmxFile.Init(null, tmxName, tmbean.lang_country, "");
					tmxFile.Init(tmxName, _path, prefix, suffix, lang, 1000,
							tmid);
					tmxFile.AnalyseTMXFile(index);
				}
			}
			if (lang != null && lang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
				tmservicedao.CleanUpTM(tmid, 0);
			} else {
				tmservicedao.CleanUpTM(tmid, 1);
			}
			// tmservicedao.TruncateTime(tmid);
			// tmservicedao.ImportTime(tmid);
			tmservicedao.Commit();
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (orderdao != null) {
				orderdao.UnInit();
			}

			if (orderfiledao != null) {
				orderfiledao.UnInit();
			}

			if (tmdao != null) {
				tmdao.UnInit();
			}

			if (tmservicedao != null) {
				tmservicedao.UnInit();
			}
			if (fastdfs != null) {
				fastdfs.UnInit();
			}
		}
		ret = Const.SUCCESS;
		return ret;
	}

	public int Request(JSONObject msg) {
		int ret = Const.FAIL;
		Log4j.log("tmadd queuethread " + msg.toString());

		try {
			String aid = Util.GetStringFromJSon("aid", msg);
			String method = Util.GetStringFromJSon("method", msg);
			String uid = Util.GetStringFromJSon("uid", msg);
			String sid = Util.GetStringFromJSon("sid", msg);
			int tmid = Util.GetIntFromJSon("tmid", msg);
			int order_id = Util.GetIntFromJSon("order_id", msg);

			int tnid = Util.GetIntFromJSon("tnid", msg);

			if (tnid != BundleConf.DEFAULT_NID) {
				Log4j.log("tmadd queuethread tnid: " + tnid + " not this node");
				return ret;
			}

			if (tmid <= 0 || order_id <= 0) {
				Log4j.log("tmadd queuethread tmid or order_id error "
						+ msg.toString());
				return ret;
			}

			ret = AddTM(order_id, tmid);

			JSONObject params = null;
			params = new JSONObject();
			params.put("sid", Util.GetStringFromJSon("sid", msg));
			params.put("uid", Util.GetStringFromJSon("uid", msg));
			params.put("nid", String.valueOf(tnid));
			params.put("tnid", String.valueOf(tnid));
			params.put("tmid", String.valueOf(tmid));
			String url = BundleConf.BUNDLE_TMSVR_API + "analysis";
			Log4j.log("start invoke TMSVR_API " + url);

			params.put("method", "DELETE");
			new HttpSimulator(url).executeMethodTimeOut(params.toString(), 2);

			// 关闭需要5秒等待时间
			Thread.sleep(5000);

			params.put("method", "PUT");
			new HttpSimulator(url).executeMethodTimeOut(params.toString(), 2);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
		}
		return ret;
	}
}
