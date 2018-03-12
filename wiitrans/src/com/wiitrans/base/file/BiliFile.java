/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file;

import java.util.ArrayList;
import com.wiitrans.base.file.charactor.XMLCharUtil;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.VirtualFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.StringBuilderExt;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XDocument;
import com.wiitrans.base.xml.XNode;

// Word : HWPF XWPF

public class BiliFile {

	public enum ENTITY_FILE_TYPE {
		NONE, HWPF, XWPF, HSSF, XSSF, HSLF, XSLF, TXT
	};

	public enum FILTER_POSITION {
		END, EMPTY, ANY
	}

	public class Content {
		public String _content;
		public short _count;
		public short _tagcount;
		public long _hashcode;
	}

	public class ReplaceMeta {
		ReplaceMeta(Object obj, String sourceText, String targetText) {
			_obj = obj;
			_sourceText = sourceText;
			_targetText = targetText;
		}

		public Object _obj = null;
		public String _sourceText = null;
		public String _targetText = null;
	}

	protected DetectLanguage _detect;

	protected ENTITY_FILE_TYPE _fileType;

	protected SENTENCE_STATE _state;

	public Language _sourceLang;
	public Language _targetLang;

	public String _fileId;
	public String _tagId;

	protected int _filesentencecount;
	protected int _filewordcount;

	// 如果文件碎片化，虚拟代表这个人在这个文件中的虚拟碎片编号，实体代表着这个人在这个文件中的实际碎片编号
	public int _virtualFragCount;
	public int _entityFragCount;
	public int _sentenceCount;
	public int _wordCount;

	public ArrayList<VirtualFragmentation> _virtualFrags;
	public ArrayList<Fragmentation> _entityFrags;

	protected String _sourceFilePath = null;
	protected String _targetFilePath = null;
	protected String _biliFilePath = null;

	protected XMLCharUtil _xmlcharutil = null;

	protected FileUtil _fileutil = null;

	protected XDocument _xmlDoc = null;

	public BiliFile() {
		_fileType = ENTITY_FILE_TYPE.NONE;

		_state = SENTENCE_STATE.NONE;
		// _sourceLang = FileConst.FILE_LANGUAGE.NONE;
		// _targetLang = FileConst.FILE_LANGUAGE.NONE;
		_fileId = null;
		_tagId = null;
		// _virtualFragCount = FileConst.INVALID_INT32_COUNT;
		// _entityFragCount = FileConst.INVALID_INT32_COUNT;
		// _sentenceCount = FileConst.INVALID_INT32_COUNT;
		// _wordCount = FileConst.INVALID_INT32_COUNT;

		_filesentencecount = 0;
		_filewordcount = 0;

		_virtualFragCount = 0;
		_entityFragCount = 0;
		_sentenceCount = 0;
		_wordCount = 0;

		_virtualFrags = new ArrayList<VirtualFragmentation>();
		_entityFrags = new ArrayList<Fragmentation>();

		_xmlDoc = new XDocument();
	}

	protected Fragmentation NewFrag() {
		return new Fragmentation();
	}

	public int Init(String sourceFilePath, String targetFilePath,
			String biliFilePath, Language sourceLang, Language targetLang,
			SENTENCE_STATE state) {

		int ret = Const.FAIL;

		_sourceFilePath = sourceFilePath;
		_targetFilePath = targetFilePath;
		_biliFilePath = biliFilePath;
		_sourceLang = sourceLang;
		_targetLang = targetLang;
		_state = state;
		_xmlcharutil = new XMLCharUtil();
		_fileutil = new FileUtil();

		_detect = new DetectLanguage();

		Log4j.log(String.format("User specified file language is [%s]",
				_sourceLang.toString()));

		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		_fileType = null;
		_xmlDoc = null;
		_sourceFilePath = null;
		_targetFilePath = null;
		_biliFilePath = null;
		_sourceLang = null;
		_targetLang = null;
		_state = null;
		_xmlcharutil = null;
		_fileutil = null;
		_fileId = null;
		_tagId = null;
		_detect = null;
		if (_virtualFrags != null) {
			_virtualFrags.clear();
			_virtualFrags = null;
		}
		if (_entityFrags != null) {
			for (Fragmentation fragmentation : _entityFrags) {
				fragmentation.UnInit();
			}
			_entityFrags.clear();
			_entityFrags = null;
		}

		ret = Const.SUCCESS;

		return ret;
	}

