package com.wiitrans.frag.bolt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

//import com.sun.xml.internal.bind.v2.runtime.output.Encoded;
import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.bundle.BundleParam;
import com.wiitrans.base.bundle.ConfigNode;
import com.wiitrans.base.db.OrderDAO;
import com.wiitrans.base.db.OrderFileDAO;
import com.wiitrans.base.db.ProcLoadFileDAO;
import com.wiitrans.base.db.model.DictLangPairBean;
import com.wiitrans.base.db.model.OrderBean;
import com.wiitrans.base.db.model.OrderFileBean;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.PreprocessFile;
import com.wiitrans.base.file.PreprocessSentence;
import com.wiitrans.base.file.PreprocessTerm;
import com.wiitrans.base.file.PreprocessTermDetail;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.VirtualFragmentation;
import com.wiitrans.base.file.lang.CheckLanguage;
import com.wiitrans.base.file.lang.DetectCheckLanguage;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.notag.BiliFileDetails;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sentence.SDLXliffSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.TTXSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.filesystem.FastDFS;
import com.wiitrans.base.http.HttpSimulator;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.CheckDigit;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.task.TaskReportor;
import com.wiitrans.base.term.Term;
//import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

// 推荐服务将译员抢到的订单双语文件发布到Redis。
// Frag服务只针对Redis进行获取双语文件，编辑双语文件，保存双语文件，保存目标文件操作。
// Frag服务拿到的文件编号fid是PHP界面可以查看到的8位数字+字母组合。
// 在线编辑器“全文下载”PHP直接通过fid到Redis服务器获取目标文件提供下载。

// ------ PHP请求定义 ------
// JSONObject :
// { 文章编号， 虚拟段落编号， 句子编号，译文，校对，命令 }

// ------ Bundle缓存定义 ------
// String : UserId
// JSONObject :
// { 文章编号，文章总段落数，文章总句子数，虚拟段落索引，缓存总句子数，缓存句子索引，类型T/E
// 文本内容 { 第一句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，
// 第二句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}，...
// 第N句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，} } }

// 处理PHP请求，回复以JSon对象形式返回。
public class FragHandlerBolt extends BaseBasicBolt {

	private TaskReportor _reportor = null;
	// private ICache _cache = null;
	// Key is fileId.
	private HashMap<String, BiliFileNoTag> _files = null;
	private HashMap<String, PreprocessFile> _preprocessfiles = null;
	private HashMap<String, HashMap<Long, ArrayList<int[]>>> _hashcodeSentences = null;
	private SentCheckWord sentcheckword = null;

	private String _path = null;

	@Override
	public void prepare(Map conf, TopologyContext context) {

		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		_reportor = new TaskReportor(BundleConf.BUNDLE_REPORT_IP,
				BundleConf.FRAGMENTATION_BUNDLE_PORT);

		_reportor.Start();

		// if (_cache == null) {
		// _cache = new RedisCache();
		// _cache.Init(BundleConf.BUNDLE_REDIS_IP);
		// }

		if (_files == null) {
			_files = new HashMap<String, BiliFileNoTag>();
		}

		if (_preprocessfiles == null) {
			_preprocessfiles = new HashMap<String, PreprocessFile>();
		}

		if (_hashcodeSentences == null) {
			_hashcodeSentences = new HashMap<String, HashMap<Long, ArrayList<int[]>>>();
		}
		sentcheckword = new SentCheckWord();
		if (_path == null) {
			// BundleParam param = app._bundles.get("fragTopo");
			BundleParam param = WiitransConfig.getInstance(0).FRAG;
			_path = param.BUNDLE_TEMPFILE_PATH;
		}
	}

	private void CheckDigit(Sentence sentence, String target) {
		if (sentence == null) {
			return;
		}
		String source = sentence._source;
		// #3523
		if (sentence instanceof SDLXliffSentence
				|| sentence instanceof TTXSentence) {
			source = Util.clearTag(source);
			target = Util.clearTag(target);
		}
		Map<String, List<String>> result = CheckDigit.matchDiff(source, target);

		List<String> slist = result.get("source");
		if (slist != null && slist.size() > 0) {
			StringBuffer sourceDigit = new StringBuffer();
			sourceDigit.append(" ... ");
			for (String string : slist) {
				sourceDigit.append(string).append(" ... ");
			}
			sentence._sourceDigit = sourceDigit.toString().trim();
		} else {
			sentence._sourceDigit = "";
		}
		List<String> tlist = result.get("target");
		if (tlist != null && tlist.size() > 0) {
			StringBuffer targetDigit = new StringBuffer();
			targetDigit.append(" ... ");
			for (String string : tlist) {
				targetDigit.append(string).append(" ... ");
			}
			sentence._targetDigit = targetDigit.toString().trim();
		} else {
			sentence._targetDigit = "";
		}
	}

	public String Encode(String text) {
		if (text == null) {
			return null;
		}
		return text.replace("#&lt", "#&!)").replace("#&gt", "#&%)")
				.replace("#&amp", "#&!%)");

	}

	public String Decode(String text) {
		if (text == null) {
			return null;
		}
		return text.replace("#&!%)", "#&amp").replace("#&%)", "#&gt")
				.replace("#&!)", "#&lt");

	}

