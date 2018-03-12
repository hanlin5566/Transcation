package com.wiitrans.base.file.notag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.charactor.XMLCharUtil;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.VirtualFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.sentence.SDLXliffSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.StringBuilderExt;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XDocument;
import com.wiitrans.base.xml.XNode;

public class BiliFileNoTag {
	public enum ENTITY_FILE_TYPE {
		NONE, HWPF, XWPF, HSSF, XSSF, HSLF, XSLF, TXT, TTX, SDLXLIFF
	};

	// public enum FILTER_POSITION {
	// END, EMPTY, ANY
	// }

	public class Content {
		public boolean _valid;
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

	public ENTITY_FILE_TYPE _fileType;

	// public SENTENCE_STATE _state;

	public Language _sourceLang;
	public Language _targetLang;

	public int _node_id;
	public String _fileId;

	public int _filesentencecount;
	public int _filewordcount;

	// 如果文件碎片化，虚拟代表这个人在这个文件中的虚拟碎片编号，实体代表着这个人在这个文件中的实际碎片编号
	public int _virtualFragCount;
	public int _entityFragCount;
	public int _sentenceCount;
	public int _wordCount;
	private BiliFileDetails _bilifiledetails;

	// 实体Frag，文件实际的frag，根据具体文件分frag
	public ArrayList<Fragmentation> _entityFrags;
	// 虚拟frag，文件虚拟的frag，根据每页n条句子分frag
	public ArrayList<VirtualFragmentation> _virtualFrags;
	// 未做（翻译或校对）的句子，暂时只是未翻译的句子
	// public ArrayList<Sentence> _notDoneTSentences;
	// public ArrayList<Sentence> _notDoneESentences;

	public String _sourceFilePath = null;
	public String _targetFilePath = null;
	public String _biliFilePath = null;
	public String _originalFileName = null;

	public XMLCharUtil _xmlcharutil = null;

	protected FileUtil _fileutil = null;

	protected XDocument _xmlDoc = null;

	public boolean[] _openstate = null;

	public BiliFileNoTag() {
		_fileType = ENTITY_FILE_TYPE.NONE;

		// _state = SENTENCE_STATE.NONE;
		_fileId = null;

		_filesentencecount = 0;
		_filewordcount = 0;

		_virtualFragCount = 0;
		_entityFragCount = 0;
		_sentenceCount = 0;
		_wordCount = 0;
		_entityFrags = new ArrayList<Fragmentation>();
		_virtualFrags = new ArrayList<VirtualFragmentation>();
		// _notDoneTSentences = new ArrayList<Sentence>();
		// _notDoneESentences = new ArrayList<Sentence>();

		_xmlDoc = new XDocument();
		// 0T，1E，2后台，3SaveAsTmx,4质量报告
		_openstate = new boolean[] { false, false, false, false, false, false,
				false, false, false, false };
	}

	protected Fragmentation NewFrag() {
		return new Fragmentation();
	}