	public int Parse() {
		return Const.NOT_IMPLEMENTED;
	}

	// Make the target file.
	public int Cleanup() {
		return Const.NOT_IMPLEMENTED;
	}

	public int ParseBili() {
		int ret = Const.FAIL;

		_entityFrags.clear();
		_virtualFrags.clear();

		// <File id="" type="" sourcelang="" targetlang="" sentencecount=""
		// wordcount="">
		XNode root = _xmlDoc.Parse(_biliFilePath);
		_fileId = root.GetAttr("id");
		_sentenceCount = Util.String2Int(root.GetAttr("sentencecount"));
		_wordCount = Util.String2Int(root.GetAttr("wordcount"));
		_fileType = ENTITY_FILE_TYPE.valueOf(root.GetAttr("type"));
		// _sourceLang = FILE_LANGUAGE.valueOf(root.GetAttr("sourcelang"));
		// _targetLang = FILE_LANGUAGE.valueOf(root.GetAttr("targetlang"));
		_sourceLang = _detect.Detect(root.GetAttr("sourcelang"));

		_targetLang = _detect.Detect(root.GetAttr("targetlang"));

		for (XNode node : root.GetChildren()) {
			// <EntityFrags count="">
			if (0 == node.GetTagName().compareTo("EntityFrags")) {
				ParseEntityFrags(node);
			}
		}

		EFragsToVFrags();

		return ret;
	}

	public int Save() {
		return Save(_biliFilePath);
	}

	// Save the bilingual file.
	public int Save(String billPath) {

		XNode xmlFile = _xmlDoc.GetRoot();

		if (xmlFile != null) {
			// <File id="" type="" sourcelang="" targetlang="" sentencecount=""
			// wordcount="">
			xmlFile.SetAttr("id", _fileId);
			xmlFile.SetAttr("type", _fileType.toString());
			xmlFile.SetAttr("sourcelang", _sourceLang.GetName().toString());

			xmlFile.SetAttr("targetlang", _targetLang == null ? ""
					: _targetLang.GetName().toString());

			xmlFile.SetAttr("sentencecount", _sentenceCount);
			xmlFile.SetAttr("wordcount", _wordCount);

			ArrayList<XNode> nodeList = xmlFile.GetChildren();
			for (XNode node : nodeList) {
				// <EntityFrags count="">
				if (0 == node.GetTagName().compareTo("EntityFrags")) {
					SaveEntityFrags(node);
				}
			}
		}

		return _xmlDoc.Save(billPath);
	}

	private int ParseEntityFrags(XNode node) {
		int ret = Const.FAIL;

		// <EntityFrags count="">
		int fragCount = Util.String2Int(node.GetAttr("count"));

		int realFragCount = 0;
		for (XNode frag : node.GetChildren()) {
			Fragmentation obj = NewFrag();
			obj.SetNode(frag);
			if (obj.Parse(_fileutil) == Const.SUCCESS) {
				_entityFrags.add(obj);

				realFragCount++;
			}
		}

		if (fragCount != realFragCount) {
			Log4j.error(String.format("The fragcount is %d, but only %d.",
					fragCount, realFragCount));
		}

		_entityFragCount = realFragCount;

		return ret;
	}

	private int SaveEntityFrags(XNode node) {
		int ret = Const.FAIL;

		// <EntityFrags count="">
		node.SetAttr("count", _entityFragCount);

		for (Fragmentation frag : _entityFrags) {
			frag.Save(_fileutil);
		}

		return ret;
	}

