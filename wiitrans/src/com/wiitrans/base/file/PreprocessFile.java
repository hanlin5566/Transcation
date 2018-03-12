package com.wiitrans.base.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.notag.BiliFileNoTag.ENTITY_FILE_TYPE;
import com.wiitrans.base.file.sentence.SDLXliffSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.TTXSentence;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XDocument;
import com.wiitrans.base.xml.XNode;

public class PreprocessFile {

	private XDocument _xmlDoc = null;
	private String _file_id = null;
	private Language _sourceLang = null;
	private Language _targetLang = null;
	private ArrayList<PreprocessSentence> _sentecenList = null;
	private HashMap<String, PreprocessSentence> _sentecenMap = null;
	public StringBuffer preview = null;

	public PreprocessFile(String path) {

		_file_id = null;
		_sourceLang = null;
		_targetLang = null;
		_sentecenList = null;
		_xmlDoc = new XDocument();
		XNode root = _xmlDoc.Parse(path);
		DetectLanguage detect = new DetectLanguage();

		_file_id = root.GetAttr("fileid");
		_sourceLang = detect.Detect(root.GetAttr("sourcelang"));

		_targetLang = detect.Detect(root.GetAttr("targetlang"));

		_sentecenList = new ArrayList<PreprocessSentence>();
		PreprocessSentence preprocessSentence = null;
		PreprocessTerm preprocessTerm = null;
		PreprocessTermDetail preprocessTermDetail = null;
		for (XNode node : root.GetChildren()) {
			if (0 == node.GetTagName().compareTo("sentence")) {
				preprocessSentence = new PreprocessSentence();
				preprocessSentence.fragIndex = Util.String2Int(node
						.GetAttr("fragindex"));
				preprocessSentence.sentenceIndex = Util.String2Int(node
						.GetAttr("sentenceindex"));
				preprocessSentence.type = node.GetAttr("type");
				preprocessSentence.termList = new ArrayList<PreprocessTerm>();
				for (XNode xnode : node.GetChildren()) {
					if (0 == xnode.GetTagName().compareTo("source")) {
						for (XNode text : xnode.GetChildren()) {
							preprocessSentence.sourceText = text.GetValue();
						}
					} else if (0 == xnode.GetTagName().compareTo("target")) {
						for (XNode text : xnode.GetChildren()) {
							preprocessSentence.targetText = text.GetValue();
						}
					} else if (0 == xnode.GetTagName().compareTo("terms")) {
						for (XNode term : xnode.GetChildren()) {
							if (term != null) {
								preprocessTerm = new PreprocessTerm();
								preprocessTerm.term_id = Util.String2Int(term
										.GetAttr("term_id"));
								preprocessTerm.term = term.GetAttr("term");
								preprocessTerm.begin = Util.String2Int(term
										.GetAttr("begin"));
								preprocessTerm.end = Util.String2Int(term
										.GetAttr("end"));
								preprocessTerm.count = Util.String2Int(term
										.GetAttr("count"));
								preprocessTerm.termDetailsList = new ArrayList<PreprocessTermDetail>();

								for (XNode details : term.GetChildren()) {
									if (details != null) {
										if (0 == details.GetTagName()
												.compareTo("details")) {

											for (XNode detail : details
													.GetChildren()) {
												if (detail != null) {
													preprocessTermDetail = new PreprocessTermDetail();
													preprocessTermDetail.term_id = preprocessTerm.term_id;
													preprocessTermDetail.translator_id = Util
															.String2Int(detail
																	.GetAttr("translator_id"));
													preprocessTermDetail.meaning = detail
															.GetAttr("meaning");
													preprocessTermDetail.remark = detail
															.GetAttr("remark");
													preprocessTermDetail.usage = detail
															.GetAttr("usage");
													preprocessTerm.termDetailsList
															.add(preprocessTermDetail);

												}
											}
										}
									}
								}
								preprocessSentence.termList.add(preprocessTerm);
							}
						}
					}
				}
				_sentecenList.add(preprocessSentence);
			}
		}
		SetMap();
	}

