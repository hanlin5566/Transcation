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
import com.wiitrans.base.db.MatchServiceDAO;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.OrderFileDAO;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.file.PreprocessFile;
import com.wiitrans.base.file.PreprocessSentence;
import com.wiitrans.base.file.PreprocessTerm;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.filesystem.FastDFS;
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

public class KeywordBolt extends BaseBasicBolt {
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

	private int getScore(int count) {
		int value;
		if (count <= 0) {
			value = 0;
		} else if (count >= 11) {
			value = 200;
		} else {
			value = 100 + (count - 1) * 10;
		}
		return value;
	}

	private int addScoreByAllOrder(JSONObject obj) {
		int ret = Const.FAIL;
		OrderDAO dao = null;
		try {
			dao = new OrderDAO();
			dao.Init(true);
			// dao.sele
			SendToPHP(obj, "OK");
		} catch (Exception e) {
			// TODO: handle exception
			SendToPHP(obj, "FAILED");
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	private int addScoreByOrder(JSONObject obj) {
		int ret = Const.FAIL;
		int order_id = Util.GetIntFromJSon("order_id", obj);
		if (order_id <= 0) {
			SendToPHP(obj, "FAILED");
		}
		OrderDAO dao = null;
		OrderFileDAO filedao = null;
		MatchServiceDAO matchservicedao = null;
		try {
			dao = new OrderDAO();
			dao.Init(true);
			OrderBean order = dao.Select(order_id);
			if (order != null && !order.add_recom_score) {
				int translatorID = order.translator_id;
				int editorID = order.editor_id;
				int pairID = order.pair_id;
				int price_level_id = order.price_level_id;
				order.add_recom_score = true;
				dao.UpdateAddRecomScore(order);
				dao.Commit();
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

							String preprocessFileName = _path
									+ "preprocessFile" + order.order_id + "_"
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
								.append(translatorID)
								.append('\t')
								.append(pairID)
								.append('\t')
								.append(keyword.keyword.replaceAll(
										"\\t|\\r|\\n", " ")).append('\t')
								.append(getScore(keyword.count)).append('\n');
						if (price_level_id > 1) {
							sbKeyword
									.append(order_id)
									.append('\t')
									.append(editorID)
									.append('\t')
									.append(pairID)
									.append('\t')
									.append(keyword.keyword.replaceAll(
											"\\t|\\r|\\n", " ")).append('\t')
									.append(getScore(keyword.count))
									.append('\n');
						}
					}

					LoadDataInfile infile = new LoadDataInfile();
					String filePath = _path + "order" + order_id
							+ "datafile.txt";
					infile.CreateFile(filePath);
					infile.WriteAppend(filePath, sbKeyword.toString());

					matchservicedao = new MatchServiceDAO();
					matchservicedao.Init();
					matchservicedao.ImportOrderKeyword(filePath);
					matchservicedao.Commit();
					matchservicedao.AddKeywordScore(order_id);
					matchservicedao.Commit();
					// System.out.println();
				}
				// System.out.println();
			}
			SendToPHP(obj, "OK");
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

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String taskStr = tuple.getStringByField("content");
		JSONObject task = new JSONObject(taskStr);

		String aid = task.getString("aid");
		String method = Util.GetStringFromJSon("method", task);
		Log4j.log("keywordbolt " + task.toString());
		//订单提交时，自动把译员对应的keyword得分追加到译员信息中
		switch (aid) {
		case "order": {
			switch (method) {
			case "POST": {
				addScoreByOrder(task);
				break;
			}
			case "PUT": {
				// addScoreByAllOrder(task);
				SendToPHP(task, "OK");
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
