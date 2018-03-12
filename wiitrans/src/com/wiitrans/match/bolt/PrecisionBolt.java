package com.wiitrans.match.bolt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.db.DictLangDAO;
import com.wiitrans.base.db.MatchServiceDAO;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.OrderFileDAO;
import com.wiitrans.base.db.model.DictLangPairBean;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.db.model.TransScoreBean;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.PreprocessFile;
import com.wiitrans.base.file.PreprocessSentence;
import com.wiitrans.base.file.PreprocessTerm;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.match.Keyword;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.tm.LoadDataInfile;
import com.wiitrans.base.xml.WiitransConfig;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class PrecisionBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;
	private String _path = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		BundleParam param = WiitransConfig.getInstance(0).MATCH;

		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.MATCH_BUNDLE_PORT);
		_reportor.Start();
		if (_path == null) {
			_path = param.BUNDLE_TEMPFILE_PATH;
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

	private int getTransByOrder(JSONObject obj) {
		int ret = Const.FAIL;
		int order_id = Util.GetIntFromJSon("order_id", obj);
		if (order_id <= 0) {
			SendToPHP(obj, "FAILED");
		}
		JSONObject resObj = new JSONObject();
		OrderDAO dao = null;
		OrderFileDAO filedao = null;
		MatchServiceDAO matchservicedao = null;
		try {
			String translatorIDs = Util.GetStringFromJSon("translatorIDs", obj);
			String trantype = Util.GetStringFromJSon("trantype", obj);
			dao = new OrderDAO();
			dao.Init(true);
			OrderBean order = dao.Select(order_id);
			if (order != null) {
				int pairID = order.pair_id;
				int industry_id = order.industry_id;
				int price_level_id = order.price_level_id;
				dao.UnInit();

				filedao = new OrderFileDAO();
				filedao.Init(true);
				List<OrderFileBean> list = filedao.SelectByOrderID(order_id);
				filedao.UnInit();
				if (list != null && list.size() > 0) {
					FastDFS dfs = null;
					HashMap<String, Keyword> keywordmap = new HashMap<String, Keyword>();
					for (OrderFileBean orderFileBean : list) {

						dfs = new FastDFS();
						if (orderFileBean.preprocess_file_id != null
								&& orderFileBean.preprocess_file_id.trim()
										.length() > 0) {

							String preprocessFileName = _path + "neworder"
									+ order.order_id + "_"
									+ orderFileBean.file_id + ".xml";
							// 下载并初始化双语文件
							dfs.Init();
							dfs.Download(orderFileBean.preprocess_file_id,
									preprocessFileName);

							File f = new File(preprocessFileName);
							if (f.exists()) {
								PreprocessFile preprocessFile = new PreprocessFile(
										preprocessFileName);
								if (preprocessFile != null) {
									ArrayList<PreprocessSentence> presentencelist = preprocessFile
											.GetSentecenList();
									for (PreprocessSentence sent : presentencelist) {
										if (sent != null
												&& sent.termList != null
												&& sent.termList.size() > 0) {
											for (PreprocessTerm preprocessTerm : sent.termList) {
												if (preprocessTerm != null
														&& preprocessTerm.term != null) {
													String keyword = preprocessTerm.term
															.trim();
													int count;
													if (preprocessTerm.count < 1) {
														count = 1;
													} else {
														count = preprocessTerm.count;
													}
													if (keywordmap
															.containsKey(keyword)) {
														Keyword tmp = keywordmap
																.get(keyword);

														tmp.count += count;
													} else {
														Keyword tmp = new Keyword();
														tmp.keyword = keyword;
														tmp.count = count;
														keywordmap.put(keyword,
																tmp);
													}
												}
											}
										}
									}
								}
							}
						}
					}

					StringBuffer sbKeyword = new StringBuffer();
					Set<String> set = keywordmap.keySet();
					for (String str : set) {
						Keyword keyword = keywordmap.get(str);
						sbKeyword
								.append(order_id)
								.append('\t')
								.append(pairID)
								.append('\t')
								.append(keyword.keyword.replaceAll(
										"\\t|\\r|\\n", " ")).append('\t')
								.append(keyword.count).append('\n');
					}

					LoadDataInfile infile = new LoadDataInfile();
					String filePath = _path + "neworder" + order_id
							+ "datafile.txt";
					infile.CreateFile(filePath);
					infile.WriteAppend(filePath, sbKeyword.toString());

					matchservicedao = new MatchServiceDAO();
					matchservicedao.Init();
					matchservicedao.DeleteNewOrderKeyword(order_id);
					matchservicedao.ImportNewOrderKeyword(filePath);
					matchservicedao.Commit();
					JSONObject recom_t = new JSONObject();
					JSONObject recom_e = new JSONObject();
					if (translatorIDs == null
							|| translatorIDs.trim().length() == 0) {
						if (trantype == null || trantype.trim().length() == 0
								|| trantype.equalsIgnoreCase("T")) {
							// t推荐
							List<TransScoreBean> translist = matchservicedao
									.SelectTopTrans(
											order_id,
											industry_id,
											price_level_id,
											WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);
							for (int i = 0; i < translist.size(); i++) {
								TransScoreBean transScoreBean = translist
										.get(i);
								JSONObject trans = new JSONObject();
								trans.put("uid", String
										.valueOf(transScoreBean.translator_id));
								trans.put("score",
										String.valueOf(transScoreBean.score));
								recom_t.put(String.valueOf(i), trans);
							}
							resObj.put("recom", recom_t);
						} else {
							if (trantype.equalsIgnoreCase("E")) {
								// e推荐
								List<TransScoreBean> editslist = matchservicedao
										.SelectTopEdits(
												order_id,
												industry_id,
												price_level_id,
												WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);

								for (int i = 0; i < editslist.size(); i++) {
									TransScoreBean editsScoreBean = editslist
											.get(i);
									JSONObject edits = new JSONObject();
									edits.put(
											"uid",
											String.valueOf(editsScoreBean.translator_id));
									edits.put("score", String
											.valueOf(editsScoreBean.score));
									recom_e.put(String.valueOf(i), edits);
								}
								resObj.put("recom", recom_e);
							}
						}
					} else {
						String[] translatorIDary = translatorIDs.split(",");
						int[] transIDs = new int[translatorIDary.length];
						for (int i = 0; i < translatorIDary.length; i++) {
							transIDs[i] = Util.String2Int(translatorIDary[i]);
						}
						List<TransScoreBean> translist = matchservicedao
								.SelectTrans(
										order_id,
										industry_id,
										price_level_id,
										transIDs,
										WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);
						if (translist != null && translist.size() > 0) {
							for (int i = 0; i < translist.size(); i++) {
								TransScoreBean transScoreBean = translist
										.get(i);
								if (transScoreBean != null) {
									JSONObject trans = new JSONObject();
									trans.put(
											"uid",
											String.valueOf(transScoreBean.translator_id));
									trans.put("score", String
											.valueOf(transScoreBean.score));
									recom_t.put(
											String.valueOf(transScoreBean.translator_id),
											trans);
								}
							}
						}
						for (String translator_id : translatorIDary) {
							JSONObject trans = Util.GetJSonFromJSon(
									translator_id, recom_t);
							if (trans == null) {
								trans = new JSONObject();
								trans.put("uid", translator_id);
								trans.put("score", "10");
								recom_t.put(translator_id, trans);
							}
						}
						resObj.put("recom_t", recom_t);
					}
				}
				// System.out.println();
			}

			resObj.put("result", "OK");
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);
		} catch (Exception e) {
			// TODO: handle exception
			SendToPHP(obj, "FAILED");
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
			if (filedao != null) {
				filedao.UnInit();
			}
			if (matchservicedao != null) {
				matchservicedao.UnInit();
			}
		}
		return ret;
	}

	private int getTransNoOrder(JSONObject obj) {
		int ret = Const.FAIL;

		JSONObject resObj = new JSONObject();
		MatchServiceDAO matchservicedao = null;
		String request_id = Util.GetStringFromJSon("id", obj);
		String java_id = request_id.substring(request_id.lastIndexOf('@') + 1);

		try {
			int pairID = Util.GetIntFromJSon("pair_id", obj);
			int industry_id = Util.GetIntFromJSon("industry_id", obj);
			int price_level_id = Util.GetIntFromJSon("price_level_id", obj);
			int term_group_id = Util.GetIntFromJSon("term_group_id", obj);
			String translatorIDs = Util.GetStringFromJSon("translatorIDs", obj);

			String trantype = Util.GetStringFromJSon("trantype", obj);

			// 所有文件
			JSONObject files_json = Util.GetJSonFromJSon("files_json", obj);
			// 调用term服务分析keyword数量
			String url = BundleConf.BUNDLE_Node.get(BundleConf.DEFAULT_NID).api
					+ "term/AnalyseFileTerm";
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", Util.GetStringFromJSon("sid", obj));
			jsonObject.put("uid", Util.GetStringFromJSon("uid", obj));
			jsonObject.put("nid", Util.GetStringFromJSon("nid", obj));
			jsonObject.put("pair_id", Util.GetStringFromJSon("pair_id", obj));
			jsonObject.put("industry_id", String.valueOf(industry_id));
			jsonObject.put("term_group_id", String.valueOf(term_group_id));
			jsonObject.put("method", "POST");
			jsonObject.put("files_json", files_json);
			JSONObject javaresult = new HttpSimulator(url)
					.executeMethodJSONTimeOut(jsonObject.toString(), 10);
			JSONObject keywordjson = Util
					.GetJSonFromJSon("keyword", javaresult);
			Log4j.info("http-keyword-json:" + keywordjson.toString());
			// 获得keyword
			String[] keywords = JSONObject.getNames(keywordjson);
			StringBuffer sbKeyword = new StringBuffer();
			JSONObject recom_t = new JSONObject();
			JSONObject recom_e = new JSONObject();
			if (keywords != null && keywords.length > 0) {
				for (String keyword : keywords) {
					int count = Util.GetIntFromJSon(keyword, keywordjson);
					sbKeyword.append(java_id).append('\t').append(pairID)
							.append('\t')
							.append(keyword.replaceAll("\\t|\\r|\\n", " "))
							.append('\t').append(count).append('\n');
				}

				// keyword保存再临时文件中
				LoadDataInfile infile = new LoadDataInfile();
				String filePath = _path + "noorder" + java_id + "datafile.txt";
				Log4j.info("filePath:" + filePath);
				infile.CreateFile(filePath);
				infile.WriteAppend(filePath, sbKeyword.toString());

				// keyword导入数据库中
				matchservicedao = new MatchServiceDAO();
				matchservicedao.Init();
				matchservicedao.DeleteNoOrderKeyword(java_id);
				matchservicedao.ImportNoOrderKeyword(filePath);
				matchservicedao.Commit();
				if (translatorIDs == null || translatorIDs.trim().length() == 0) {

					if (price_level_id <= 1 || trantype == null
							|| trantype.trim().length() == 0
							|| trantype.equalsIgnoreCase("T")) {
						// t推荐
						List<TransScoreBean> translist = matchservicedao
								.SelectTopTransByJavaID(
										java_id,
										industry_id,
										price_level_id,
										WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);
						for (int i = 0; i < translist.size(); i++) {
							TransScoreBean transScoreBean = translist.get(i);
							JSONObject trans = new JSONObject();
							trans.put("uid", String
									.valueOf(transScoreBean.translator_id));
							trans.put("score",
									String.valueOf(transScoreBean.score));
							recom_t.put(String.valueOf(i), trans);
						}
						resObj.put("recom_t", recom_t);
					}

					if (price_level_id > 1
							&& (trantype == null
									|| trantype.trim().length() == 0 || trantype
										.equalsIgnoreCase("E"))) {
						// e推荐
						List<TransScoreBean> editslist = matchservicedao
								.SelectTopEditsByJavaID(
										java_id,
										industry_id,
										price_level_id,
										WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);

						for (int i = 0; i < editslist.size(); i++) {
							TransScoreBean editsScoreBean = editslist.get(i);
							JSONObject edits = new JSONObject();
							edits.put("uid", String
									.valueOf(editsScoreBean.translator_id));
							edits.put("score",
									String.valueOf(editsScoreBean.score));
							recom_e.put(String.valueOf(i), edits);
						}
						resObj.put("recom_e", recom_e);
					}
				} else {
					String[] translatorIDary = translatorIDs.split(",");
					int[] transIDs = new int[translatorIDary.length];
					for (int i = 0; i < translatorIDary.length; i++) {
						transIDs[i] = Util.String2Int(translatorIDary[i]);
					}
					List<TransScoreBean> translist = matchservicedao
							.SelectTransByJavaID(
									java_id,
									industry_id,
									price_level_id,
									transIDs,
									WiitransConfig.getInstance(0).MATCH.BUNDLE_MATCH_VARIABLE);
					if (translist != null && translist.size() > 0) {
						for (int i = 0; i < translist.size(); i++) {
							TransScoreBean transScoreBean = translist.get(i);
							if (transScoreBean != null) {
								JSONObject trans = new JSONObject();
								trans.put("uid", String
										.valueOf(transScoreBean.translator_id));
								trans.put("score",
										String.valueOf(transScoreBean.score));
								recom_t.put(String
										.valueOf(transScoreBean.translator_id),
										trans);
							}
						}
					}
					for (String translator_id : translatorIDary) {
						JSONObject trans = Util.GetJSonFromJSon(translator_id,
								recom_t);
						if (trans == null) {
							trans = new JSONObject();
							trans.put("uid", translator_id);
							trans.put("score", "10");
							recom_t.put(translator_id, trans);
						}
					}
					resObj.put("recom_t", recom_t);
				}
			}

			resObj.put("result", "OK");
			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));

			return _reportor.Report(resObj);
		} catch (Exception e) {
			SendToPHP(obj, "FAILED");
			Log4j.error(e);
		} finally {
			if (matchservicedao != null) {
				matchservicedao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String taskStr = tuple.getStringByField("content");
		JSONObject task = new JSONObject(taskStr);

		String aid = task.getString("aid");
		String method = Util.GetStringFromJSon("method", task);
		Log4j.log("precisionbolt " + task.toString());

		switch (aid) {
		case "order": {
			switch (method) {
			case "POST": {
				collector.emit(new Values(task.toString()));
				break;
			}
			case "PUT": {
				// 根据订单编号查询最匹配的译员【20160628暂时无用】
				getTransByOrder(task);
				break;
			}
			case "GET": {
				// 根据javaid查询最匹配的译员【即请求中id变量中@后面的数据】
				getTransNoOrder(task);
				break;
			}
			default:
				SendToPHP(task, "OK");
				break;
			}

			break;
		}

		default:
			SendToPHP(task, "OK");
			break;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("content"));
	}
}