	public PreprocessFile(Language source, Language target,
			BiliFileNoTag bilifile, int file_id) {
		_xmlDoc = new XDocument();
		_sourceLang = source;
		_targetLang = target;
		_file_id = String.valueOf(file_id);
		_sentecenList = new ArrayList<PreprocessSentence>();
		PreprocessSentence preprocessSentence = null;
		for (Fragmentation fragmentation : bilifile._entityFrags) {

			for (Sentence sentence : fragmentation._sentences) {
				// 添加 当只有ttx标签的时候依然显示的判断
				boolean has_ttxTag = false;
				if (sentence._sourceWordCount == 0
						&& (bilifile._fileType.equals(ENTITY_FILE_TYPE.TTX) || bilifile._fileType
								.equals(ENTITY_FILE_TYPE.SDLXLIFF))
						&& sentence._source.length() > 0) {
					Pattern tagPattern = Pattern.compile("\\<.*?\\>");
					Matcher tagMatch = tagPattern.matcher(sentence._source);
					has_ttxTag = tagMatch.matches();
				}
				if (sentence._sourceWordCount > 0 || has_ttxTag) {
					preprocessSentence = new PreprocessSentence();
					preprocessSentence.fragIndex = fragmentation._fragIndex;
					preprocessSentence.sentenceIndex = sentence._entitySentenceIndex;
					preprocessSentence.wordcount = sentence._sourceWordCount;
					preprocessSentence.type = fragmentation._fragType
							.toString();
					preprocessSentence.sourceText = sentence._source;
					// TODO:为SDLXLIFF或TTX添加推荐翻译
					if (bilifile._fileType == ENTITY_FILE_TYPE.TTX) {
						TTXSentence ttxSsentence = (TTXSentence) sentence;
						preprocessSentence.targetText = ttxSsentence._recomTrans;
					} else if (bilifile._fileType == ENTITY_FILE_TYPE.SDLXLIFF) {
						SDLXliffSentence sdlXliffSentence = (SDLXliffSentence) sentence;
						preprocessSentence.targetText = sdlXliffSentence._recomTrans;
					} else {
						preprocessSentence.targetText = "";
					}
					_sentecenList.add(preprocessSentence);
				}
			}
		}

		preview = new StringBuffer();
		int previewlength = 0;
		int previewwordcount = 0;
		String sourcetext = null;
		int maxlen = 1000;
		int maxwordcount = 200;
		for (PreprocessSentence preprocesssentence : _sentecenList) {
			sourcetext = preprocesssentence.sourceText.trim();

			if (previewlength + sourcetext.length() <= maxlen
					&& previewwordcount + preprocesssentence.wordcount <= maxwordcount) {
				preview.append(sourcetext);
				previewlength = preview.length();
				previewwordcount += preprocesssentence.wordcount;

			} else {
				break;
			}

		}
		SetMap();
	}

	public int Save(String preprocessPath) {
		_xmlDoc.SetRoot(new XNode("PreprocessFile"));
		XNode xmlFile = _xmlDoc.GetRoot();

		if (xmlFile != null) {

			xmlFile.SetAttr("fileid", _file_id);
			xmlFile.SetAttr("sourcelang", _targetLang == null ? ""
					: _sourceLang.GetName().toString());
			xmlFile.SetAttr("targetlang", _targetLang == null ? ""
					: _targetLang.GetName().toString());

			// ArrayList<XNode> nodeList = xmlFile.GetChildren();
			for (PreprocessSentence sentence : _sentecenList) {
				XNode sentenceNode = new XNode("sentence");
				sentenceNode.SetAttr("fragindex", sentence.fragIndex);
				sentenceNode.SetAttr("sentenceindex", sentence.sentenceIndex);
				sentenceNode.SetAttr("type", sentence.type);
				sentenceNode.AddChild("source", sentence.sourceText);
				sentenceNode.AddChild("target", sentence.targetText);
				XNode termNodes = new XNode("terms");
				sentenceNode.AddChild(termNodes);
				if (sentence.termList != null) {
					for (PreprocessTerm preprocessTerm : sentence.termList) {
						XNode termNode = new XNode("term");
						termNode.SetAttr("term", preprocessTerm.term);
						termNode.SetAttr("term_id", preprocessTerm.term_id);
						termNode.SetAttr("begin", preprocessTerm.begin);
						termNode.SetAttr("end", preprocessTerm.end);
						termNode.SetAttr("count", preprocessTerm.count);
						termNodes.AddChild(termNode);
						XNode termDetailsNodes = new XNode("details");
						termNode.AddChild(termDetailsNodes);
						if (preprocessTerm.termDetailsList != null) {
							for (PreprocessTermDetail termdetail : preprocessTerm.termDetailsList) {
								XNode termDetailsNode = new XNode("detail");
								termDetailsNode.SetAttr("translator_id",
										termdetail.translator_id);
								termDetailsNode.SetAttr("meaning",
										termdetail.meaning);
								termDetailsNode.SetAttr("usage",
										termdetail.usage);
								termDetailsNode.SetAttr("remark",
										termdetail.remark);
								termDetailsNodes.AddChild(termDetailsNode);
							}
						}
					}
				}

				_xmlDoc.GetRoot().AddChild(sentenceNode);
			}
		}

		return _xmlDoc.Save(preprocessPath);
	}

	public ArrayList<PreprocessSentence> GetSentecenList() {
		return _sentecenList;

	}

	public HashMap<String, PreprocessSentence> GetSentecenMap() {
		return _sentecenMap;

	}

	private void SetMap() {
		if (_sentecenList != null) {
			_sentecenMap = new HashMap<String, PreprocessSentence>();
			for (PreprocessSentence preprocessSentence : _sentecenList) {
				_sentecenMap.put(preprocessSentence.type + "_"
						+ preprocessSentence.fragIndex + "_"
						+ preprocessSentence.sentenceIndex, preprocessSentence);
			}
		}
	}
}