	// Adjust the _entityFrag into _virtualFrag.
	public int EFragsToVFrags() {
		int ret = Const.FAIL;

		int sentenceIndex = 0;
		VirtualFragmentation vFrag = new VirtualFragmentation();
		vFrag._fragIndex = _virtualFragCount;
		for (Fragmentation frag : _entityFrags) {

			for (Sentence sent : frag._sentences) {
				// 句子字数必须大于0，才可以转换成虚拟碎片的句子
				if (sent != null && sent._sourceWordCount > 0) {
					sent._virtualFragIndex = vFrag._fragIndex;
					sent._virtualSentenceIndex = sentenceIndex;

					vFrag._sentences.add(sent);
					vFrag._sentenceCount++;
					vFrag._sentencesMap.put(String.valueOf(sentenceIndex++),
							sent);

					if (sentenceIndex >= Const.FRAG_SIZE) {

						// 保存当前VFrag
						_virtualFrags.add(vFrag);
						_virtualFragCount++;

						// 新生成一个VFrag
						sentenceIndex = 0;
						vFrag = new VirtualFragmentation();
						vFrag._fragIndex = _virtualFragCount;

					}
				}
			}

		}

		if (sentenceIndex > 0) {
			_virtualFrags.add(vFrag);
			_virtualFragCount++;
		}

		return ret;
	}

	protected String FilterText(String txt, String sep, FILTER_POSITION pos) {
		String fTxt = null;

		if ((txt != null) && (!txt.isEmpty()) && (sep != null)
				&& (!sep.isEmpty())) {
			switch (pos) {
			// Remove the last character.
			case END: {
				if (txt.charAt(txt.length() - 1) == sep.charAt(0)) {
					String subTxt = txt.substring(0, txt.length() - 1);
					if (!subTxt.isEmpty()) {
						fTxt = subTxt;
					}
				} else {
					fTxt = txt;
				}
				break;
			}
			case EMPTY: {
				String subTxt = txt;
				for (int i = 0; i < sep.length(); i++) {
					subTxt = subTxt.replace(String.valueOf(sep.charAt(i)), "");
				}

				if (subTxt.length() > 0) {
					fTxt = subTxt;
				}

				break;
			}
			// Replace the string in file.
			case ANY: {

				break;
			}
			default:
				break;
			}
		}

		return fTxt;
	}

	protected ArrayList<Fragmentation> GetFragsWithType(FRAG_TYPE type) {
		ArrayList<Fragmentation> frags = null;

		if (_entityFragCount > 0) {
			frags = new ArrayList<Fragmentation>();
		}

		for (Fragmentation frag : _entityFrags) {
			if (frag._fragType == type) {
				frags.add(frag);
			}
		}

		return frags;
	}

	protected ArrayList<String> getFragContent(Fragmentation frag,
			int sentOffset, int sentCount) {
		ArrayList<String> fragText = null;

		// SENTENCE_STATE state = frag._state;
		StringBuilderExt source = new StringBuilderExt();
		StringBuilderExt target = new StringBuilderExt();

		int count = sentOffset;
		for (int index = sentOffset; index < frag._sentences.size(); ++index) {
			Sentence sent = frag._sentences.get(index);
			if (count == (sentOffset + sentCount)) {
				break;
			}
			count++;
			String ssource = _fileutil.UnTagPair(sent._source);
			String stranslate = _fileutil.UnTagPair(sent._translate);
			String sedit = _fileutil.UnTagPair(sent._edit);
			source.append(ssource);
			if (SENTENCE_STATE.NONE == SENTENCE_STATE.E) {
				if (sedit != null && !sedit.isEmpty()) {
					target.append(sedit);
				} else if (stranslate != null && !stranslate.isEmpty()) {
					target.append(stranslate);
				} else {
					target.append(ssource);
				}
			} else if (SENTENCE_STATE.T == SENTENCE_STATE.E) {
				// 1.T
				if ((stranslate == null) || stranslate.isEmpty()) {
					target.append(ssource);
				} else {
					target.append(stranslate);
				}

			} else if (SENTENCE_STATE.E == SENTENCE_STATE.E) {
				// 2.E
				if (sent._eScore > 0 && (sedit.isEmpty())) {
					if ((stranslate == null) || stranslate.isEmpty()) {
						target.append(ssource);
					} else {
						target.append(stranslate);
					}
				} else {
					target.append(sedit);
				}
			} else if (SENTENCE_STATE.TQA == SENTENCE_STATE.E) {
				// 3.T+Q
				if ((sent._qScore > 0) && (sent._qa.isEmpty())) {
					target.append(stranslate);
				} else {
					target.append(sent._qa);
				}
			} else if (SENTENCE_STATE.EQA == SENTENCE_STATE.E) {
				// 4.T+E+Q
				if ((sent._qScore > 0) && (sent._qa.isEmpty())) {
					target.append(sedit);
				} else {
					target.append(sent._qa);
				}
			} else {
				Log4j.error("Sentence state is none.");
			}
		}

		if (source.length() > 0) {
			fragText = new ArrayList<String>();
			fragText.add(source.toString());
			fragText.add(target.toString());
		} else {
			Log4j.error("source.length() <= 0");
		}

		return fragText;
	}

