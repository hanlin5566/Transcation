package com.wiitrans.term.bolt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.cache.ICache;
import com.wiitrans.base.cache.RedisCache;
import com.wiitrans.base.db.DictIndustryDAO;
import com.wiitrans.base.db.DictLangDAO;
import com.wiitrans.base.db.DictTermDAO;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.OrderFileDAO;
import com.wiitrans.base.db.ProcPreprocessDAO;
import com.wiitrans.base.db.TermGroupDAO;
import com.wiitrans.base.db.model.DictIndustryBean;
import com.wiitrans.base.db.model.DictLangPairBean;
import com.wiitrans.base.db.model.DictTermBean;
import com.wiitrans.base.db.model.DictTermDetailsBean;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.db.model.TermCustoBean;
import com.wiitrans.base.db.model.TermCustoGroupBean;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.PreprocessFile;
import com.wiitrans.base.file.PreprocessSentence;
import com.wiitrans.base.file.PreprocessTerm;
import com.wiitrans.base.file.PreprocessTermDetail;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.lang.MachineTranslation;
import com.wiitrans.base.file.lang.MachineTranslation.TRANS_TYPE;
import com.wiitrans.base.file.lang.MakeWordTree;
import com.wiitrans.base.file.lang.TextTerm;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.notag.BiliFileNoTag.ENTITY_FILE_TYPE;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.match.Keyword;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
//import com.wiitrans.base.xml.AppConfig;
// [预处理]
// 根据输入fid，到文件系统下载相应xml双语文件，解析源句子，逐句进行如下处理，
// 将处理后的结果存入相应预处理xml文件，并上传到文件系统，将预处理文件fid存入db相应字段。
import com.wiitrans.base.xml.WiitransConfig;

// 1.机器翻译处理:根据当前支持选择不同机器翻译处理引擎（东大、微软），将源句子翻译后保存到该句子推荐译文变量
// 2.术语预处理：根据当前语言对下，当前领域下优质术语库进行术语识别，将识别到的多个术语id标识保存到该句子推荐术语变量。
// 优质术语库不会经常变化，从db中读取即可。

// [术语实时查询]
// 返回当前语言对下全部领域100%匹配成功的术语解释
// 1.从当前语言对下优质术语库中匹配。
// 2.从当前语言对下译员贡献术语缓存(Redis)中读取。Redis中译员贡献术语库结构参见PersistenceBolt.java

class PairDictTermDetails {
	public int pair_id;
	public int last_update_time = 0;
	HashMap<String, HashMap<String, List<DictTermDetailsBean>>> map;
}

public class TermBolt extends BaseBasicBolt {
	private TaskReportor _reportor = null;

	private HashMap<String, HashMap<String, MakeWordTree>> _map;
	private HashMap<String, MakeWordTree> _mapCusto;
	private HashMap<String, Language> _pairSourceLangMap;
	private HashMap<String, String> _industryMap;

	private HashMap<String, PairDictTermDetails> _mapDictTerm;

	private int termtimeout = 3600;
	private ICache _cache = null;
	private String _path = null;

	@Override
	public void prepare(Map conf, TopologyContext context) {

		WiitransConfig.getInstance(0);
		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.TERM_BUNDLE_PORT);

		_reportor.Start();

		if (_cache == null) {
			_cache = new RedisCache();
			_cache.Init(BundleConf.BUNDLE_REDIS_IP);
		}

		// 语言对对应的原语言对象
		if (_pairSourceLangMap == null) {
			DictLangDAO dictlangdao = null;
			try {
				dictlangdao = new DictLangDAO();
				dictlangdao.Init(true);
				List<DictLangPairBean> list = dictlangdao.SelectAllPair();
				dictlangdao.UnInit();
				_pairSourceLangMap = new HashMap<String, Language>();

				DetectLanguage detectLanguage = new DetectLanguage();
				Language sourceLang;
				for (DictLangPairBean dictLangPairBean : list) {
					sourceLang = detectLanguage
							.Detect(dictLangPairBean.source_class_name);
					if (dictLangPairBean.pair_id > 0 && sourceLang != null) {
						_pairSourceLangMap.put(
								String.valueOf(dictLangPairBean.pair_id),
								sourceLang);
					} else {
						Log4j.error("语言对(" + dictLangPairBean.pair_id
								+ ")不存在或语言对对应的原语言("
								+ dictLangPairBean.source_class_name
								+ ")没有语言类对象");
					}
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dictlangdao != null) {
					dictlangdao.UnInit();
				}
			}
		}

		// industry的id和name的对应关系，提前查询出来，不用再每次都查询数据库
		if (_industryMap == null) {
			DictIndustryDAO dictindustrydao = null;
			try {
				dictindustrydao = new DictIndustryDAO();
				dictindustrydao.Init(true);
				List<DictIndustryBean> list = dictindustrydao.SelectAll();
				dictindustrydao.UnInit();
				_industryMap = new HashMap<String, String>();
				for (DictIndustryBean dictIndustryBean : list) {
					_industryMap.put(
							String.valueOf(dictIndustryBean.industry_id),
							dictIndustryBean.industry_name);
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dictindustrydao != null) {
					dictindustrydao.UnInit();
				}
			}

		}

		// 每个语言对每个industry有一个优质术语树MakeWordTree，用来分解String中的优质术语
		if (_map == null) {
			_map = new HashMap<String, HashMap<String, MakeWordTree>>();

			DictTermDAO termdao = null;

			try {
				int newTime = Util.GetIntFromNow();
				termdao = new DictTermDAO();
				termdao.Init(true);
				List<DictTermBean> list = termdao.SelectAllTerm();
				termdao.UnInit();

				MakeTerm(list, newTime);

			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (termdao != null) {
					termdao.UnInit();
				}
			}
		}