	public int Init(String sourceFilePath, String targetFilePath,
			String biliFilePath, String originalFileName, Language sourceLang,
			Language targetLang) {

		int ret = Const.FAIL;

		_sourceFilePath = sourceFilePath;
		_targetFilePath = targetFilePath;
		_biliFilePath = biliFilePath;
		_originalFileName = originalFileName;
		_sourceLang = sourceLang;
		_targetLang = targetLang;
		// _state = state;
		_xmlcharutil = new XMLCharUtil();
		_fileutil = new FileUtil();
		// _bilifiledetails = new BiliFileDetails();
		_detect = new DetectLanguage();

		// Log4j.log(String.format("User specified file language is [%s]",
		// _sourceLang.toString()));

		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		_fileType = null;
		_xmlDoc = null;
		_openstate = null;
		_sourceFilePath = null;
		_targetFilePath = null;
		_biliFilePath = null;
		_sourceLang = null;
		_targetLang = null;
		// _state = null;
		_xmlcharutil = null;
		_fileutil = null;
		_fileId = null;
		_bilifiledetails = null;
		// _tagId = null;
		_detect = null;
		if (_virtualFrags != null) {
			for (VirtualFragmentation virfragmentation : _virtualFrags) {
				virfragmentation.UnInit();
			}
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

		// if (_notDoneTSentences != null) {
		// _notDoneTSentences.clear();
		// _notDoneTSentences = null;
		// }
		//
		// if (_notDoneESentences != null) {
		// _notDoneESentences.clear();
		// _notDoneESentences = null;
		// }

		ret = Const.SUCCESS;

		return ret;
	}

	public int Parse() {
		return Const.NOT_IMPLEMENTED;
	}

	// Make the target file.
	public int Cleanup(SENTENCE_STATE state) {
		return Const.NOT_IMPLEMENTED;
	}

	public int ParseBili() {
		int ret = Const.FAIL;

		_entityFrags.clear();
		_virtualFrags.clear();

		XNode root = _xmlDoc.Parse(_biliFilePath);
		_fileId = root.GetAttr("id");
		_sentenceCount = Util.String2Int(root.GetAttr("sentencecount"));
		_wordCount = Util.String2Int(root.GetAttr("wordcount"));
		_fileType = ENTITY_FILE_TYPE.valueOf(root.GetAttr("type"));
		_sourceLang = _detect.Detect(root.GetAttr("sourcelang"));

		_targetLang = _detect.Detect(root.GetAttr("targetlang"));

		for (XNode node : root.GetChildren()) {
			if (0 == node.GetTagName().compareTo("EntityFrags")) {
				ParseEntityFrags(node);
			}
		}

		EFragsToVFrags();
		EFragsToNotDoneAndDetails();

		return ret;
	}

	public int Save() {
		return Save(_biliFilePath);
	}

	// Save the bilingual file.
	public int Save(String billPath) {

		XNode xmlFile = _xmlDoc.GetRoot();

		if (xmlFile != null) {
			xmlFile.SetAttr("id", _fileId);
			xmlFile.SetAttr("type", _fileType.toString());
			xmlFile.SetAttr("sourcelang", _sourceLang.GetName().toString());

			xmlFile.SetAttr("targetlang", _targetLang == null ? ""
					: _targetLang.GetName().toString());

			xmlFile.SetAttr("sentencecount", _sentenceCount);
			xmlFile.SetAttr("wordcount", _wordCount);

			ArrayList<XNode> nodeList = xmlFile.GetChildren();
			for (XNode node : nodeList) {
				if (0 == node.GetTagName().compareTo("EntityFrags")) {
					SaveEntityFrags(node);
				}
			}
		}

		return _xmlDoc.Save(billPath);
	}

	public int ParseEntityFrags(XNode node) {
		int ret = Const.FAIL;

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

	public int SaveEntityFrags(XNode node) {
		int ret = Const.FAIL;

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
			// vFrag._fragType = frag._fragType;
			for (Sentence sent : frag._sentences) {
				// 句子字数必须大于0，才可以转换成虚拟碎片的句子
				if (this.sentencesVisable(sent)) {
					sent._virtualFragIndex = vFrag._fragIndex;
					sent._virtualSentenceIndex = sentenceIndex;
					sent._entityFragType = frag._fragType;
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

	public int EFragsToNotDoneAndDetails() {
		int ret = Const.FAIL;

		_bilifiledetails = new BiliFileDetails();
		// _notDoneTSentences.clear();
		// _notDoneESentences.clear();

		for (Fragmentation frag : _entityFrags) {
			for (Sentence sentence : frag._sentences) {
				// 句子字数必须大于0，才可以转换成虚拟碎片的句子
				if (this.sentencesVisable(sentence)) {

					// 添加如果未sdl句子，判断是否锁定，如果锁定不加字数
					if (sentence instanceof SDLXliffSentence
							&& ((SDLXliffSentence) sentence)._lock) {
						// 句子数增加保持翻页序号。
						_bilifiledetails.sentenceCount += 1;
						_bilifiledetails.tranlateSentenceCount += 1;
						_bilifiledetails.editSentenceCount += 1;
						continue;
					}
					_bilifiledetails.sentenceCount += 1;
					_bilifiledetails.wordCount += sentence._sourceWordCount;
					if (sentence._translatestatus) {
						_bilifiledetails.tranlateSentenceCount += 1;
						_bilifiledetails.tranlateWordCount += sentence._sourceWordCount;
					}
					// else {
					// _notDoneTSentences.add(sentence);
					// }
					if (sentence._editstatus) {
						_bilifiledetails.editSentenceCount += 1;
						_bilifiledetails.editWordCount += sentence._sourceWordCount;
					}
					// else {
					// _notDoneESentences.add(sentence);
					// }
				}
			}
		}
		return ret;
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
			int sentOffset, int sentCount, SENTENCE_STATE state) {
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

			if (sent._valid) {
				source.append(ssource);
				if (SENTENCE_STATE.NONE == state) {
					if (sedit != null && !sedit.isEmpty()) {
						target.append(sedit);
					} else if (stranslate != null && !stranslate.isEmpty()) {
						target.append(stranslate);
					}
					// 添加当未xliff时，并且为锁定字段则即使target未空也不替换为原文
					else if (sent instanceof SDLXliffSentence
							&& ((SDLXliffSentence) sent)._lock) {
						target.append(sedit == null ? "" : sedit);
					} else {
						target.append(ssource);
					}
				} else if (SENTENCE_STATE.T == state) {
					// 1.T

					if (stranslate != null && !stranslate.isEmpty()) {
						target.append(stranslate);
					}
					// 添加当未xliff时，并且为锁定字段则即使target未空也不替换为原文
					else if (sent instanceof SDLXliffSentence
							&& ((SDLXliffSentence) sent)._lock) {
						target.append(stranslate == null ? "" : stranslate);
					} else {
						target.append(ssource);
					}

				} else if (SENTENCE_STATE.E == state) {
					// 2.E
					if (sent._eScore <= 0 && (sedit == null || sedit.isEmpty())) {
						if ((stranslate == null) || stranslate.isEmpty()) {
							// 添加当未xliff时，并且为锁定字段则即使target未空也不替换为原文
							if (sent instanceof SDLXliffSentence
									&& ((SDLXliffSentence) sent)._lock) {
								target.append(stranslate == null ? ""
										: stranslate);
							} else {
								target.append(ssource);
							}
						} else {
							target.append(stranslate);
						}
					} else {
						target.append(sedit);
					}
				} else if (SENTENCE_STATE.TQA == state) {
					// 3.T+Q
					if ((sent._qScore <= 0)
							&& (sent._qa == null || sent._qa.isEmpty())) {
						// 添加当未xliff时，并且为锁定字段则即使target未空也不替换为原文
						if (sent instanceof SDLXliffSentence
								&& ((SDLXliffSentence) sent)._lock) {
							target.append(stranslate == null ? "" : stranslate);
						} else {
							target.append(ssource);
						}
					} else {
						target.append(sent._qa);
					}
				} else if (SENTENCE_STATE.EQA == state) {
					// 4.T+E+Q
					if ((sent._qScore <= 0)
							&& (sent._qa == null || sent._qa.isEmpty())) {
						// 添加当未xliff时，并且为锁定字段则即使target未空也不替换为原文
						if (sent instanceof SDLXliffSentence
								&& ((SDLXliffSentence) sent)._lock) {
							target.append(sedit == null ? "" : sedit);
						} else {
							target.append(sedit);
						}
					} else {
						target.append(sent._qa);
					}
				} else {
					Log4j.error("Sentence state is none.");
				}
			} else {
				source.append(_sourceLang.Decode(ssource));
				target.append(_sourceLang.Decode(ssource));
			}
		}

		if (source.length() > 0) {
			fragText = new ArrayList<String>();
			fragText.add(source.toString());
			fragText.add(target.toString());
		} else {
			// Log4j.error("source.length() <= 0");
		}

		return fragText;
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

	public BiliFileDetails GetBiliFileDetails() {
		return _bilifiledetails;
	}

	// 句子是否在在线编辑器显示
	private boolean sentencesVisable(Sentence sentence) {
		boolean ret = false;
		// 添加 当只有ttx标签的时候依然显示的判断
		boolean has_ttxTag = false;
		if (sentence != null
				&& sentence._sourceWordCount == 0
				&& (this._fileType.equals(ENTITY_FILE_TYPE.TTX) || this._fileType
						.equals(ENTITY_FILE_TYPE.SDLXLIFF))
				&& sentence._source.length() > 0) {
			Pattern tagPattern = Pattern.compile("\\<.*?\\>");
			Matcher tagMatch = tagPattern.matcher(sentence._source);
			has_ttxTag = tagMatch.matches();
		}
		ret = (sentence != null && sentence._sourceWordCount > 0) || has_ttxTag;
		return ret;
	}

	public HashMap<Long, ArrayList<int[]>> GetHashCodeMap() {
		HashMap<Long, ArrayList<int[]>> hashcodeMap = new HashMap<Long, ArrayList<int[]>>();
		//HashSet<Long> hash = new HashSet<Long>();
		ArrayList<int[]> list;
		int[] index;
		for (VirtualFragmentation frag : this._virtualFrags) {
			for (Sentence sent : frag._sentences) {
				if (sentencesVisable(sent)) {
//					if (sent._translate != null && sent._translate.length() > 0) {
//						if (!hash.contains(sent._hashcode)) {
//							hash.add(sent._hashcode);
//						}
//					}

					if (hashcodeMap.containsKey(sent._hashcode)) {
						list = hashcodeMap.get(sent._hashcode);
					} else {
						list = new ArrayList<int[]>();
					}
					index = new int[] { sent._virtualFragIndex,
							sent._virtualSentenceIndex };
					list.add(index);
					hashcodeMap.put(sent._hashcode, list);
				}
			}
		}
//		Iterator<Long> iterator = hash.iterator();
//		while (iterator.hasNext()) {
//			Long s = iterator.next();
//			if (hashcodeMap.containsKey(s)) {
//				hashcodeMap.remove(s);
//			}
//		}

		return hashcodeMap;
	}
}