	protected ArrayList<ReplaceMeta> ContentToMeta(String runsSource,
			String runsTarget) {
		ArrayList<ReplaceMeta> run = null;

		String[] source = runsSource.split(_tagId);
		String[] target = runsTarget.split(_tagId);
		if (source.length > 0) {
			if (source.length == target.length) {
				run = new ArrayList<ReplaceMeta>();
				for (int index = 0; index < source.length; ++index) {
					run.add(new ReplaceMeta(null, source[index], target[index]));
				}
			} else {
				run = new ArrayList<ReplaceMeta>();
				for (int index = 0; index < source.length; ++index) {
					run.add(new ReplaceMeta(null, source[index], ""));
				}
			}
		}

		return run;
	}

	public int GetEditScore() {
		int score = 0;

		for (Fragmentation frag : _entityFrags) {
			for (Sentence sentence : frag._sentences) {
				if (sentence._eScore > 0) {
					score += sentence._eScore;
				}
			}
		}

		return score;
	}

	public String GetBiliFilePath() {
		return _biliFilePath;
	}

	public String GetTargetFilePath() {
		return _targetFilePath;
	}

	public short GetTagCount(String text) {
		return (short) _fileutil.GetTagCount(text, _tagId);
	}

	public int GetTranlateCount() {
		int count = 0;
		for (VirtualFragmentation frag : _virtualFrags) {
			for (Sentence sentence : frag._sentences) {
				if (sentence._sourceWordCount > 0) {
					if (sentence._translate != null
							&& sentence._translate.trim().length() > 0) {
						count += sentence._sourceWordCount;
					}
				}
			}
		}
		return count;
	}

	public int GetEditCount() {
		int count = 0;
		for (VirtualFragmentation frag : _virtualFrags) {
			for (Sentence sentence : frag._sentences) {
				if (sentence._sourceWordCount > 0) {
					if (sentence._eScore >= 0) {
						count += sentence._sourceWordCount;
					}
				}
			}
		}
		return count;
	}

	// public String GetExt() {
	// // NONE, HWPF, XWPF, HSSF, XSSF, HSLF, XSLF
	// if (_fileType == ENTITY_FILE_TYPE.HWPF) {
	// return "doc";
	// } else if (_fileType == ENTITY_FILE_TYPE.XWPF) {
	// return "docx";
	// } else if (_fileType == ENTITY_FILE_TYPE.HSSF) {
	// return "xls";
	// } else if (_fileType == ENTITY_FILE_TYPE.XSSF) {
	// return "xlsx";
	// } else if (_fileType == ENTITY_FILE_TYPE.HSLF) {
	// return "ppt";
	// } else if (_fileType == ENTITY_FILE_TYPE.XSLF) {
	// return "pptx";
	// } else {
	// return "";
	// }
	// }
}