	// 同步方法中，句子同步处理
	private void SynSentence(int fid, Sentence sentence, String type,
			JSONObject obj, BasicOutputCollector collector, BiliFileNoTag file,
			HashMap<Long, ArrayList<int[]>> hashcodeMap) {

		CheckLanguage targetCheckLang = DetectCheckLanguage
				.Detect(file._targetLang);

		switch (type.toUpperCase()) {
		case "T": {
			String text = Util.GetStringFromJSon("text", obj);

			// 覆盖该句子的翻译信息
			// #3306 3307 如果不是ttx与xliff文件才解码
			if (sentence instanceof SDLXliffSentence) {
				if (((SDLXliffSentence) sentence)._lock) {
					return;
				}
				sentence._translate = text;
				sentence._translate_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex, Encode(text),
						targetCheckLang, obj, collector));
			} else if (sentence instanceof TTXSentence) {
				sentence._translate = text;
				sentence._translate_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex, Encode(text),
						targetCheckLang, obj, collector));
			} else {
				sentence._translate = Util.deCodeSpecialChart(text);
				sentence._translate_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex,
						Encode(Util.deCodeSpecialChart(text)), targetCheckLang,
						obj, collector));
			}

			BiliFileDetails details = file.GetBiliFileDetails();

			if (details != null) {
				// 翻译内容有效则立刻更新未翻译列表和个数统计
				if (sentence._translate == null
						|| sentence._translate.trim().length() == 0) {
					if (sentence._translatestatus) {
						details.tranlateWordCount -= sentence._sourceWordCount;
						details.tranlateSentenceCount -= 1;
						sentence._translatestatus = false;
					}
					if (sentence._termList != null) {
						// 增加CheckTerm
						for (Term term : sentence._termList) {
							if (term != null) {
								term.check_number = 0;
							}
						}
					}
					CheckDigit(sentence, "");
				} else {
					if (!sentence._translatestatus) {
						details.tranlateWordCount += sentence._sourceWordCount;
						details.tranlateSentenceCount += 1;
						sentence._translatestatus = true;
					}
					if (sentence._termList != null) {
						// 增加CheckTerm
						for (Term term : sentence._termList) {
							if (term != null) {
								term.check_number = Util.CheckTermCount(
										sentence._translate,
										term.check_meaning, term.check_count);
							}
						}
					}
					CheckDigit(sentence, sentence._translate);
				}
			}

			break;
		}
		case "E": {
			String text = Util.GetStringFromJSon("text", obj);
			String sscore = Util.GetStringFromJSon("score", obj);
			// 覆盖该句子的校对信息
			// #3306 3307 如果不是ttx与xliff文件才解码
			if (sentence instanceof SDLXliffSentence) {
				if (((SDLXliffSentence) sentence)._lock) {
					return;
				}
				sentence._edit = text;
				sentence._edit_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex, Encode(text),
						targetCheckLang, obj, collector));
			} else if (sentence instanceof TTXSentence) {
				sentence._edit = text;
				sentence._edit_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex, Encode(text),
						targetCheckLang, obj, collector));
			} else {
				sentence._edit = Util.deCodeSpecialChart(text);
				sentence._edit_r = Decode(sentcheckword.ChechWord(fid,
						sentence._virtualFragIndex * Const.FRAG_SIZE
								+ sentence._virtualSentenceIndex,
						Encode(Util.deCodeSpecialChart(text)), targetCheckLang,
						obj, collector));
			}
			int score = Util.String2Int(sscore);
			if (score >= 0) {
				sentence._eScore = score;
			}

			BiliFileDetails details = file.GetBiliFileDetails();
			if (details != null) {
				// 翻译内容有效则立刻更新未翻译列表和个数统计

				if (sentence._eScore < 0) {
					if (sentence._editstatus) {
						details.editWordCount -= sentence._sourceWordCount;
						details.editSentenceCount -= 1;
						sentence._editstatus = false;
					}
					if (sentence._termList != null) {
						// 增加CheckTerm
						for (Term term : sentence._termList) {
							if (term != null) {
								term.check_number = Util.CheckTermCount(
										sentence._translate,
										term.check_meaning, term.check_count);
							}
						}
					}
					CheckDigit(sentence, sentence._translate);
				} else {
					if (!sentence._editstatus) {
						details.editWordCount += sentence._sourceWordCount;
						details.editSentenceCount += 1;
						sentence._editstatus = true;
					}
					String content;
					if (sentence._edit == null
							|| sentence._edit.trim().length() == 0) {
						content = sentence._translate;
					} else {
						content = sentence._edit;
					}

					if (sentence._termList != null) {
						// 增加CheckTerm
						for (Term term : sentence._termList) {
							if (term != null) {
								term.check_number = Util.CheckTermCount(
										content, term.check_meaning,
										term.check_count);
							}
						}
					}
					CheckDigit(sentence, content);
				}
			}

			break;
		}
		default:
			break;
		}
	}

	// 更新某文件某段落某个句子的翻译或者校对信息
	private int SynHandler(JSONObject obj, BasicOutputCollector collector) {
		int ret = Const.FAIL;
		JSONObject resObj = new JSONObject();
		try {
			String fid = Util.GetStringFromJSon("fid", obj);
			resObj.put("fid", fid);

			int vPageNo = Util.GetIntFromJSon("pageno", obj); // 获取当前虚拟段落索引
			String sentNo = Util.GetStringFromJSon("sentno", obj); // 获取当前句子编号

			// 返回值中添加
			resObj.put("pageno", String.valueOf(vPageNo));
			resObj.put("sentno", sentNo);
			resObj.put("type", Util.GetStringFromJSon("type", obj));
			resObj.put("text", Util.GetStringFromJSon("text", obj));

			String type = Util.GetStringFromJSon("type", obj);// T或E
			if (fid != null && fid.trim().length() > 0) {
				if (_files.containsKey(fid)) {// 校验文件是否已经打开
					// 得到文件
					BiliFileNoTag file = _files.get(fid);
					HashMap<Long, ArrayList<int[]>> hashcodeMap = null;
					if (_hashcodeSentences.containsKey(fid)) {
						// 获取相同hashcode的map，hashcode在文件生成时已经计算
						hashcodeMap = _hashcodeSentences.get(fid);
					}
					// 得到段落
					if (vPageNo < file._virtualFragCount) {
						VirtualFragmentation vFrag = file._virtualFrags
								.get(vPageNo);
						// 含有这个句子
						if (vFrag != null && vFrag._sentencesMap != null) {
							if (vFrag._sentencesMap.containsKey(sentNo)
									&& type != null) {
								Sentence sentence = vFrag._sentencesMap
										.get(sentNo);
								// 句子同步
								SynSentence(Util.String2Int(fid), sentence,
										type, obj, collector, file, hashcodeMap);
								if (sentence instanceof SDLXliffSentence
										|| sentence instanceof TTXSentence) {
									if (type.equalsIgnoreCase("T")) {
										resObj.put("content_r",
												sentence._translate_r);
									} else {
										resObj.put("content_r",
												sentence._edit_r);
									}
								} else {
									if (type.equalsIgnoreCase("T")) {
										resObj.put(
												"content_r",
												Util.replaceCodeSpecialChat(sentence._translate_r));
									} else {
										resObj.put(
												"content_r",
												Util.replaceCodeSpecialChat(sentence._edit_r));
									}
								}

								sentence._hashstatus = true;
								if (type.equalsIgnoreCase("T")
										&& sentence._translate != null
										&& sentence._translate.trim().length() > 0) {
									String repetition = Util.GetStringFromJSon(
											"repetition", obj);
									if (repetition != null
											&& repetition.equals("true")) {
										// 100%重复数据，执行一次repetition后立刻return，防止执行结尾的report
										return Const.SUCCESS;
									} else if (hashcodeMap != null
											&& hashcodeMap
													.containsKey(sentence._hashcode)) {
										// 获取repetition列表，即hashcode的map中数据
										ArrayList<int[]> sentenceIndexList = hashcodeMap
												.get(sentence._hashcode);
										if (sentenceIndexList != null
												&& sentenceIndexList.size() > 1) {
											JSONObject sens = new JSONObject();
											JSONObject sen;
											JSONObject repetitionObj;
											int[] senindex;
											for (int i = 0; i < sentenceIndexList
													.size(); ++i) {
												// 模拟该方法的请求，递归所有repetition
												senindex = sentenceIndexList
														.get(i);

												if (senindex.length != 2) {
													// 编号错误，本次循环无效
													continue;
												}
												if (sentence._virtualFragIndex == senindex[0]
														&& sentence._virtualSentenceIndex == senindex[1]) {
													// 编号错误，本次循环无效
													continue;
												}

												// repetition的句子
												Sentence sentencetmp = this
														.GetSentence(file,
																senindex);

												if (sentencetmp == null
														|| sentencetmp._hashstatus) {
													// 句子错误，本次循环无效
													continue;
												}
												if (senindex[0] < file._virtualFragCount) {
													// 校验这次循环的记录是否无效
													VirtualFragmentation repetitionFrag = file._virtualFrags
															.get(senindex[0]);
													if (repetitionFrag != null
															&& repetitionFrag._sentencesMap != null) {
														if (repetitionFrag._sentencesMap
																.containsKey(String
																		.valueOf(senindex[1]))
																&& type != null) {
															Sentence repetitionSent = repetitionFrag._sentencesMap
																	.get(String
																			.valueOf(senindex[1]));
															if (repetitionSent instanceof SDLXliffSentence) {
																if (((SDLXliffSentence) repetitionSent)._lock) {
																	continue;
																}
															}
														}
													}
												}
												sen = new JSONObject();
												sen.put("pageno", String
														.valueOf(senindex[0]));
												sen.put("sentno", String
														.valueOf(senindex[1]));
												sens.put(String.valueOf(i), sen);
												repetitionObj = new JSONObject(
														obj.toString());
												repetitionObj
														.put("pageno",
																String.valueOf(senindex[0]));
												repetitionObj
														.put("sentno",
																String.valueOf(senindex[1]));
												repetitionObj.put("repetition",
														"true");
												// 模拟该方法的请求，递归所有repetition
												SynHandler(repetitionObj,
														collector);

											}
											resObj.put("same", sens);
										}
									}
								}
								if (hashcodeMap != null
										&& hashcodeMap
												.containsKey(sentence._hashcode)) {

									ArrayList<int[]> list = hashcodeMap
											.get(sentence._hashcode);
									// 校验一致性，修改第一句，重新验证，修改其他句，只校验该句
									CheckRepetition(file, list, sentence);
								}
							}
							ret = Const.SUCCESS;
						} else {
							Log4j.error("sentNo [" + sentNo
									+ "] is not contain in vPageNo " + vPageNo);
						}
					} else {
						Log4j.error("vPageNo [" + vPageNo
								+ "] is larger than actural.");
					}
					BiliFileDetails details = file.GetBiliFileDetails();
					resObj.put("sentcount",
							String.valueOf(details.sentenceCount));
					resObj.put("tsentcount",
							String.valueOf(details.tranlateSentenceCount));
					resObj.put("esentcount",
							String.valueOf(details.editSentenceCount));
					resObj.put("wordcount", String.valueOf(details.wordCount));
					resObj.put("twordcount",
							String.valueOf(details.tranlateWordCount));
					resObj.put("ewordcount",
							String.valueOf(details.editWordCount));

				} else {
					Log4j.error("文件ID(" + fid + ")未加载[SynHandler]");
				}
			} else {
				Log4j.error("文件ID无效");
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		if (Const.SUCCESS == ret) {
			// 记录当前文件页数，用于PHP分页显示
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

		return ret;
	}

	private int LoadFile(int fnid, String fid) {
		int ret = Const.FAIL;
		ProcLoadFileDAO procloadfiledao = null;
		OrderBean orderBean = null;
		DictLangPairBean pairBean = null;
		Language sourceLang = null;
		Language targetLang = null;
		OrderFileBean orderfilebean = null;
		FastDFS dfs = null;
		try {

			Map map = new HashMap<String, Object>();
			map.put("p_node_id", fnid);
			map.put("p_file_id", Util.String2Int(fid));
			// 执行存储过程，查询订单信息，订单语言对信息，订单所属文件信息
			procloadfiledao = new ProcLoadFileDAO();
			procloadfiledao.Init(true);
			List<List<?>> list = procloadfiledao.LoadFileSelect(map);
			procloadfiledao.UnInit();

			// 校验查询结果集是否三个
			if (list != null && list.size() == 3) {
				orderBean = ((List<OrderBean>) list.get(0)).get(0);
				pairBean = ((List<DictLangPairBean>) list.get(1)).get(0);
				orderfilebean = ((List<OrderFileBean>) list.get(2)).get(0);
			} else {
				Log4j.error("[loadfile] procedure error");
				return Const.FAIL;
			}

			// 校验文件信息是否存在
			if (orderfilebean == null) {
				Log4j.error("[loadfile] file(" + fid + ") is not exists. ");
				return Const.FAIL;
			} else if (orderBean == null) {
				// 校验订单是否存在
				Log4j.error("[loadfile] order is not exists. ");
				return Const.FAIL;
			} else if (pairBean == null) {
				// 校验订单是否有语言对信息
				Log4j.error("[loadfile] langpair(" + orderBean.pair_id
						+ ") is not exists.");
				return Const.FAIL;
			}

			// 得到订单语言对对应的原语言和目标语言信息
			DetectLanguage detectLanguage = new DetectLanguage();
			sourceLang = detectLanguage.Detect(pairBean.source_class_name);
			targetLang = detectLanguage.Detect(pairBean.target_class_name);
			if (sourceLang == null) {
				Log4j.error("[loadfile] source lang class object is not exists.");
				return Const.FAIL;
			}

			if (orderfilebean.b_file_id == null
					|| orderfilebean.b_file_id.trim().length() == 0) {
				Log4j.error("[loadfile] the filename of file(" + fid
						+ ")'s bilifile is wrong. ");
				return Const.FAIL;
			}

			dfs = new FastDFS();
			BiliFileNoTag file = null;

			String biliFileName = _path + fid + ".xml";
			File f = new File(biliFileName);

			// 下载并初始化双语文件
			dfs.Init();
			dfs.Download(orderfilebean.b_file_id, biliFileName);
			if (f.exists()) {
				FileUtil fileutil = new FileUtil();
				String ext = fileutil
						.GetExtFromFileName(orderfilebean.source_file_id);
				file = fileutil.GetBiliFileNoTagByExt(ext);

				// file._tagId = "☂";
				file.Init("", "", biliFileName, orderfilebean.originalFileName,
						sourceLang, targetLang);
				file.ParseBili();

				String sourceFileName = _path + fid + "." + ext;
				String targetFileName = _path + fid + "_cleanup." + ext;

				file.Init(sourceFileName, targetFileName, biliFileName,
						orderfilebean.originalFileName, sourceLang, targetLang);
				file._node_id = fnid;
				dfs.Download(orderfilebean.source_file_id, sourceFileName);
				f = new File(sourceFileName);
				if (f.exists()) {
					_files.put(fid, file);
					ret = Const.SUCCESS;
				} else {
					Log4j.error("[loadfile] the source file of file(" + fid
							+ ") download error. ");
				}
			} else {
				Log4j.error("[loadfile] the bilifile of file(" + fid
						+ ") download error. ");
			}

			if (ret == Const.SUCCESS) {
				String preprocess_file_id;
				preprocess_file_id = orderfilebean.preprocess_file_id;

				if (preprocess_file_id != null
						&& preprocess_file_id.trim().length() > 0) {
					try {
						String preprocessFileName = _path + fid
								+ "_preprocess.xml";

						dfs.Download(preprocess_file_id, preprocessFileName);
						File pfile = new File(preprocessFileName);
						if (pfile.exists()) {
							PreprocessFile preprocessFile = new PreprocessFile(
									preprocessFileName);

							_preprocessfiles.put(fid, preprocessFile);
							if (file != null && preprocessFile != null) {
								ArrayList<PreprocessSentence> presentencelist = preprocessFile
										.GetSentecenList();
								for (PreprocessSentence sent : presentencelist) {
									if (sent != null && sent.termList != null
											&& sent.termList.size() > 0) {
										Fragmentation frag = file._entityFrags
												.get(sent.fragIndex);
										Sentence sentence = frag._sentences
												.get(sent.sentenceIndex);
										if (frag != null
												&& sentence != null
												&& (sentence._termList == null || sentence._termList
														.size() == 0)) {
											if (sentence._termList == null) {
												sentence._termList = new ArrayList<Term>();
											}
											for (PreprocessTerm preprocessTerm : sent.termList) {
												if (preprocessTerm != null) {
													Term term = new Term();
													term.term_id = preprocessTerm.term_id;
													term._term = preprocessTerm.term;
													term.check_begin = preprocessTerm.begin;
													term.check_end = preprocessTerm.end;
													term.check_count = preprocessTerm.count;
													term.check_number = 0;
													if (preprocessTerm.termDetailsList != null
															&& preprocessTerm.termDetailsList
																	.size() > 0) {
														PreprocessTermDetail details = preprocessTerm.termDetailsList
																.get(0);
														term.check_meaning = details.meaning;
														term.check_remark = details.remark;
														term.check_usage = details.usage;
													}

													sentence._termList
															.add(term);
												}
											}
										}
									}
									// System.out.println();
								}
							}
						} else {
							Log4j.log("the preprocess file of file(" + fid
									+ ") download error. ");
						}

					} catch (Exception ex) {
						Log4j.error(ex);
					}
				} else {
					Log4j.log("the preprocess file of file(" + fid
							+ ") is wrong. ");
				}
			} else {
				Log4j.error("the bilifile of file(" + fid + ") load error. ");
			}
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (procloadfiledao != null) {
				procloadfiledao.UnInit();
			}
			if (dfs != null) {
				dfs.UnInit();
			}
		}
		return ret;
	}

	private JSONObject FragToJSon(ArrayList<Sentence> sentences,
			HashMap<String, PreprocessSentence> sentenceMap, boolean notdone) {
		JSONObject obj = new JSONObject();
		JSONObject terms;
		JSONObject term;
		JSONObject term_details;
		JSONObject term_detail;
		// int sentIndex = 0;
		PreprocessSentence preprocessSentence = null;
		for (Sentence sent : sentences) {

			// 术语
			JSONObject sentObj = new JSONObject();
			sentObj.put("src", sent._source);
			sentObj.put("T", sent._translate);
			sentObj.put("E", sent._edit);
			sentObj.put("T_r", sent._translate_r);
			sentObj.put("E_r", sent._edit_r);
			sentObj.put("recom", sent._recommend);
			// sentObj.put("tagcount", sent._sourceTagCount);
			if (sent instanceof SDLXliffSentence) {
				// sdlxliff添加匹配度，锁定
				sent = (SDLXliffSentence) sent;
				sentObj.put("lock", ((SDLXliffSentence) sent)._lock);
				sentObj.put("percent", ((SDLXliffSentence) sent)._percent);
			} else if (sent instanceof TTXSentence) {
				// ttx添加匹配读
				sent = (TTXSentence) sent;
				sentObj.put("percent", ((TTXSentence) sent)._percent);
			} else {
				sentObj.put("src", Util.replaceCodeSpecialChat(sent._source));
				sentObj.put("T", Util.replaceCodeSpecialChat(sent._translate));
				sentObj.put("E", Util.replaceCodeSpecialChat(sent._edit));
				sentObj.put("T_r", Util.replaceCodeSpecialChat(sent._translate_r));
				sentObj.put("E_r", Util.replaceCodeSpecialChat(sent._edit_r));
				sentObj.put("recom",
						Util.replaceCodeSpecialChat(sent._recommend));
			}

			sentObj.put("score", sent._eScore);
			// System.out.println((Const.FRAG_SIZE * sent._virtualFragIndex
			// + sent._virtualSentenceIndex + 1)
			// + ":" + sent._hashcode);
			sentObj.put("hashcode", sent._hashcode);
			sentObj.put("hashstatus", String.valueOf(sent._hashstatus));
			sentObj.put("sentindex", Const.FRAG_SIZE * sent._virtualFragIndex
					+ sent._virtualSentenceIndex + 1);
			if (sentenceMap != null
					&& sentenceMap.containsKey(sent._entityFragType.toString()
							+ "_" + sent._entityFragIndex + "_"
							+ sent._entitySentenceIndex)) {
				// 预处理文件
				preprocessSentence = sentenceMap.get(sent._entityFragType
						.toString()
						+ "_"
						+ sent._entityFragIndex
						+ "_"
						+ sent._entitySentenceIndex);
				sentObj.put("mt", preprocessSentence.targetText);
				terms = new JSONObject();
				int termnum = 0;
				for (PreprocessTerm preprocessterm : preprocessSentence.termList) {
					if (preprocessterm != null) {
						term = new JSONObject();
						term.put("term_id", preprocessterm.term_id);
						term.put("term", preprocessterm.term);
						term.put("begin", String.valueOf(preprocessterm.begin));
						term.put("end", String.valueOf(preprocessterm.end));
						term_details = new JSONObject();
						for (PreprocessTermDetail detail : preprocessterm.termDetailsList) {
							term_detail = new JSONObject();
							term_detail.put("meaning", detail.meaning);
							term_detail.put("usage", detail.usage);
							term_detail.put("remark", detail.remark);
							term_details.put(
									String.valueOf(detail.translator_id),
									term_detail);
						}
						term.put("details", term_details);
						terms.put(String.valueOf(termnum++), term);
					}
				}

				sentObj.put("terms", terms);
			}
			if (notdone) {
				obj.put(sent._virtualFragIndex + "_"
						+ sent._virtualSentenceIndex, sentObj);
			} else {
				obj.put(Integer.toString(sent._virtualSentenceIndex), sentObj);
			}
		}

		return obj;
	}

	// JSONObject :
	// { PHP连接ID，文件编号，文件总页数，类型T/E（尚未配置）
	// 文本内容 { 第一句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，
	// 第二句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，}，...
	// 第N句{句子编号，原文，T，E，推荐译文，推荐术语 {术语总数，术语一{原文，译文}，...术语N{原文，译文} }}，} } }

	private int StartHandler(JSONObject obj) {
		int ret = Const.FAIL;

		// 加载双语文件
		int fnid = Util.GetIntFromJSon("fnid", obj);
		String fid = Util.GetStringFromJSon("fid", obj);
		JSONObject resObj = new JSONObject();
		try {
			// 参数用来表达是新加载的双语文件还是以前就已经存在的
			boolean isopened = false;
			if (fid != null && !_files.containsKey(fid)) {
				// 双语文件加载
				ret = LoadFile(fnid, fid);
				isopened = false;
			} else {
				ret = Const.SUCCESS;
				isopened = true;
			}
			if (Const.SUCCESS == ret) {
				JSONObject contentObj = new JSONObject();
				int fragindex = 0;
				BiliFileNoTag file = _files.get(fid);
				String openstate = Util.GetStringFromJSon("openstate", obj);
				boolean remove = false;
				if (openstate != null && file._openstate != null
						&& file._openstate.length == 10) {
					switch (openstate.toLowerCase()) {
					case "t":
						file._openstate[0] = true;
						break;
					case "e":
						file._openstate[1] = true;
						break;
					case "s":
						file._openstate[2] = true;
						break;
					case "q":
						if (isopened && !file._openstate[1]
								&& !file._openstate[4]) {
							// 当遇到q并且不所eq打开时，重新打开文件
							boolean[] ostate = file._openstate;
							LoadFile(fnid, fid);
							remove = true;
							file = _files.get(fid);
							file._openstate = ostate;
						}
						file._openstate[4] = true;
						break;
					default:
						break;
					}
				}
				PreprocessFile preprocessFile = null;
				// 预处理文件
				if (_preprocessfiles.containsKey(fid)) {
					preprocessFile = _preprocessfiles.get(fid);
				}

				for (VirtualFragmentation frag : file._virtualFrags) {

					// 生成一页
					JSONObject fragObj = FragToJSon(
							frag._sentences,
							(preprocessFile == null) ? null : preprocessFile
									.GetSentecenMap(), false);
					if (fragObj != null) {

						contentObj.put(Integer.toString(fragindex++), fragObj);
					}
				}

				if (!_hashcodeSentences.containsKey(fid)) {
					_hashcodeSentences.put(fid, file.GetHashCodeMap());
				}
				BiliFileDetails details = file.GetBiliFileDetails();

				// 记录当前文件页数，用于PHP分页显示
				resObj.put("result", "OK");
				resObj.put("pagecount", Integer.toString(fragindex));
				resObj.put("fid", fid);
				resObj.put("countperpage", String.valueOf(Const.FRAG_SIZE));
				resObj.put("content", contentObj);
				resObj.put("tag", "");// file._tagId);
				resObj.put("sentcount", String.valueOf(details.sentenceCount));
				resObj.put("tsentcount",
						String.valueOf(details.tranlateSentenceCount));
				resObj.put("esentcount",
						String.valueOf(details.editSentenceCount));
				resObj.put("wordcount", String.valueOf(details.wordCount));
				resObj.put("twordcount",
						String.valueOf(details.tranlateWordCount));
				resObj.put("ewordcount", String.valueOf(details.editWordCount));
				if (remove) {
					resObj.put("openstate", "Q");
					resObj.put("remove", "remove");
				}
			} else {
				resObj.put("result", "FAILED");
			}

		} catch (Exception e) {
			Log4j.error(e);
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

		return ret;
	}

	private String Cleanup(int fnid, BiliFileNoTag file, String fid,
			SENTENCE_STATE state) {
		String target_file_id = "";

		if (Const.SUCCESS == file.Cleanup(state)) {
			// 将Cleanup后的文件ID保存到数据库中并从文件系统中删除原来的
			OrderFileDAO orderfiledao = null;
			FastDFS dfs = null;
			try {
				dfs = new FastDFS();
				// 上传新目标文件
				dfs.Init();
				target_file_id = dfs.Upload(file.GetTargetFilePath(),
						new FileUtil().GetExtFromFileName(file
								.GetTargetFilePath()));
				orderfiledao = new OrderFileDAO();
				orderfiledao.Init(true);
				OrderFileBean orderfilebean;
				if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
					orderfilebean = orderfiledao.Select(Util.String2Int(fid));
				} else {
					orderfilebean = orderfiledao.SelectForNode(fnid,
							Util.String2Int(fid));
				}
				orderfiledao.UnInit();
				if (orderfilebean != null) {
					if (orderfilebean.status == 20 && state == SENTENCE_STATE.T) {

						orderfiledao.Init(true);
						String old_trans_file_id = orderfilebean.trans_file_id;

						orderfilebean.trans_file_id = target_file_id;
						if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
							orderfiledao.UpdateTransFile(orderfilebean);
						} else {
							orderfiledao.UpdateTransFileForNode(orderfilebean);
						}
						if (old_trans_file_id != null
								&& old_trans_file_id.trim().length() > 0) {
							dfs.Delete(old_trans_file_id);
						}
						orderfiledao.Commit();
					} else if (orderfilebean.status == 30
							&& state == SENTENCE_STATE.E) {
						orderfiledao.Init(true);
						String old_edit_file_id = orderfilebean.edit_file_id;

						orderfilebean.edit_file_id = target_file_id;
						if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
							orderfiledao.UpdateEditFile(orderfilebean);
						} else {
							orderfiledao.UpdateEditFileForNode(orderfilebean);
						}

						if (old_edit_file_id != null
								&& old_edit_file_id.trim().length() > 0) {
							dfs.Delete(old_edit_file_id);
						}
						orderfiledao.Commit();
					} else {
						Log4j.error("file(" + fid
								+ ")'s status is not 20 or 30. ");
					}

				} else {
					Log4j.error("order_file table file(" + fid
							+ ")is not exists. ");
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (orderfiledao != null) {
					orderfiledao.UnInit();
				}
				if (dfs != null) {
					dfs.UnInit();
				}
			}

		} else {
			Log4j.error("Clean up failed.");
		}

		return target_file_id;
	}

	private int FullDownload(JSONObject obj) {
		int ret = Const.FAIL;
		int fnid = Util.GetIntFromJSon("fnid", obj);
		String fid = Util.GetStringFromJSon("fid", obj);
		String sstate = Util.GetStringFromJSon("sentstate", obj);
		String target_file_id = null;
		if (_files.containsKey(fid)) {
			BiliFileNoTag file = _files.get(fid);
			SENTENCE_STATE state = SENTENCE_STATE.String2Enum(sstate);
			if (state == SENTENCE_STATE.NONE) {
				state = SENTENCE_STATE.E;
			}
			target_file_id = Cleanup(fnid, file, fid, state);
		} else {
			Log4j.error("file(" + fid + ") is not load. ");
		}

		JSONObject resObj = new JSONObject();
		// 记录当前文件页数，用于PHP分页显示

		resObj.put("fid", fid);
		if (target_file_id != null) {
			resObj.put("full_file_id", target_file_id);
			ret = Const.SUCCESS;
		}
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));

		if (Const.SUCCESS == ret) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}

		_reportor.Report(resObj);
		return ret;
	}

	private int StopHandler(JSONObject obj, boolean compel) {
		int ret = Const.FAIL;
		int fnid = Util.GetIntFromJSon("fnid", obj);
		String fid = Util.GetStringFromJSon("fid", obj);
		String sstate = Util.GetStringFromJSon("sentstate", obj);
		String remove = "";
		if (fid != null && _files.containsKey(fid)) {
			BiliFileNoTag file = _files.get(fid);
			if (file != null) {

				if (fnid <= 0) {
					fnid = file._node_id;
				}
				SENTENCE_STATE state = SENTENCE_STATE.String2Enum(sstate);
				if (state == SENTENCE_STATE.NONE) {
					state = SENTENCE_STATE.E;
				}
				Cleanup(fnid, file, fid, state);
				BiliFileDetails details = file.GetBiliFileDetails();
				ret = file.Save();
				if (Const.SUCCESS == ret) {
					// 将双语文件ID保存到数据库中并从文件系统中删除原来的双语文件
					ret = SynBiliFile(fnid, file, fid,
							details.tranlateWordCount, details.editWordCount);
					if (Const.SUCCESS == ret) {
						String openstate = Util.GetStringFromJSon("openstate",
								obj);
						if (openstate != null && file._openstate != null
								&& file._openstate.length == 10
								&& !file._openstate[0] && !file._openstate[1]
								&& !file._openstate[2] && !file._openstate[3]
								&& !file._openstate[4] && !file._openstate[5]
								&& !file._openstate[6] && !file._openstate[7]
								&& !file._openstate[8] && !file._openstate[9]) {
							switch (openstate.toLowerCase()) {
							case "t":
								file._openstate[0] = false;
								break;
							case "e":
								file._openstate[1] = false;
								break;
							case "s":
								file._openstate[2] = false;
								break;
							case "q":
								file._openstate[4] = true;
								break;
							default:
								break;
							}
						}

						if (compel || file._openstate != null
								&& file._openstate.length == 10
								&& !file._openstate[0] && !file._openstate[1]
								&& !file._openstate[2] && !file._openstate[3]
								&& !file._openstate[4] && !file._openstate[5]
								&& !file._openstate[6] && !file._openstate[7]
								&& !file._openstate[8] && !file._openstate[9]) {
							if (_preprocessfiles.containsKey(fid)) {
								_preprocessfiles.remove(fid);
							}
							if (_hashcodeSentences.containsKey(fid)) {
								_hashcodeSentences.remove(fid);
							}

							_files.remove(fid);
							file.UnInit();
							remove = "remove";
						}
					}
				} else {
					Log4j.error("Save bill failed.");
				}
			}
		} else {
			Log4j.error("文件ID(" + fid + ")未加载[StopHandler]");
		}

		JSONObject resObj = new JSONObject();
		// 记录当前文件页数，用于PHP分页显示
		resObj.put("fid", fid);
		resObj.put("remove", remove);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));

		if (Const.SUCCESS == ret) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}
		_reportor.Report(resObj);
		return ret;
	}

	private int SynBiliFile(int fnid, BiliFileNoTag file, String fid,
			int translate, int edit) {
		int ret = Const.FAIL;
		OrderFileDAO orderfiledao = null;
		FastDFS dfs = null;
		try {

			dfs = new FastDFS();
			// 上传新双语文件
			dfs.Init();
			String b_file_id = dfs.Upload(file.GetBiliFilePath(), "xml");

			orderfiledao = new OrderFileDAO();
			orderfiledao.Init(true);
			OrderFileBean orderfilebean;
			if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
				orderfilebean = orderfiledao.Select(Util.String2Int(fid));
			} else {
				orderfilebean = orderfiledao.SelectForNode(fnid,
						Util.String2Int(fid));
			}

			String old_b_file_id = orderfilebean.b_file_id;
			orderfilebean.b_file_id = b_file_id;
			orderfilebean.edit_score = file.GetEditScore();
			orderfilebean.finished_word_count_t = translate;
			orderfilebean.finished_word_count_e = edit;
			if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
				// 修改双语文件
				orderfiledao.UpdateBiliFile(orderfilebean);
				// 修改校对评分
				orderfiledao.UpdateEditScore(orderfilebean);

				orderfiledao.UpdateWordCount(orderfilebean);
				orderfiledao.Commit();
			} else {
				// 修改双语文件
				orderfiledao.UpdateBiliFileForNode(orderfilebean);
				// 修改校对评分
				orderfiledao.UpdateEditScoreForNode(orderfilebean);

				orderfiledao.UpdateWordCountForNode(orderfilebean);
				orderfiledao.Commit();
			}

			SynOrderWordCount(fnid, orderfilebean.order_id);

			if (orderfilebean != null && old_b_file_id != null
					&& old_b_file_id.trim().length() > 0) {
				dfs.Delete(old_b_file_id);
			}
			dfs.UnInit();
			ret = Const.SUCCESS;

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (orderfiledao != null) {
				orderfiledao.UnInit();
			}
			if (dfs != null) {
				dfs.UnInit();
			}
		}
		return ret;
	}

	private int SynOrderWordCount(int nid, int order_id) {
		int ret = Const.FAIL;
		OrderDAO orderdao = null;
		try {

			orderdao = new OrderDAO();
			orderdao.Init(true);
			if (nid <= 0 || BundleConf.DEFAULT_NID == nid) {
				orderdao.UpdateWordCount(order_id);
			} else {
				orderdao.UpdateWordCountForNode(nid, order_id);
			}
			orderdao.Commit();
			ret = Const.SUCCESS;

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (orderdao != null) {
				orderdao.UnInit();
			}
		}
		return ret;
	}

	private int SynFileHandler(JSONObject obj) {
		int ret = Const.FAIL;
		int fnid = Util.GetIntFromJSon("fnid", obj);
		String fid = Util.GetStringFromJSon("fid", obj);

		String b_file_id = "";
		if (fid != null && _files.containsKey(fid)) {
			BiliFileNoTag file = _files.get(fid);
			BiliFileDetails details = file.GetBiliFileDetails();
			ret = file.Save();
			if (Const.SUCCESS == ret) {
				// 将双语文件ID保存到数据库中并从文件系统中删除原来的双语文件
				ret = SynBiliFile(fnid, file, fid, details.tranlateWordCount,
						details.editWordCount);
				if (Const.SUCCESS == ret) {

					OrderFileDAO orderfiledao = null;
					try {
						orderfiledao = new OrderFileDAO();
						orderfiledao.Init(true);
						OrderFileBean orderfilebean;
						if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
							orderfilebean = orderfiledao.Select(Util
									.String2Int(fid));
						} else {
							orderfilebean = orderfiledao.SelectForNode(fnid,
									Util.String2Int(fid));
						}
						if (orderfilebean != null) {
							b_file_id = orderfilebean.b_file_id;
						}

					} catch (Exception e) {
						Log4j.error(e);
					} finally {
						if (orderfiledao != null) {
							orderfiledao.UnInit();
						}
					}

				} else {
					Log4j.error("双语文件保存数据库失败");
				}

			} else {
				Log4j.error("Save bill failed.");
			}
		} else {
			Log4j.error("文件ID(" + fid + ")未加载[SynFileHandler]");
		}
		JSONObject resObj = new JSONObject();
		// 记录当前文件页数，用于PHP分页显示

		resObj.put("fid", fid);
		resObj.put("b_file_id", b_file_id);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));

		if (Const.SUCCESS == ret) {
			resObj.put("result", "OK");
		} else {
			resObj.put("result", "FAILED");
		}
		_reportor.Report(resObj);
		return ret;
	}

	private int Close(JSONObject obj) {
		int ret = Const.FAIL;

		Set<String> fids = _files.keySet();
		for (String fid : fids) {
			JSONObject json = new JSONObject();
			json.put("fid", fid);
			this.StopHandler(json, true);
		}

		JSONObject resObj = new JSONObject();
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));

		resObj.put("result", "OK");
		_reportor.Report(resObj);
		return ret;
	}

	private int SaveAsTmx(JSONObject obj) {
		int ret = Const.FAIL;

		// 加载双语文件，后生成tmx文件
		int fnid = Util.GetIntFromJSon("fnid", obj);
		int order_id = Util.GetIntFromJSon("order_id", obj);
		String slang = Util.GetStringFromJSon("slang", obj);
		String tlang = Util.GetStringFromJSon("tlang", obj);

		JSONObject resObj = new JSONObject();
		if (order_id > 0 && slang != null && tlang != null) {
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

		OrderFileDAO orderfiledao = null;
		OrderDAO orderdao = null;
		FastDFS dfs = null;
		try {
			orderfiledao = new OrderFileDAO();
			orderfiledao.Init(true);
			List<OrderFileBean> orderfilelist;
			if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
				orderfilelist = orderfiledao.SelectByOrderID(order_id);
			} else {
				orderfilelist = orderfiledao.SelectByOrderIDForNode(fnid,
						order_id);
			}
			if (orderfilelist != null && orderfilelist.size() > 0) {
				for (OrderFileBean orderFileBean : orderfilelist) {

					int fid = orderFileBean.file_id;
					String sfid = String.valueOf(fid);
					if (fid > 0) {
						if (!_files.containsKey(sfid)) {
							// 双语文件加载
							ret = LoadFile(fnid, sfid);
							BiliFileNoTag file = _files.get(sfid);
							if (file != null) {
								file._openstate[3] = true;
							} else {
								// 当源文件不可解析时，没有双语文件
								return Const.FAIL;
							}
						} else {
							ret = Const.SUCCESS;
							BiliFileNoTag file = _files.get(sfid);
							if (file != null) {
								file._openstate[3] = true;
							}
						}
						if (ret == Const.SUCCESS) {
							BiliFileNoTag file = _files.get(sfid);
							if (file != null && file._sourceLang != null
									&& file._targetLang != null) {
								String tmxfilepath = _path + sfid + ".tmx";

								TmxFile tmxFile = new TmxFile();
								tmxFile.Init(file, tmxfilepath, slang, tlang);
								tmxFile.Parse();
								ret = tmxFile.WriteTMXFile();

								tmxFile.UnInit();
								file._openstate[3] = false;
								if (file._openstate != null
										&& file._openstate.length == 10
										&& !file._openstate[0]
										&& !file._openstate[1]
										&& !file._openstate[2]
										&& !file._openstate[3]
										&& !file._openstate[4]
										&& !file._openstate[5]
										&& !file._openstate[6]
										&& !file._openstate[7]
										&& !file._openstate[8]
										&& !file._openstate[9]) {
									file.UnInit();
									_files.remove(sfid);
								}
								if (ret == Const.SUCCESS) {

									dfs = new FastDFS();
									// 上传新tmx文件
									dfs.Init();
									String tmx_file_id = dfs.Upload(
											tmxfilepath, "tmx");
									OrderFileBean orderfilebean;
									if (fnid <= 0
											|| BundleConf.DEFAULT_NID == fnid) {
										orderfilebean = orderfiledao
												.Select(fid);
									} else {
										orderfilebean = orderfiledao
												.SelectForNode(fnid, fid);
									}

									String old_tmx_file_id = orderfilebean.tmx_file_id;
									orderfilebean.tmx_file_id = tmx_file_id;

									// 修改tmx文件
									if (fnid <= 0
											|| BundleConf.DEFAULT_NID == fnid) {
										orderfiledao
												.UpdateTmxFile(orderfilebean);
									} else {
										orderfiledao
												.UpdateTmxFileForNode(orderfilebean);
									}
									orderfiledao.Commit();

									Set<Integer> set = BundleConf.BUNDLE_Node
											.keySet();
									ConfigNode bs;
									for (Integer node_id : set) {
										if (node_id > 0
												&& node_id == BundleConf.DEFAULT_NID) {
											bs = BundleConf.BUNDLE_Node
													.get(node_id);
											if (bs != null) {
												JSONObject paramJson = new JSONObject();
												paramJson
														.put("className",
																"com.wiitrans.automation.logic.SyncDataLogicImpl");
												paramJson
														.put("file_id",
																String.valueOf(orderfilebean.file_id));
												paramJson
														.put("nid",
																String.valueOf(node_id));
												paramJson.put("uid", Util
														.GetStringFromJSon(
																"uid", obj));
												paramJson.put("syncType",
														"send");
												if (fnid <= 0
														|| BundleConf.DEFAULT_NID == fnid) {
													paramJson.put(
															"dataTemplate",
															"synTMX");
												} else {
													paramJson.put(
															"dataTemplate",
															"synNodeTMX");
												}
												JSONObject jsonObject = new JSONObject();
												jsonObject.put("sid", Util
														.GetStringFromJSon(
																"sid", obj));
												jsonObject.put("uid", Util
														.GetStringFromJSon(
																"uid", obj));
												jsonObject
														.put("nid",
																String.valueOf(node_id));
												jsonObject.put("param",
														paramJson);
												jsonObject.put("taskType", "1");
												jsonObject.put("corn", "");
												jsonObject
														.put("job_class",
																"com.wiitrans.automation.quartz.job.PushStromJob");

												new HttpSimulator(bs.api
														+ "automation/newtask/")
														.executeMethodTimeOut(
																jsonObject
																		.toString(),
																bs.timeout);
											}
										}
									}

									if (orderfilebean != null
											&& old_tmx_file_id != null
											&& old_tmx_file_id.trim().length() > 0) {
										dfs.Delete(old_tmx_file_id);
									}
									dfs.UnInit();
								}
							}
						}
					}
				}
			}
			orderdao = new OrderDAO();
			orderdao.Init(true);
			OrderBean order = orderdao.Select(order_id);
			if (BundleConf.DEFAULT_NID == fnid && order != null
					&& order.tm_id > 0) {
				JSONObject tmsvr = new JSONObject();
				tmsvr.put("sid", Util.GetStringFromJSon("sid", obj));
				tmsvr.put("uid", Util.GetStringFromJSon("uid", obj));
				tmsvr.put("nid", Util.GetStringFromJSon("nid", obj));
				tmsvr.put("order_id", Util.GetStringFromJSon("order_id", obj));
				tmsvr.put("tmid", String.valueOf(order.tm_id));
				tmsvr.put("method", "PUT");
				if (fnid <= 0 || BundleConf.DEFAULT_NID == fnid) {
					tmsvr.put("tnid", String.valueOf(BundleConf.DEFAULT_NID));
				}

				// "sid":"sid_0","uid":"0","nid":"1","tmid":"77","order_id":"1380","tnid":"1"
				Log4j.info("save tmx tmsvr json:" + tmsvr.toString());
				new HttpSimulator(BundleConf.BUNDLE_TMSVR_API + "manage")
						.executeMethodTimeOut(tmsvr.toString(), 2);
			}
		} catch (Exception e) {
			Log4j.error(e);
			ret = Const.FAIL;
		} finally {
			if (dfs != null) {
				dfs.UnInit();
			}
			if (orderfiledao != null) {
				orderfiledao.UnInit();
			}
			if (orderdao != null) {
				orderdao.UnInit();
			}
		}

		return ret;
	}

	private int NotDonePage(JSONObject obj) {
		int ret = Const.FAIL;

		try {
			String fid = Util.GetStringFromJSon("fid", obj);

			if (_files.containsKey(fid)) {
				// PHP在线编辑器未翻译页面翻页
				int countperpage = Util.GetIntFromJSon("countperpage", obj);
				if (countperpage <= Const.FRAG_SIZE) {
					countperpage = Const.FRAG_SIZE;
				}

				JSONObject result = new JSONObject();

				String fragSentIndex = Util.GetStringFromJSon("FragSentIndex",
						obj);
				if (fragSentIndex != null) {
					int fragIndex = -1;
					int sentIndex = -1;

					if (fragSentIndex.equalsIgnoreCase("new")) {
						// new是从第一句开始
						fragIndex = 0;
						sentIndex = 0;
					} else {
						// 最后一句的frag和sent的索引
						String[] indexes = fragSentIndex.split("_");
						if (indexes.length == 2) {
							fragIndex = Util.String2Int(indexes[0]);
							sentIndex = Util.String2Int(indexes[1]) + 1;
						}
					}
					ArrayList<Sentence> page = new ArrayList<Sentence>();

					BiliFileNoTag file = _files.get(fid);
					String type = Util.GetStringFromJSon("type", obj);
					boolean transType = true;
					switch (type.toUpperCase()) {
					case "T": {
						// notDoneSentences = file._notDoneTSentences;
						transType = true;
						break;
					}
					case "E": {
						// notDoneSentences = file._notDoneESentences;
						transType = false;
						break;
					}
					default:
						break;
					}

					// 循环未翻译句子数，达到要求数量就不再添加进page中，
					// 但是会执行到结束，计算出总的未翻译句子数
					int notDoneSentCount = 0;
					boolean pageflag = false;
					if (file._virtualFragCount > fragIndex && fragIndex >= 0
							&& sentIndex >= 0) {

						for (int i = fragIndex; i < file._virtualFragCount; i++) {
							VirtualFragmentation frag = file._virtualFrags
									.get(i);
							for (int j = sentIndex; j < frag._sentenceCount; j++) {
								Sentence sent = frag._sentences.get(j);
								if (transType) {
									if (sent instanceof SDLXliffSentence) {
										if (!((SDLXliffSentence) sent)._lock) {
											if (!sent._translatestatus) {
												if (!pageflag) {
													page.add(sent);
													if (page.size() >= countperpage) {
														pageflag = true;
													}
												}
												notDoneSentCount++;
											}
										}
									} else {
										if (!sent._translatestatus) {
											if (!pageflag) {
												page.add(sent);
												if (page.size() >= countperpage) {
													pageflag = true;
												}
											}
											notDoneSentCount++;
										}
									}
								} else {
									if (sent instanceof SDLXliffSentence) {
										if (!((SDLXliffSentence) sent)._lock) {
											if (!sent._editstatus) {
												if (!pageflag) {
													page.add(sent);
													if (page.size() >= countperpage) {
														pageflag = true;
													}
												}
												notDoneSentCount++;
											}
										}
									} else {
										if (!sent._editstatus) {
											if (!pageflag) {
												page.add(sent);
												if (page.size() >= countperpage) {
													pageflag = true;
												}
											}
											notDoneSentCount++;
										}
									}
								}
							}
							sentIndex = 0;
						}

						PreprocessFile preprocessFile = null;

						if (_preprocessfiles.containsKey(fid)) {
							preprocessFile = _preprocessfiles.get(fid);
						}

						JSONObject resObj = FragToJSon(page,
								(preprocessFile == null) ? null
										: preprocessFile.GetSentecenMap(), true);
						result.put("content", resObj);

						result.put("sentcount", notDoneSentCount);

						result.put("pagecount", (notDoneSentCount
								+ countperpage - 1)
								/ countperpage);
					}
				}

				result.put("result", "OK");

				result.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
				result.put(Const.BUNDLE_INFO_ID,
						Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
				result.put(Const.BUNDLE_INFO_ACTION_ID, Util.GetStringFromJSon(
						Const.BUNDLE_INFO_ACTION_ID, obj));
				result.put(Const.BUNDLE_INFO_BUNDLE_ID, Util.GetStringFromJSon(
						Const.BUNDLE_INFO_BUNDLE_ID, obj));
				_reportor.Report(result);

				ret = Const.SUCCESS;
			}
		} catch (Exception e) {
			Log4j.error(e);
			JSONObject result = new JSONObject();
			result.put("content", "");
			result.put("result", "FAILED");
			result.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			result.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			result.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
			result.put(Const.BUNDLE_INFO_BUNDLE_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
			_reportor.Report(result);
		}

		return ret;
	}

	private int GetSentsByLen(JSONObject obj) {
		int ret = Const.FAIL;

		try {
			String fid = Util.GetStringFromJSon("fid", obj);

			if (_files.containsKey(fid)) {
				// PHP在线编辑器未翻译页面翻页
				int sentcount = Util.GetIntFromJSon("sentcount", obj);
				if (sentcount < 0) {
					sentcount = 0;
				}

				JSONObject result = new JSONObject();

				String fragSentIndex = Util.GetStringFromJSon("FragSentIndex",
						obj);
				if (fragSentIndex != null) {
					// 类似与未翻译句子列表方法NotDonePage，不需要全部扫描，达到要求直接跳出嵌套循环
					int fragIndex = -1;
					int sentIndex = -1;
					if (fragSentIndex.equalsIgnoreCase("new")) {
						fragIndex = 0;
						sentIndex = 0;
					} else {
						String[] indexes = fragSentIndex.split("_");
						if (indexes.length == 2) {
							fragIndex = Util.String2Int(indexes[0]);
							sentIndex = Util.String2Int(indexes[1]) + 1;
						}
					}
					ArrayList<Sentence> page = new ArrayList<Sentence>();

					BiliFileNoTag file = _files.get(fid);

					if (file._virtualFragCount > fragIndex && fragIndex >= 0
							&& sentIndex >= 0) {

						outerLoop: for (int i = fragIndex; i < file._virtualFragCount; i++) {
							VirtualFragmentation frag = file._virtualFrags
									.get(i);
							for (int j = sentIndex; j < frag._sentenceCount; j++) {
								Sentence sent = frag._sentences.get(j);
								page.add(sent);
								if (page.size() >= sentcount) {
									break outerLoop;
								}

							}
							sentIndex = 0;
						}

						PreprocessFile preprocessFile = null;

						if (_preprocessfiles.containsKey(fid)) {
							preprocessFile = _preprocessfiles.get(fid);
						}

						JSONObject resObj = FragToJSon(page,
								(preprocessFile == null) ? null
										: preprocessFile.GetSentecenMap(), true);
						result.put("content", resObj);
						result.put("pagecount",
								Integer.toString(file._virtualFragCount));
					}
				}

				result.put("result", "OK");
				result.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
				result.put(Const.BUNDLE_INFO_ID,
						Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
				result.put(Const.BUNDLE_INFO_ACTION_ID, Util.GetStringFromJSon(
						Const.BUNDLE_INFO_ACTION_ID, obj));
				result.put(Const.BUNDLE_INFO_BUNDLE_ID, Util.GetStringFromJSon(
						Const.BUNDLE_INFO_BUNDLE_ID, obj));
				_reportor.Report(result);

				ret = Const.SUCCESS;
			}
		} catch (Exception e) {
			Log4j.error(e);
			JSONObject result = new JSONObject();
			result.put("content", "");
			result.put("result", "FAILED");
			result.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
			result.put(Const.BUNDLE_INFO_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
			result.put(Const.BUNDLE_INFO_ACTION_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
			result.put(Const.BUNDLE_INFO_BUNDLE_ID,
					Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));
			_reportor.Report(result);
		}

		return ret;
	}

	private int CloseFile(JSONObject obj) {
		int ret = Const.FAIL;
		String fid = Util.GetStringFromJSon("fid", obj);
		if (fid != null && _files.containsKey(fid)) {
			BiliFileNoTag file = _files.get(fid);
			if (file != null && file._openstate != null
					&& file._openstate.length == 10 && !file._openstate[0]
					&& !file._openstate[1] && !file._openstate[2]
					&& !file._openstate[3] && !file._openstate[4]
					&& !file._openstate[5] && !file._openstate[6]
					&& !file._openstate[7] && !file._openstate[8]
					&& !file._openstate[9]) {
				if (_preprocessfiles.containsKey(fid)) {
					_preprocessfiles.remove(fid);
				}

				if (_hashcodeSentences.containsKey(fid)) {
					_hashcodeSentences.remove(fid);
				}
				_files.remove(fid);
				file.UnInit();
			}
		} else {
			Log4j.error("文件ID(" + fid + ")未加载[CloseFile]");
		}

		JSONObject resObj = new JSONObject();
		// 记录当前文件页数，用于PHP分页显示
		resObj.put("fid", fid);
		resObj.put(Const.BUNDLE_INFO_STATE, Const.BUNDLE_REPORT);
		resObj.put(Const.BUNDLE_INFO_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ID, obj));
		resObj.put(Const.BUNDLE_INFO_ACTION_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_ACTION_ID, obj));
		resObj.put(Const.BUNDLE_INFO_BUNDLE_ID,
				Util.GetStringFromJSon(Const.BUNDLE_INFO_BUNDLE_ID, obj));

		resObj.put("result", "OK");

		_reportor.Report(resObj);
		return ret;
	}

	private JSONObject CheckDigit(BiliFileNoTag file) {
		// 术语检验
		JSONObject digitjson = new JSONObject();
		for (int i = 0; i < file._virtualFragCount; i++) {
			Fragmentation frag = file._virtualFrags.get(i);
			// frag循环
			if (frag != null) {
				for (int j = 0; j < frag._sentences.size(); j++) {
					Sentence sent = frag._sentences.get(j);
					// 句子循环
					if (sent != null) {
						int sentno = i * Const.FRAG_SIZE + j + 1;
						String sourceDigit = null, targetDigit = null;
						JSONObject sentdigitJson = new JSONObject();
						if (sent._sourceDigit != null
								&& sent._sourceDigit.trim().length() > 0) {
							sourceDigit = sent._sourceDigit;
							sentdigitJson.put("source", sourceDigit);
						} else {
							sentdigitJson.put("source", "");
						}
						if (sent._targetDigit != null
								&& sent._targetDigit.trim().length() > 0) {
							targetDigit = sent._targetDigit;
							sentdigitJson.put("target", targetDigit);
						} else {
							sentdigitJson.put("target", "");
						}
						if (sourceDigit != null || targetDigit != null) {
							digitjson
									.put(String.valueOf(sentno), sentdigitJson);
						}
					}
				}
			}
		}
		return digitjson;
	}

	private JSONObject CheckTerm(BiliFileNoTag file) {
		// 术语检验
		JSONObject termjson = new JSONObject();
		for (int i = 0; i < file._virtualFragCount; i++) {
			Fragmentation frag = file._virtualFrags.get(i);
			// frag循环
			if (frag != null) {
				for (int j = 0; j < frag._sentences.size(); j++) {
					Sentence sent = frag._sentences.get(j);
					// 句子循环
					if (sent != null) {
						int sentno = i * Const.FRAG_SIZE + j + 1;
						if (sent._termList != null && sent._termList.size() > 0) {
							JSONObject terms = new JSONObject();
							boolean termFlag = false;
							for (int k = 0; k < sent._termList.size(); k++) {
								Term term = sent._termList.get(k);
								// 术语循环
								if (term != null
										&& term.check_count > term.check_number) {
									JSONObject termcheck = new JSONObject();
									termcheck.put("term", term._term);
									termcheck
											.put("meaning", term.check_meaning);
									termcheck.put("count", term.check_count
											- term.check_number);
									terms.put(String.valueOf(k), termcheck);
									termFlag = true;
								}
							}
							if (termFlag) {
								termjson.put(String.valueOf(sentno), terms);
							}
						}
					}
				}
			}
		}
		return termjson;
	}

	private Sentence GetSentence(BiliFileNoTag file, int[] index) {
		// Sentence ret = null;
		if (index.length != 2) {
			return null;
		}

		if (index[0] >= file._virtualFragCount) {
			return null;
		}
		VirtualFragmentation vFrag = file._virtualFrags.get(index[0]);
		// 含有这个句子
		if (vFrag == null || vFrag._sentences == null) {
			return null;
		}
		if (index[1] >= vFrag._sentenceCount) {
			return null;
		}

		Sentence sentence = vFrag._sentences.get(index[1]);
		return sentence;
	}

	private boolean CheckSententRepetition(Sentence source, Sentence target) {
		// boolean flag = false;
		if (source == null) {
			if (target == null) {
				return true;
			} else {
				return false;
			}
		}
		String s;
		if (source._edit == null || source._edit.trim().length() == 0) {
			s = source._translate;
		} else {
			s = source._edit;
		}
		String t;
		if (target._edit == null || target._edit.trim().length() == 0) {
			t = target._translate;
		} else {
			t = target._edit;
		}
		if (s == null) {
			// source的译文T是空
			s = "";
		}
		if (t == null) {
			t = "";
		}
		return s.equals(t);
	}

	private void CheckRepetition(BiliFileNoTag file, ArrayList<int[]> list,
			Sentence sentence) {
		if (list != null && list.size() > 1) {
			Sentence firstsent = this.GetSentence(file, list.get(0));

			if (firstsent._virtualFragIndex == sentence._virtualFragIndex
					&& firstsent._virtualSentenceIndex == sentence._virtualSentenceIndex) {
				// 修改第一句，全部重新校验
				StringBuffer hashcheck = new StringBuffer()
						.append(Const.FRAG_SIZE * firstsent._virtualFragIndex
								+ firstsent._virtualSentenceIndex + 1);
				boolean have = false;
				for (int i = 1; i < list.size(); i++) {
					Sentence sent = this.GetSentence(file, list.get(i));
					if (!this.CheckSententRepetition(firstsent, sent)) {
						hashcheck.append(",").append(
								Const.FRAG_SIZE * sent._virtualFragIndex
										+ sent._virtualSentenceIndex + 1);
						have = true;
					}
				}
				if (have) {
					firstsent._hashCheck = hashcheck.toString();
				} else {
					firstsent._hashCheck = "";
				}
			} else {

				String hashcheck = firstsent._hashCheck;

				if (hashcheck != null && hashcheck.trim().length() > 0) {
					String[] indexes = hashcheck.split(",");
					if (indexes != null && indexes.length > 1) {
						// 旧结果基础上更新
						TreeSet<Integer> set = new TreeSet<Integer>();
						for (int i = 1; i < indexes.length; i++) {
							if (!set.contains(Util.String2Int(indexes[i]))) {
								set.add(Util.String2Int(indexes[i]));
							}
						}

						int index = Const.FRAG_SIZE
								* sentence._virtualFragIndex
								+ sentence._virtualSentenceIndex + 1;
						if (this.CheckSententRepetition(firstsent, sentence)) {
							if (set.contains(index)) {
								set.remove(index);
							}
						} else {
							if (!set.contains(index)) {
								set.add(index);
							}
						}
						if (set.size() == 0) {
							firstsent._hashCheck = "";
						} else {
							StringBuffer hashchecksb = new StringBuffer()
									.append(Const.FRAG_SIZE
											* firstsent._virtualFragIndex
											+ firstsent._virtualSentenceIndex
											+ 1);
							for (Integer indextmp : set) {
								hashchecksb.append(",").append(indextmp);
							}
							firstsent._hashCheck = hashchecksb.toString();
						}
						return;
					}
				}
				// 新的结果
				if (!this.CheckSententRepetition(firstsent, sentence)) {
					firstsent._hashCheck = (Const.FRAG_SIZE
							* firstsent._virtualFragIndex
							+ firstsent._virtualSentenceIndex + 1)
							+ ","
							+ (Const.FRAG_SIZE * sentence._virtualFragIndex
									+ sentence._virtualSentenceIndex + 1);
				}
			}
		}
	}

	private JSONObject CheckRepetition(BiliFileNoTag file,
			HashMap<Long, ArrayList<int[]>> hashmap) {
		// 一致性检验
		JSONObject repetitionjson = new JSONObject();
		Set<Long> set = hashmap.keySet();
		for (Long hashcode : set) {
			if (hashmap.containsKey(hashcode)) {
				ArrayList<int[]> list = hashmap.get(hashcode);
				if (list != null && list.size() > 1) {
					int[] senindex = list.get(0);

					Sentence sentence = this.GetSentence(file, senindex);
					if (sentence == null || !sentence._hashstatus) {
						continue;
					}
					if (sentence._hashCheck != null
							&& sentence._hashCheck.trim().length() > 0) {
						repetitionjson
								.put(String.valueOf(senindex[0]
										* Const.FRAG_SIZE + senindex[1] + 1),
										sentence._hashCheck);
					}
				}
			}
		}
		return repetitionjson;
	}

	private int Check(JSONObject obj) {
		int ret = Const.FAIL;
		JSONObject check = new JSONObject();
		try {

			// 增加Check
			String fid = Util.GetStringFromJSon("fid", obj);
			if (fid != null && fid.trim().length() > 0) {
				if (_files.containsKey(fid)) {
					// 得到文件
					BiliFileNoTag file = _files.get(fid);
					if (file != null) {
						check.put("term", CheckTerm(file));
						check.put("digit", CheckDigit(file));
						if (_hashcodeSentences.containsKey((fid))) {
							HashMap<Long, ArrayList<int[]>> hashmap = _hashcodeSentences
									.get(fid);
							if (hashmap != null && hashmap.size() > 0) {
								check.put("repetition",
										CheckRepetition(file, hashmap));
							} else {
								check.put("repetition", new JSONObject());
							}
						} else {
							check.put("repetition", new JSONObject());
						}
						ret = Const.SUCCESS;
					}
				} else {
					Log4j.error("文件ID(" + fid + ")未加载[SynHandler]");
				}
			} else {
				Log4j.error("文件ID无效");
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		JSONObject resObj = new JSONObject();
		if (Const.SUCCESS == ret) {
			resObj.put("result", "OK");
			resObj.put("check", check);
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

		return ret;
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		JSONObject obj = new JSONObject(tuple.getStringByField("content"));
		String aid = Util.GetStringFromJSon("aid", obj);

		Log4j.debug("fraghandlerbolt " + obj.toString());
		try {

			switch (aid) {
			case "syn": {
				// 同步保存当前句子
				SynHandler(obj, collector);
				break;
			}
			case "synfile": {
				// 保存当前文件并上传
				SynFileHandler(obj);
				break;
			}
			case "start": {
				// 开始在线翻译
				StartHandler(obj);
				break;
			}
			case "stop": {
				// 停止在线翻译,不强制执行
				StopHandler(obj, false);
				break;
			}
			case "full": {
				// 全文下载
				FullDownload(obj);
				break;
			}
			case "close": {
				// 关闭时自动保存未保存的文件
				Close(obj);
				break;
			}
			case "tmx": {
				// 保存tmx文件，在订单提交时调用
				SaveAsTmx(obj);
				break;
			}
			case "notdone": {
				// 未翻译列表,根据起始位置FragSentIndex和每页句子个数获得
				NotDonePage(obj);
				break;
			}
			case "closefile": {
				// 关闭文件
				CloseFile(obj);
				break;
			}
			case "check": {
				// 检查文件，检查数字、术语、一致性，只是把结果显示出来，具体检查算法在每次的句子保存时执行，【SynHandler-SynSentence】
				Check(obj);
				break;
			}
			case "sents": {
				// 获得一组句子，根据起始位置FragSentIndex和句子个数获得
				GetSentsByLen(obj);
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	@Override
	public void cleanup() {
		Log4j.log("fragclose-cleanup begin");
		if (_files != null && _files.size() > 0) {
			Set<String> fids = _files.keySet();
			for (String fid : fids) {
				Log4j.log("fragclose-cleanup fid" + fid);
				if (fid != null && _files.containsKey(fid)) {
					BiliFileNoTag file = _files.get(fid);
					BiliFileDetails details = file.GetBiliFileDetails();
					int ret = file.Save();
					if (Const.SUCCESS == ret) {
						// 将双语文件ID保存到数据库中并从文件系统中删除原来的双语文件
						SynBiliFile(file._node_id, file, fid,
								details.tranlateWordCount,
								details.editWordCount);
					}
				}
			}
		}
		super.cleanup();
		Log4j.debug("fragclose-cleanup end");
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("content"));
		// declarer.declare(new Fields("sid", "fid"));
	}
}