		// 每个语言对每个industry每一个用户有一个优质术语树MakeWordTree，用来分解String中的优质术语
		if (_mapCusto == null) {
			_mapCusto = new HashMap<String, MakeWordTree>();

			TermGroupDAO termgroupdao = null;

			try {
				int newTime = Util.GetIntFromNow();
				termgroupdao = new TermGroupDAO();
				termgroupdao.Init(true);
				List<TermCustoGroupBean> list = termgroupdao
						.SelectAllTermCustoGroup();
				Language lang;
				for (TermCustoGroupBean termCustoGroup : list) {
					if (!_mapCusto.containsKey(String
							.valueOf(termCustoGroup.term_group_id))) {
						lang = _pairSourceLangMap.get(String
								.valueOf(termCustoGroup.pair_id));
						MakeWordTree treetemp = new MakeWordTree(lang);
						treetemp.last_update_time = 0;
						_mapCusto.put(
								String.valueOf(termCustoGroup.term_group_id),
								treetemp);
					}
					MakeWordTree tree = _mapCusto.get(String
							.valueOf(termCustoGroup.term_group_id));
					if (tree != null) {
						List<TermCustoBean> termCustoList = termgroupdao
								.SelectForUpdateTime(
										termCustoGroup.term_group_id,
										tree.last_update_time);
						if (termCustoList != null && termCustoList.size() > 0) {
							tree.MakeCusto(termCustoList);
							tree.last_update_time = newTime;
						}
					}

				}

				termgroupdao.UnInit();

			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (termgroupdao != null) {
					termgroupdao.UnInit();
				}
			}
		}

		// 语言对------行业领域------术语（用来查看优质术语是否存在）
		if (_mapDictTerm == null) {
			_mapDictTerm = new HashMap<String, PairDictTermDetails>();

			DictTermDAO termdao1 = null;

			try {
				termdao1 = new DictTermDAO();
				termdao1.Init(true);
				List<DictTermBean> dicttermlist = termdao1.SelectAllTerm();
				List<DictTermDetailsBean> dictTermDetailsList = termdao1
						.SelectAllTermDetails();
				HashMap<String, DictTermDetailsBean> dictTermDetailsSet = new HashMap<String, DictTermDetailsBean>();
				for (DictTermDetailsBean dictTermDetailsBean : dictTermDetailsList) {
					dictTermDetailsSet.put(
							String.valueOf(dictTermDetailsBean.term_id),
							dictTermDetailsBean);
				}
				termdao1.UnInit();
				this.AddTermDetails(dicttermlist, dictTermDetailsSet, 0);
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (termdao1 != null) {
					termdao1.UnInit();
				}
			}

		}
		// BundleParam param = app._bundles.get("termTopo");
		BundleParam param = WiitransConfig.getInstance(0).TERM;

		if (_path == null) {
			_path = param.BUNDLE_TEMPFILE_PATH;
		}

		termtimeout = param.BUNDLE_DICTTERM_TIMEOUT;
	}

	private void MakeTerm(List<DictTermBean> list, int newtime) {
		HashMap<String, MakeWordTree> industryMap;
		Language lang;
		for (DictTermBean dictTermBean : list) {
			lang = _pairSourceLangMap.get(String.valueOf(dictTermBean.pair_id));

			if (!_map.containsKey(String.valueOf(dictTermBean.pair_id))) {
				_map.put(String.valueOf(dictTermBean.pair_id),
						new HashMap<String, MakeWordTree>());
			}

			industryMap = _map.get(String.valueOf(dictTermBean.pair_id));
			if (!industryMap.containsKey(String
					.valueOf(dictTermBean.industry_id))) {
				MakeWordTree treetemp = new MakeWordTree(lang);
				treetemp.last_update_time = Util.GetIntFromNow();
				industryMap.put(String.valueOf(dictTermBean.industry_id),
						treetemp);
			}

			MakeWordTree tree = industryMap.get(String
					.valueOf(dictTermBean.industry_id));
			tree.last_update_time = newtime;
			tree.Make(dictTermBean);
		}
	}

	private void MakeTerm(int pair_id, int term_group_id,
			List<TermCustoBean> list, int newtime) {
		Language lang;
		for (TermCustoBean termCustoBean : list) {
			lang = _pairSourceLangMap.get(pair_id);

			if (!_mapCusto.containsKey(String.valueOf(term_group_id))) {
				MakeWordTree treetemp = new MakeWordTree(lang);
				treetemp.last_update_time = Util.GetIntFromNow();
				_mapCusto.put(String.valueOf(term_group_id), treetemp);
			}

			MakeWordTree tree = _mapCusto.get(String.valueOf(term_group_id));
			tree.last_update_time = newtime;
			tree.MakeCusto(termCustoBean);
		}
	}

	private void RefreshCustoTree(int pair_id, int term_group_id,
			int last_update_time, int newTime) {
		TermGroupDAO termdao = null;

		try {
			termdao = new TermGroupDAO();
			termdao.Init(true);
			List<TermCustoBean> list = termdao.SelectForUpdateTime(
					term_group_id, last_update_time);
			termdao.UnInit();
			if (list != null && list.size() > 0) {
				MakeTerm(pair_id, term_group_id, list, newTime);
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (termdao != null) {
				termdao.UnInit();
			}
		}

	}

	private void RefreshPairIndustryTree(int pair_id, int industry_id,
			int last_update_time, int newTime) {
		DictTermDAO termdao = null;

		try {
			termdao = new DictTermDAO();
			termdao.Init(true);
			List<DictTermBean> list = termdao.SelectForUpdateTime(pair_id,
					industry_id, last_update_time);
			termdao.UnInit();
			if (list != null && list.size() > 0) {
				MakeTerm(list, newTime);
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (termdao != null) {
				termdao.UnInit();
			}
		}

	}

	private void AddTermDetails(List<DictTermBean> dicttermlist,
			HashMap<String, DictTermDetailsBean> dictTermDetailsMap, int newTime) {
		PairDictTermDetails pairDictTermDetails;
		List<DictTermDetailsBean> dictTermDetailslist;
		HashMap<String, List<DictTermDetailsBean>> map = new HashMap<String, List<DictTermDetailsBean>>();
		for (DictTermBean dictTermBean : dicttermlist) {
			if (!_mapDictTerm.containsKey(String.valueOf(dictTermBean.pair_id))) {
				pairDictTermDetails = new PairDictTermDetails();
				pairDictTermDetails.map = new HashMap<String, HashMap<String, List<DictTermDetailsBean>>>();
				pairDictTermDetails.pair_id = dictTermBean.pair_id;
				_mapDictTerm.put(String.valueOf(dictTermBean.pair_id),
						pairDictTermDetails);
			}

			pairDictTermDetails = _mapDictTerm.get(String
					.valueOf(dictTermBean.pair_id));
			if (!pairDictTermDetails.map.containsKey(String
					.valueOf(dictTermBean.industry_id))) {
				pairDictTermDetails.map.put(
						String.valueOf(dictTermBean.industry_id),
						new HashMap<String, List<DictTermDetailsBean>>());
			}

			HashMap<String, List<DictTermDetailsBean>> dictTermListMap = pairDictTermDetails.map
					.get(String.valueOf(dictTermBean.industry_id));

			if (!dictTermListMap.containsKey(dictTermBean.term.toLowerCase())) {
				dictTermListMap.put(dictTermBean.term.toLowerCase(),
						new ArrayList<DictTermDetailsBean>());
			}
			dictTermDetailslist = dictTermListMap.get(dictTermBean.term
					.toLowerCase());

			if (!map.containsKey(String.valueOf(dictTermBean.term_id))) {
				map.put(String.valueOf(dictTermBean.term_id),
						dictTermDetailslist);
			}
		}

		Set<String> termIDSet = dictTermDetailsMap.keySet();
		DictTermDetailsBean bean;
		for (String termID : termIDSet) {
			bean = dictTermDetailsMap.get(termID);
			if (bean != null) {
				if (map.containsKey(termID)) {
					dictTermDetailslist = map.get(termID);
					dictTermDetailslist.add(bean);
				}
			}
		}
	}

	private void AnalyseFileTerm(JSONObject obj) {
		ArrayList<OrderFileBean> list;
		FastDFS dfs = null;
		BiliFileNoTag bilifile = null;
		DictLangDAO langdao = null;
		JSONObject keywordJSON = new JSONObject();
		try {
			int langpair = Util.GetIntFromJSon("pair_id", obj);
			int industry_id = Util.GetIntFromJSon("industry_id", obj);
			int term_group_id = Util.GetIntFromJSon("term_group_id", obj);
			langdao = new DictLangDAO();
			langdao.Init(true);
			DictLangPairBean langpairbean = langdao.SelectPair(langpair);
			langdao.UnInit();
			if (langpairbean != null) {
				dfs = new FastDFS();
				dfs.Init();
				String biliFileName;
				// String preproceesFileName;
				// String preprocess_file_id;
				HashMap<String, Keyword> keywordmap = new HashMap<String, Keyword>();
				list = new ArrayList<OrderFileBean>();
				JSONObject files_json = Util.GetJSonFromJSon("files_json", obj);
				Set<String> fileset = files_json.keySet();
				for (String originalFileName : fileset) {
					OrderFileBean filebean = new OrderFileBean();
					filebean.originalFileName = originalFileName;
					filebean.b_file_id = Util.GetStringFromJSon(
							originalFileName, files_json);
					list.add(filebean);
				}
				for (OrderFileBean orderFileBean : list) {
					biliFileName = null;
					// 下载并解析双语文件
					biliFileName = _path + "keyword"
							+ orderFileBean.originalFileName + ".xml";
					// preproceesFileName = _path + orderFileBean.file_id
					// + "_preprocess.xml";
					dfs.Download(orderFileBean.b_file_id, biliFileName);
					File f = new File(biliFileName);
					if (!f.exists()) {

						Log4j.error("file(" + biliFileName + ") not exists. ");
						continue;
					}

					FileUtil fileutil = new FileUtil();
					String ext = fileutil
							.GetExtFromFileName(orderFileBean.originalFileName);
					bilifile = fileutil.GetBiliFileNoTagByExt(ext);
					// 得到语言对对应的原语言和目标语言信息
					DetectLanguage detectLanguage = new DetectLanguage();
					Language sourceLang = detectLanguage
							.Detect(langpairbean.source_class_name);
					Language targetLang = detectLanguage
							.Detect(langpairbean.target_class_name);

					// bilifile._tagId = "☂";
					bilifile.Init("", "", biliFileName,
							orderFileBean.originalFileName, sourceLang,
							targetLang);
					// TTX解析重写ParseBili，读取预翻译字段
					bilifile.ParseBili();
					// 预处理文件初始化
					PreprocessFile preprocessFile = new PreprocessFile(
							sourceLang, targetLang, bilifile,
							orderFileBean.file_id);
					ENTITY_FILE_TYPE fileType = bilifile._fileType;
					boolean tagFilter;
					if (fileType != ENTITY_FILE_TYPE.TTX
							&& fileType != ENTITY_FILE_TYPE.SDLXLIFF) {
						tagFilter = false;
					} else {
						tagFilter = true;
					}
					bilifile.UnInit();
					if (preprocessFile != null) {
						for (PreprocessSentence sentence : preprocessFile
								.GetSentecenList()) {
							// 句子分解优质术语
							sentence.termList = PreprocessTerm(
									sentence.sourceText,
									String.valueOf(langpair),
									String.valueOf(industry_id), term_group_id,
									tagFilter);
							if (sentence != null && sentence.termList != null
									&& sentence.termList.size() > 0) {
								for (PreprocessTerm preprocessTerm : sentence.termList) {
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
										if (keywordmap.containsKey(keyword)) {
											Keyword tmp = keywordmap
													.get(keyword);

											tmp.count += count;
										} else {
											Keyword tmp = new Keyword();
											tmp.keyword = keyword;
											tmp.count = count;
											keywordmap.put(keyword, tmp);
										}
									}
								}
							}
						}
					}
				}
				if (keywordmap != null) {

					Set<String> set = keywordmap.keySet();
					for (String keyword : set) {
						Keyword tmp = keywordmap.get(keyword);
						keywordJSON.put(tmp.keyword, String.valueOf(tmp.count));
					}
					Log4j.info("termbolt-orderrecom-term"
							+ keywordJSON.toString());
				}
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dfs != null) {
				dfs.UnInit();
			}
			if (bilifile != null) {
				bilifile.UnInit();
			}
			if (langdao != null) {
				langdao.UnInit();
			}
		}

		JSONObject resObj = new JSONObject();
		resObj.put("result", "OK");
		resObj.put("keyword", keywordJSON);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
		_reportor.Report(resObj);
	}

	public void Preprocess(JSONObject obj) {
		// Log4j.debug("-------------------------preprocess func start");
		JSONObject resObj = new JSONObject();
		resObj.put("result", "OK");
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
		_reportor.Report(resObj);

		try {
			// 缓冲3秒，让开order在recom中的注册时间
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Log4j.error(e);
		}
		// Log4j.debug("-------------------------preprocess func sleep3000 over");

		int order_id = Util.String2Int(Util.GetStringFromJSon("order_id", obj));
		if (order_id <= 0) {
			Log4j.error("[preprocess] order_id is null or order_id is not int can't preprocess");
			return;
		}

		String nid = Util.GetStringFromJSon("nid", obj);

		ProcPreprocessDAO procpreprocessdao = null;
		OrderDAO orderdao = null;
		OrderFileDAO orderfiledao = null;
		OrderBean orderBean = null;
		DictLangPairBean pairBean = null;
		Language sourceLang = null;
		Language targetLang = null;
		List<OrderFileBean> orderfilebeanList = null;

		List<OrderFileBean> orderfilebeanPreprocessList = null;
		BiliFileNoTag bilifile = null;
		FastDFS dfs = null;
		try {

			Map map = new HashMap<String, Object>();
			map.put("p_order_id", order_id);
			// 执行存储过程，查询订单信息，订单语言对信息，订单所属文件信息
			procpreprocessdao = new ProcPreprocessDAO();
			procpreprocessdao.Init(true);
			List<List<?>> list = procpreprocessdao.PreprocessSelect(map);
			procpreprocessdao.UnInit();
			// 校验查询结果集是否三个
			if (list != null && list.size() == 3) {
				orderBean = ((List<OrderBean>) list.get(0)).get(0);
				pairBean = ((List<DictLangPairBean>) list.get(1)).get(0);
				orderfilebeanList = (List<OrderFileBean>) list.get(2);
			} else {
				Log4j.error("[preprocess] procedure error");
				return;
			}

			int term_group_id = Util.GetIntFromJSon("tgid", obj);

			// 校验订单是否存在
			if (orderBean == null) {
				Log4j.error("[preprocess] order(" + order_id
						+ ") is not exists. ");
				return;
			} else if (orderBean.preprocess) {
				// 校验订单是否已经预处理
				Log4j.error("[preprocess] order(" + order_id
						+ ") is preprocessed.");
				return;
			} else if (pairBean == null) {
				// 校验订单是否有语言对信息
				Log4j.error("[preprocess] langpair(" + orderBean.pair_id
						+ ") is not exists.");
				return;
			}

			// 得到订单语言对对应的原语言和目标语言信息
			DetectLanguage detectLanguage = new DetectLanguage();
			sourceLang = detectLanguage.Detect(pairBean.source_class_name);
			targetLang = detectLanguage.Detect(pairBean.target_class_name);
			if (sourceLang == null || targetLang == null) {
				Log4j.error("[preprocess] source lang class object or target lang class object is not exists.");
				return;
			} else if (orderfilebeanList == null
					|| orderfilebeanList.size() == 0) {
				// 校验订单文件信息是否存在
				Log4j.error("[preprocess] order(" + order_id + ") hasn't files");
				return;
			}
			if (term_group_id <= 0) {
				// dictterm更新
				if (orderBean.pair_id > 0 && orderBean.industry_id > 0) {
					if (!_map.containsKey(String.valueOf(orderBean.pair_id))) {
						_map.put(String.valueOf(orderBean.pair_id),
								new HashMap<String, MakeWordTree>());
					}

					if (_map.containsKey(String.valueOf(orderBean.pair_id))) {
						HashMap<String, MakeWordTree> industryMap = _map
								.get(String.valueOf(orderBean.pair_id));
						if (!industryMap.containsKey(String
								.valueOf(orderBean.industry_id))) {
							Language lang = _pairSourceLangMap.get(String
									.valueOf(orderBean.pair_id));
							MakeWordTree treetemp = new MakeWordTree(lang);
							treetemp.last_update_time = 0;
							industryMap.put(
									String.valueOf(orderBean.industry_id),
									treetemp);
						}
						if (industryMap.containsKey(String
								.valueOf(orderBean.industry_id))) {
							MakeWordTree tree = industryMap.get(String
									.valueOf(orderBean.industry_id));
							int newTime = Util.GetIntFromNow();
							if (tree.last_update_time + termtimeout <= newTime) {
								RefreshPairIndustryTree(orderBean.pair_id,
										orderBean.industry_id,
										tree.last_update_time, newTime);
								if (tree.last_update_time < newTime) {
									tree.last_update_time = newTime;
								}
							}
						}
					}
				}
			} else {
				if (orderBean.pair_id > 0) {
					if (!_mapCusto.containsKey(String.valueOf(term_group_id))) {
						Language lang = _pairSourceLangMap.get(String
								.valueOf(orderBean.pair_id));
						MakeWordTree treetemp = new MakeWordTree(lang);
						treetemp.last_update_time = 0;
						_mapCusto.put(String.valueOf(term_group_id), treetemp);
					}
					if (_mapCusto.containsKey(String.valueOf(term_group_id))) {
						MakeWordTree tree = _mapCusto.get(String
								.valueOf(term_group_id));
						int newTime = Util.GetIntFromNow();
						if (tree.last_update_time + termtimeout <= newTime) {
							RefreshCustoTree(orderBean.pair_id, term_group_id,
									tree.last_update_time, newTime);
							if (tree.last_update_time < newTime) {
								tree.last_update_time = newTime;
							}
						}
					}
				}
			}

			dfs = new FastDFS();
			dfs.Init();
			String biliFileName;
			String preproceesFileName;
			String preprocess_file_id;

			// 存储所有需要修改的订单文件bean，在循环结束后一起更新数据库
			orderfilebeanPreprocessList = new ArrayList<OrderFileBean>();
			for (OrderFileBean orderFileBean : orderfilebeanList) {
				biliFileName = null;
				preproceesFileName = null;
				preprocess_file_id = null;
				if (!orderFileBean.analyse || orderFileBean.b_file_id == null
						|| orderFileBean.b_file_id.trim().length() == 0) {
					continue;
				}
				// 下载并解析双语文件
				biliFileName = _path + orderFileBean.file_id + ".xml";
				preproceesFileName = _path + orderFileBean.file_id
						+ "_preprocess.xml";
				dfs.Download(orderFileBean.b_file_id, biliFileName);
				File f = new File(biliFileName);
				if (!f.exists()) {

					Log4j.error("file(" + orderFileBean.file_id
							+ ") hasn't bilifile. ");
					continue;
				}

				FileUtil fileutil = new FileUtil();
				String ext = fileutil
						.GetExtFromFileName(orderFileBean.source_file_id);
				bilifile = fileutil.GetBiliFileNoTagByExt(ext);

				// bilifile._tagId = "☂";
				bilifile.Init("", "", biliFileName,
						orderFileBean.originalFileName, sourceLang, targetLang);
				// TTX解析重写ParseBili，读取预翻译字段
				bilifile.ParseBili();
				// 预处理文件初始化
				PreprocessFile preprocessFile = new PreprocessFile(sourceLang,
						targetLang, bilifile, orderFileBean.file_id);
				ENTITY_FILE_TYPE fileType = bilifile._fileType;
				bilifile.UnInit();
				if (preprocessFile != null) {
					// 修改预览
					if (preprocessFile.preview != null
							&& preprocessFile.preview.length() > 0) {
						// 更新缓存

						SetPreviewForCache(nid, orderBean, orderFileBean,
								preprocessFile.preview.toString());

						orderFileBean.preview = preprocessFile.preview
								.toString();
					}
					String targetText;// 推荐翻译字段
					if (fileType != ENTITY_FILE_TYPE.TTX
							&& fileType != ENTITY_FILE_TYPE.SDLXLIFF) {
						MachineTranslation machine = null;
						if (sourceLang != null && targetLang != null) {
							if (pairBean.source_ename
									.equalsIgnoreCase("simplified Chinese")
									&& pairBean.target_ename
											.equalsIgnoreCase("english")) {

								machine = new MachineTranslation(TRANS_TYPE.C2E);
							} else if (pairBean.source_ename
									.equalsIgnoreCase("english")
									&& pairBean.target_ename
											.equalsIgnoreCase("simplified Chinese")) {
								machine = new MachineTranslation(TRANS_TYPE.E2C);
							} else {
								Log4j.error("order is not e2c or c2e,hasn't machinetranslation. ");
							}
						}

						for (PreprocessSentence sentence : preprocessFile
								.GetSentecenList()) {
							// 句子分解优质术语
							sentence.termList = PreprocessTerm(
									sentence.sourceText,
									String.valueOf(orderBean.pair_id),
									String.valueOf(orderBean.industry_id),
									term_group_id, false);
							if (machine != null && machine._usable) {
								targetText = null;
								targetText = this.MachineTranslat(machine,
										sentence.sourceText);
								// \n
								if (targetText != null) {
									if (targetText.endsWith("\n")) {
										targetText = targetText.substring(0,
												targetText.length() - 2);
									}
								}

								sentence.targetText = targetText;
							}
						}
					} else {
						for (PreprocessSentence sentence : preprocessFile
								.GetSentecenList()) {
							// 句子分解优质术语
							sentence.termList = PreprocessTerm(
									sentence.sourceText,
									String.valueOf(orderBean.pair_id),
									String.valueOf(orderBean.industry_id),
									term_group_id, true);
						}
					}

					// if (machine != null) {
					// machine.ReleaseConnection();
					// }
					preprocessFile.Save(preproceesFileName);

					preprocess_file_id = dfs.Upload(preproceesFileName, "xml");

					orderFileBean.preprocess_file_id = preprocess_file_id;

					orderfilebeanPreprocessList.add(orderFileBean);
				}
			}
			dfs.UnInit();
			// 初始化DAO，执行所有更新数据库操作。使数据库链接尽可能集中到一个时间段，减少死锁机率
			orderfiledao = new OrderFileDAO();
			orderfiledao.Init(true);
			for (OrderFileBean orderFileBean : orderfilebeanPreprocessList) {
				orderfiledao.UpdatePreprocessFile(orderFileBean);
				if (orderFileBean.preview != null
						&& orderFileBean.preview.length() > 0) {
					orderfiledao.UpdatePreview(orderFileBean);
				}
			}
			orderfiledao.Commit();
			orderfiledao.UnInit();

			orderdao = new OrderDAO();
			orderdao.Init(true);
			orderdao.UpdatePreprocess(order_id);
			orderdao.Commit();
			orderdao.UnInit();

			JSONObject paramJson = new JSONObject();
			paramJson.put("className",
					"com.wiitrans.automation.logic.SyncDataLogicImpl");
			paramJson.put("order_id", String.valueOf(order_id));
			paramJson.put("nid", String.valueOf(nid));
			paramJson.put("uid", Util.GetStringFromJSon("uid", obj));
			paramJson.put("syncType", "send");
			paramJson.put("dataTemplate", "updatePreprocess");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sid", Util.GetStringFromJSon("sid", obj));
			jsonObject.put("uid", Util.GetStringFromJSon("uid", obj));
			jsonObject.put("param", paramJson);
			jsonObject.put("taskType", "1");
			jsonObject.put("corn", "");
			jsonObject.put("job_class",
					"com.wiitrans.automation.quartz.job.PushStromJob");
			ConfigNode node = BundleConf.BUNDLE_Node
					.get(BundleConf.DEFAULT_NID);
			if (node != null) {
				// Log4j.debug("-------------------------preprocess func http start");
				new HttpSimulator(node.api + "automation/newtask/")
						.executeMethodTimeOut(jsonObject.toString(),
								node.timeout);
				// Log4j.debug("-------------------------preprocess func http end");
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (procpreprocessdao != null) {
				procpreprocessdao.UnInit();
			}
			if (orderfiledao != null) {
				orderfiledao.UnInit();
			}
			if (orderdao != null) {
				orderdao.UnInit();
			}
			if (dfs != null) {
				dfs.UnInit();
			}
			if (bilifile != null) {
				bilifile.UnInit();
			}
		}
		// Log4j.debug("-------------------------preprocess func end");
	}

	private int SetPreviewForCache(String nid, OrderBean orderBean,
			OrderFileBean orderFileBean, String preview) {
		int ret = Const.FAIL;
		// Log4j.debug("-------------------------SetPreviewForCache func start");
		if (orderBean.code != null && orderBean.code.length() > 0) {
			String orderkey = "order_" + orderBean.code;
			String sOrder = _cache.GetString(Util.String2Int(nid), orderkey);
			JSONObject jsOrder, jsFiles, jsFile;
			if (sOrder != null && sOrder.trim().length() > 0) {
				jsOrder = new JSONObject(sOrder);
				jsFiles = Util.GetJSonFromJSon("file", jsOrder);
				if (jsFiles != null) {
					jsFile = Util.GetJSonFromJSon(
							String.valueOf(orderFileBean.file_id), jsFiles);

					if (jsFile != null) {
						String spreview = jsFile.get("preview").toString();
						if (spreview == null || spreview.length() == 0) {
							jsFile.put("preview", preview);
							_cache.SetString(Util.String2Int(nid), orderkey,
									jsOrder.toString());
						}
					}
				}
			}
		}
		// Log4j.debug("-------------------------SetPreviewForCache func end");
		ret = Const.SUCCESS;
		return ret;
	}

	private String MachineTranslat(MachineTranslation machine, String sourceText) {
		String result = "";
		try {
			result = machine.run(sourceText);
		} catch (Exception e) {
			Log4j.error(e);
		}

		return result;
	}

	private ArrayList<PreprocessTerm> PreprocessTerm(String sentence,
			String pair_id, String industry_id, int term_group_id,
			boolean tagFilter) {
		// Log4j.debug("-------------------------PreprocessTerm func start");
		ArrayList<PreprocessTerm> termList = null;

		HashMap<String, MakeWordTree> industryMap;
		DictTermDAO dicttermdao = null;
		List<DictTermDetailsBean> termDetailsList;
		if (pair_id != null && industry_id != null) {
			try {
				MakeWordTree tree = null;
				if (term_group_id <= 0) {
					if (_map.containsKey(pair_id)) {
						industryMap = _map.get(pair_id);
					} else {
						Log4j.error("语言对(" + pair_id + ")没有对应优质术语树");
						return termList;
					}

					if (industryMap.containsKey(industry_id)) {
						tree = industryMap.get(industry_id);
					} else {
						Log4j.error("语言对(" + pair_id + ")下industry("
								+ industry_id + ")没有对应优质术语树");
						return termList;
					}
				} else {
					if (_mapCusto.containsKey(String.valueOf(term_group_id))) {
						tree = _mapCusto.get(String.valueOf(term_group_id));
					} else {
						Log4j.error("客户术语组(" + term_group_id + ")不存在");
						return termList;
					}
				}

				if (tree == null) {
					return termList;
				}

				ArrayList<TextTerm> termInText = tree.AnalyseGreedyText(
						sentence, tagFilter);

				if (termInText != null && termInText.size() > 0) {
					termList = new ArrayList<PreprocessTerm>();
					PreprocessTerm term;
					dicttermdao = new DictTermDAO();
					dicttermdao.Init(true);
					for (int i = 0; i < termInText.size(); i++) {
						term = new PreprocessTerm();
						term.term = termInText.get(i).term;
						term.term_id = termInText.get(i).term_id;
						term.begin = termInText.get(i).index;
						term.end = (termInText.get(i).index + termInText.get(i).term
								.length());
						term.count = termInText.get(i).count;
						if (term_group_id <= 0) {
							termDetailsList = dicttermdao
									.SelectTermDetailsByTermID(term.term_id);

							if (termDetailsList != null) {
								PreprocessTermDetail preprocesstermdetail;
								term.termDetailsList = new ArrayList<PreprocessTermDetail>();
								for (DictTermDetailsBean termdetails : termDetailsList) {
									preprocesstermdetail = new PreprocessTermDetail();
									preprocesstermdetail.term_id = termdetails.term_id;
									preprocesstermdetail.translator_id = termdetails.translator_id;
									preprocesstermdetail.meaning = termdetails.meaning;
									preprocesstermdetail.usage = termdetails.usage;
									preprocesstermdetail.remark = termdetails.remark;
									term.termDetailsList
											.add(preprocesstermdetail);
								}
							}
						} else {
							term.termDetailsList = new ArrayList<PreprocessTermDetail>();
							PreprocessTermDetail preprocesstermdetail = new PreprocessTermDetail();
							preprocesstermdetail.term_id = term.term_id;
							preprocesstermdetail.translator_id = 0;
							preprocesstermdetail.meaning = termInText.get(i).meaning;
							preprocesstermdetail.usage = "";
							preprocesstermdetail.remark = "";
							term.termDetailsList.add(preprocesstermdetail);
						}
						termList.add(term);
					}
				} else {
					Log4j.error("分解术语结果为空");
				}

			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dicttermdao != null) {
					dicttermdao.UnInit();
				}
			}

		}
		// Log4j.debug("-------------------------PreprocessTerm func end");
		return termList;
	}

	public int SearchTerm(JSONObject obj) {
		// Log4j.debug("-------------------------searchTerm func start");
		int result = Const.FAIL;
		String uid = Util.GetStringFromJSon("uid", obj);
		int nid = Util.GetIntFromJSon("nid", obj);
		String pair_id = Util.GetStringFromJSon("pair_id", obj);
		String term = Util.GetStringFromJSon("term", obj);
		term = term.toLowerCase();
		JSONObject termsJSON = null;

		String term_cache_key = this
				.GetCacheKey(Util.String2Int(pair_id), term);

		String sterms = _cache.GetString(nid, term_cache_key);
		if (sterms != null) {
			termsJSON = new JSONObject(sterms);
			if (termsJSON != null) {
				try {
					String industry_name;
					JSONObject industryJSON;

					String term_user_id;
					JSONObject termJSON;
					String term_agree_id;
					Iterator<String> iter = termsJSON.keys();
					while (iter.hasNext()) {
						industry_name = iter.next();
						industryJSON = Util.GetJSonFromJSon(industry_name,
								termsJSON);
						Iterator<String> iterTranslator = industryJSON.keys();

						while (iterTranslator.hasNext()) {
							String myagree = "0";
							term_user_id = iterTranslator.next();
							if (!term_user_id.equals("industry_id")) {
								termJSON = Util.GetJSonFromJSon(term_user_id,
										industryJSON);
								if (termJSON.has("agree")) {
									JSONObject agreeJsonObject = Util
											.GetJSonFromJSon("agree", termJSON);
									Iterator<String> iteragree = agreeJsonObject
											.keys();
									while (iteragree.hasNext()) {
										term_agree_id = iteragree.next();
										if (term_agree_id.equals(uid)) {
											myagree = Util.GetStringFromJSon(
													term_agree_id,
													agreeJsonObject);
										}
									}
								}

								termJSON.put("myagree", myagree);
								industryJSON.put(term_user_id, termJSON);
							}
						}
					}

					result = Const.SUCCESS;
				} catch (Exception e) {
					Log4j.error(e);
				}

			}
		} else {
			Log4j.log("在缓存中key为" + term_cache_key + "不存在");
		}

		JSONObject resObj = new JSONObject();
		if (termsJSON != null) {
			resObj.put("terms", termsJSON);
		}

		// int last_update_time = 0;
		JSONObject dicttermsJSON = new JSONObject();
		boolean dictflag = false;
		try {
			if (!_mapDictTerm.containsKey(pair_id)) {
				PairDictTermDetails pairDictTermDetails = new PairDictTermDetails();
				pairDictTermDetails.map = new HashMap<String, HashMap<String, List<DictTermDetailsBean>>>();
				pairDictTermDetails.pair_id = Util.String2Int(pair_id);
				_mapDictTerm.put(pair_id, pairDictTermDetails);
			}

			if (_mapDictTerm.containsKey(pair_id)) {
				String industry_name;
				PairDictTermDetails pairDictTermDetails = _mapDictTerm
						.get(pair_id);
				Set<String> industryset = pairDictTermDetails.map.keySet();
				for (String industryID : industryset) {
					HashMap<String, List<DictTermDetailsBean>> termHashMap = pairDictTermDetails.map
							.get(industryID);
					if (_industryMap.containsKey(industryID)) {
						industry_name = _industryMap.get(industryID);

						JSONObject industryDictTerms = new JSONObject();
						industryDictTerms.put("industry_id", industryID);
						dicttermsJSON.put(industry_name, industryDictTerms);

						if (termHashMap.containsKey(term)) {
							List<DictTermDetailsBean> dictTermDetailslist = termHashMap
									.get(term);
							JSONObject dicttermJSON;

							for (DictTermDetailsBean dictTermDetailsBean : dictTermDetailslist) {
								dicttermJSON = new JSONObject();
								dicttermJSON.put("meaning",
										dictTermDetailsBean.meaning);

								dicttermJSON.put("usage",
										dictTermDetailsBean.usage);

								dicttermJSON.put("remark",
										dictTermDetailsBean.remark);
								industryDictTerms
										.put(String
												.valueOf(dictTermDetailsBean.translator_id),
												dicttermJSON);
								dictflag = true;
							}
						}
					} else {
						Log4j.error("语言对(" + pair_id + ")下的industry("
								+ industryID + ")的优质术语为空");
					}
				}
			} else {
				Log4j.error("语言对(" + pair_id + ")下优质术语为空");
			}
			result = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		}

		if (dicttermsJSON != null && dictflag) {
			resObj.put("dictterms", dicttermsJSON);
		}

		if (Const.SUCCESS == result) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
		_reportor.Report(resObj);

		// dictterm更新
		if (pair_id != null) {
			// Log4j.debug("-------------------------searchTerm dictterm update");
			if (_mapDictTerm.containsKey(pair_id)) {
				PairDictTermDetails pairDictTermDetails = _mapDictTerm
						.get(pair_id);

				int newTime = Util.GetIntFromNow();
				if (pairDictTermDetails.last_update_time + termtimeout <= newTime) {

					DictTermDAO termdao = null;

					try {
						termdao = new DictTermDAO();
						termdao.Init(true);
						List<DictTermBean> dicttermlist = termdao
								.SelectForUpdateTimeOnPair(
										Util.String2Int(pair_id),
										pairDictTermDetails.last_update_time);
						List<DictTermDetailsBean> detailslist = termdao
								.SelectTermDetailsForUpdateTimeOnPair(
										Util.String2Int(pair_id),
										pairDictTermDetails.last_update_time);
						termdao.UnInit();

						HashMap<String, DictTermDetailsBean> dictTermDetailsSet = new HashMap<String, DictTermDetailsBean>();
						for (DictTermDetailsBean dictTermDetailsBean : detailslist) {
							dictTermDetailsSet
									.put(String
											.valueOf(dictTermDetailsBean.term_id),
											dictTermDetailsBean);
						}
						this.AddTermDetails(dicttermlist, dictTermDetailsSet,
								newTime);
						pairDictTermDetails.last_update_time = newTime;

					} catch (Exception e) {
						Log4j.error(e);
					} finally {
						if (termdao != null) {
							termdao.UnInit();
						}
					}

				}
			}
		}

		// Log4j.debug("-------------------------searchTerm func end");
		return result;
	}

	public int SetTerm(BasicOutputCollector collector, JSONObject obj) {
		// Log4j.debug("-------------------------setTerm func start");
		int result = Const.FAIL;

		String pair_id = Util.GetStringFromJSon("pair_id", obj);
		String term = Util.GetStringFromJSon("term", obj);

		String meaning = Util.GetStringFromJSon("meaning", obj);

		// String usage = Util.GetStringFromJSon("usage", obj);
		//
		// String remark = Util.GetStringFromJSon("remark", obj);

		if (pair_id != null && term != null && meaning != null
				&& meaning.length() < 200) {
			collector.emit(new Values(obj.toString()));
		} else {

			JSONObject resObj = new JSONObject();
			resObj.put("result", "FAILED");

			resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			resObj.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			resObj.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
			resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
			_reportor.Report(resObj);
		}
		// Log4j.debug("-------------------------setTerm func end");
		return result;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String task = tuple.getStringByField("task");
		JSONObject obj = new JSONObject(task);
		String aid = Util.GetStringFromJSon("aid", obj);

		Log4j.log("termbolt " + obj.toString());

		switch (aid) {
		case "preprocess": {
			// 预处理[文件预览、句子的术语【根据term_group_id区分优质、custo】、句子的推荐译文、句子的机器翻译（已经放弃）]
			Preprocess(obj);
			break;
		}
		case "getterm": {
			// 分析术语
			// GetTerm(obj);
			break;
		}
		case "search": {
			// 查询术语、忽略大小写
			SearchTerm(obj);
			break;
		}
		case "setterm": {
			// 创建贡献术语
			SetTerm(collector, obj);
			break;
		}
		case "setagree": {
			collector.emit(new Values(obj.toString()));
			break;
		}
		case "AnalyseFileTerm": {
			// 文件term处理，提供给match服务使用，根据pair_id和industry_id分解出所有文件的术语统计
			AnalyseFileTerm(obj);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("task"));
	}

	private String GetCacheKey(int pair_id, String term) {
		return String.format("cterm_%d_%s", pair_id, term);
	}
}
