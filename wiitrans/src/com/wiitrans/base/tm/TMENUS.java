package com.wiitrans.base.tm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.db.model.TMServiceIndexBean;
import com.wiitrans.base.db.model.TMServiceTimesBean;
import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.hbase.HbaseRow;
import com.wiitrans.base.hbase.HbaseTMTUDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;

public class TMENUS extends TMLanguage {

	private LANGUAGE_NAME _name = LANGUAGE_NAME.ENGLISH;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.ENUS;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_ENUS;
	private long _bit60 = 1152921504606846976L;

	private TmxFile _tmFile;

	private TmxFile getTmxFile() {
		return _tmFile;
	}

	private void setTmxFile(TmxFile tmFile) {
		this._tmFile = tmFile;
	}

	private TmxFileChunk _tmFileChunk;

	private TmxFileChunk getTmxFileChunk() {
		return _tmFileChunk;
	}

	private void setTmxFileChunk(TmxFileChunk tmFileChunk) {
		this._tmFileChunk = tmFileChunk;
	}

	private boolean _isSource;

	private boolean getIsSource() {
		return _isSource;
	}

	private void setIsSource(boolean isSource) {
		this._isSource = isSource;
	}

	private int _wordLen = 30;

	private int getWordLen() {
		return _wordLen;
	}

	private void setWordLen(int wordLen) {
		this._wordLen = wordLen;
	}

	// private int _wordTimes = 30000;
	private int _wordMaxTimes = 200000;

	private int getWordMaxTimes() {
		return _wordMaxTimes;
	}

	private void setWordMaxTimes(int wordMaxTimes) {
		this._wordMaxTimes = wordMaxTimes;
	}

	private int _tuTimes = 10;

	private int getTuTimes() {
		return _tuTimes;
	}

	private void setTuTimes(int tuTimes) {
		this._tuTimes = tuTimes;
	}

	private int _tuMaxTimes = 100;

	private int getTuMaxTimes() {
		return _tuMaxTimes;
	}

	private void setTuMaxTimes(int tuMaxTimes) {
		this._tuMaxTimes = tuMaxTimes;
	}

	@Override
	public LANGUAGE_NAME GetLanguageName() {
		return _name;
	}

	@Override
	public LANGUAGE_TYPE GetLanguageType() {
		return _type;
	}

	@Override
	public String GetLanguageCountryName() {
		return _slangcountry;
	}

	@Override
	public LANGUAGE_COUNTRY GetLanguageCountry() {
		return _langcountry;
	}

	@Override
	public int IsLetter(char c) {
		// 1小写2大写3数字0非字母
		int result;
		if (c >= 'a' && c <= 'z') {
			result = 1;
		} else if (c >= 'A' && c <= 'Z') {
			result = 2;
		} else if (c >= '0' && c <= '9') {
			result = 3;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public String GetWordByHansonCode(long wordID) {
		String word = "";

		ArrayList<Short> letters = new ArrayList<Short>();

		if (wordID > 0L && wordID < _bit60) {
			letters.add((short) ((wordID & 1134907106097364992L) / 18014398509481984L));
			letters.add((short) ((wordID & 17732923532771328L) / 281474976710656L));
			letters.add((short) ((wordID & 277076930199552L) / 4398046511104L));
			letters.add((short) ((wordID & 4329327034368L) / 68719476736L));
			letters.add((short) ((wordID & 67645734912L) / 1073741824L));
			letters.add((short) ((wordID & 1056964608L) / 16777216L));
			letters.add((short) ((wordID & 16515072L) / 262144L));
			letters.add((short) ((wordID & 258048L) / 4096L));
			letters.add((short) ((wordID & 4032L) / 64L));
			letters.add((short) (wordID & 63L));

			for (short letter : letters) {
				if (letter >= 0 && letter <= 9) {
					word += (char) (letter + '0');
				} else if (letter >= 10 && letter <= 35) {
					word += (char) (letter + 'a' - 10);
				} else {
					word += " ";
				}
			}
		}

		return word;
	}

	@Override
	public long GetHansonCodeByWord(String word) {
		long result = 0;
		if (word.length() <= 10) {
			// word = word.PadRight(10, '-');
			word = word.concat("----------").substring(0, 10);

			char[] cs = word.toCharArray();

			long[] letters = new long[cs.length];

			for (int i = 0; i < cs.length; i++) {
				if (cs[i] >= 'a' && cs[i] <= 'z') {
					letters[i] = cs[i] - 'a' + 10;
				} else if (cs[i] >= 'A' && cs[i] <= 'Z') {
					letters[i] = cs[i] - 'A' + 10;
				} else if (cs[i] >= '0' && cs[i] <= '9') {
					letters[i] = cs[i] - '0';
				} else {
					letters[i] = 63;
				}
			}

			result += letters[9] * 1L;
			result += letters[8] * 64L;
			result += letters[7] * 4096L;
			result += letters[6] * 262144L;
			result += letters[5] * 16777216L;
			result += letters[4] * 1073741824L;
			result += letters[3] * 68719476736L;
			result += letters[2] * 4398046511104L;
			result += letters[1] * 281474976710656L;
			result += letters[0] * 18014398509481984L;
		} else {
			// long bit60 = 1152921504606846976L;

			char[] cs = word.toLowerCase().toCharArray();

			// unchecked
			// {
			for (char item : cs) {
				result = result * 31 + item;
			}
			// }

			result = result % _bit60;

			if (result < 0) {
				result = result + _bit60 + _bit60;
			} else {
				result = result + _bit60;
			}
		}

		return result;
	}

	@Override
	public ArrayList<TMWord> AnalyseWord(String sentence) {
		ArrayList<TMWord> list = new ArrayList<TMWord>();

		boolean isAbled = true;

		TMWord tmword;

		if (sentence != null && sentence.trim().length() > 0) {
			char[] chars = sentence.toCharArray();

			int length = sentence.length(), i = 0, j = 0;
			String word;
			char letter = '\0';// , preLetter;
			int property = 0, preProperty;// 字母的性质
			int maxLen = this.getWordLen();

			while (j < length) {
				word = null;
				// preLetter = letter;
				letter = chars[j];
				preProperty = property;

				// 当前字符是否字母
				property = this.IsLetter(letter);

				if (property == 0 && preProperty == 0) {
					// 第j个字符和第j-1个字符都不是字母
					++j;

					isAbled = true;
				} else if (property > 0 && preProperty == 0) {
					// 单词开始
					i = j;
					isAbled = true;
				} else if (property == 0 && preProperty > 0) {
					// 单词结束
					// word = sentence.substring(i, j - i);
					word = sentence.substring(i, j);

					tmword = validateWord(word, maxLen);
					if (tmword != null && tmword.isWord) {
						tmword.isAbled = isAbled;
						tmword.sequence = list.size() + 1;
						tmword.beginAt = i + 1;
						list.add(tmword);
						// list.add(new TMWord(word, isAbled, list.size() + 1, i
						// + 1));
					}
					++j;
					i = j;
				} else if (property == 2 && preProperty == 1) {
					// 第j-1个字符为单词结束，第j个字符为单词开始，从小写变成大写
					// word = sentence.substring(i, j - i);
					word = sentence.substring(i, j);

					tmword = validateWord(word, maxLen);
					if (tmword != null && tmword.isWord) {
						tmword.isAbled = isAbled;
						tmword.sequence = list.size() + 1;
						tmword.beginAt = i + 1;
						list.add(tmword);
						// list.add(new TMWord(word, isAbled, list.size() + 1, i
						// + 1));
						// list.add(new TMWord(word, isAbled, list.Count + 1, i
						// + 1));

						// 另一个单词开始了
						isAbled = true;
					}
					i = j;
				} else if (property == 3 || preProperty == 3) {
					// 第j个字符和第j-1个字符都是数字

					++j;
					isAbled = false;
				} else {
					// 第j个字符和第j-1个字符都是字母
					++j;
				}

			}

			if (property > 0) {
				// word = sentence.substring(i, j - i);
				word = sentence.substring(i, j);
				tmword = validateWord(word, maxLen);
				if (tmword != null && tmword.isWord) {
					tmword.isAbled = isAbled;
					tmword.sequence = list.size() + 1;
					tmword.beginAt = i + 1;
					list.add(tmword);
				}
			}
		}

		return list;
	}

	private TMWord validateWord(String word, int maxLen) {

		// 此方法只能从单词开始的位子操作
		if (word == null || word.length() == 0) {
			TMWord tmword = new TMWord();
			tmword.isWord = false;
			return tmword;
		}

		word = word.toLowerCase();
		TMWord tmword = new TMWord();
		tmword.word = word;
		// tmword.isAbled = true;
		tmword.isWord = true;

		// 去掉所有单词前面的横线
		// word = word.TrimStart('-');
		word = word.replaceFirst("^-*", "");

		if (word.length() == 0) {
			// 只有-的不算单词
			tmword.isWord = false;
			return tmword;
		} else {
			char firstLitter = word.charAt(0);

			if (firstLitter >= '0' && firstLitter <= '9') {
				// 如果首字母是数字，不算单词
				tmword.isWord = false;
				return tmword;
			} else if (word.toLowerCase()
					.replace(String.valueOf(firstLitter).toLowerCase(), "")
					.length() == 0) {
				// 如果所有字母全部一样，不算单词
				// 其中仅有一个字母,也不算单词
				tmword.isWord = false;
				return tmword;

			} else if (word.length() > maxLen) {
				// 如果大于最大长度，不算单词
				tmword.isWord = false;
				return tmword;
			}
		}
		return tmword;
	}

	@Override
	public int Init(TmxFile tmFile, boolean isSource) {
		int ret = Const.FAIL;
		this.setTmxFile(tmFile);
		// _isSource = isSource;
		this.setIsSource(isSource);

		// 暂时不运行
		/*
		 * if (false && _wordVariant == null) {
		 * 
		 * TMDAO dao = null; DictLangDAO langdao = null; try {
		 * 
		 * langdao = new DictLangDAO(); langdao.Init(true); DictLangCountryBean
		 * bean = langdao .SelectLangCountry(_slangcountry); if (bean != null) {
		 * dao = new TMDAO(); dao.Init(true); List<TMVariantBean> list =
		 * dao.SelectVariant(bean.lang_id); if (list != null && list.size() > 0)
		 * { _wordVariant = new HashMap<String, String[]>(); for (TMVariantBean
		 * tmVariantBean : list) { if
		 * (!_wordVariant.containsKey(tmVariantBean.word)) {
		 * _wordVariant.put(tmVariantBean.word,
		 * tmVariantBean.variant.split(",")); } } } }
		 * 
		 * } catch (Exception e) { Log4j.error(e); } finally { if (langdao !=
		 * null) { langdao.UnInit(); } if (dao != null) { dao.UnInit(); } } }
		 */
		return ret;
	}

	@Override
	public int UnInit() {
		int ret = Const.FAIL;
		// _tmFile = null;
		this.setTmxFile(null);
		if (this.getWordLonger() != null) {
			this.getWordLonger().clear();
		}
		if (this.getWordInvalid() != null) {
			this.getWordInvalid().clear();
		}
		// if (_wordLonger != null) {
		// _wordVariant.clear();
		// }
		if (this.getWordTime() != null) {
			this.getWordTime().clear();
		}
		if (this.getTuIndex() != null) {
			this.getTuIndex().clear();
		}
		if (this.getWordIndex() != null) {
			this.getWordIndex().clear();
		}
		return ret;
	}

	private HashMap<String, Long> _wordLonger;

	private HashMap<String, Long> getWordLonger() {
		return _wordLonger;
	}

	private void setWordLonger(HashMap<String, Long> wordLonger) {
		this._wordLonger = wordLonger;
	}

	private ArrayList<TMWord> _wordInvalid;

	private ArrayList<TMWord> getWordInvalid() {
		return _wordInvalid;
	}

	private void setWordInvalid(ArrayList<TMWord> wordInvalid) {
		this._wordInvalid = wordInvalid;
	}

	// private static HashMap<String, String[]> _wordVariant;
	private HashMap<Long, Integer> _wordTime;

	private HashMap<Long, Integer> getWordTime() {
		return _wordTime;
	}

	private void setWordTime(HashMap<Long, Integer> wordTime) {
		this._wordTime = wordTime;
	}

	private HashMap<Integer, long[]> _tuIndex;

	private HashMap<Integer, long[]> getTuIndex() {
		return _tuIndex;
	}

	private void setTuIndex(HashMap<Integer, long[]> tuIndex) {
		this._tuIndex = tuIndex;
	}

	private HashMap<Long, int[]> _wordIndex;

	private HashMap<Long, int[]> getWordIndex() {
		return _wordIndex;
	}

	private void setWordIndex(HashMap<Long, int[]> wordIndex) {
		this._wordIndex = wordIndex;
	}

	@Override
	public int Parse(int tmid) {
		int ret = Const.FAIL;

		// tmx文件所有数据分解为tm相关格式
		this.setWordLonger(new HashMap<String, Long>());

		// word的tuID列表，存储在_wordIndex中，
		this.setWordIndex(new HashMap<Long, int[]>());
		// tu的wordID列表，存储在_wordIndex中，
		this.setTuIndex(new HashMap<Integer, long[]>());
		// 不可用的单词，有数字的
		this.setWordInvalid(new ArrayList<TMWord>());

		this.setWordTime(new HashMap<Long, Integer>());

		String word;
		long wordID;
		int tuID;

		// parse方法中的word所在tuID列表,在方法最后转成_wordIndex
		HashMap<Long, HashSet<Integer>> wordTUList = new HashMap<Long, HashSet<Integer>>();
		// parse方法中的tu所在wordID列表,在方法最后转成_tuIndex
		HashMap<Integer, ArrayList<Long>> tuWordList = new HashMap<Integer, ArrayList<Long>>();

		ArrayList<TMWord> wordList;
		HashSet<Integer> tuIDListForWordID;
		ArrayList<Long> wordIDListForTUID;
		for (TMTU tu : this.getTmxFile()._tmtuList) {
			tuID = tu._tuid;
			wordList = this.AnalyseWord(tu._tuv1);

			if (wordList != null && wordList.size() > 0) {
				wordIDListForTUID = new ArrayList<Long>();

				for (TMWord tmWord : wordList) {
					wordID = 0;

					if (tmWord.isWord) {
						word = tmWord.word;
						if (tmWord.isAbled) {
							wordID = this.GetHansonCodeByWord(word);
							if (word.length() > 10) {
								if (!this.getWordLonger().containsKey(word)) {
									this.getWordLonger().put(word, wordID);
								}
							}

							if (wordTUList.containsKey(wordID)) {
								tuIDListForWordID = wordTUList.get(wordID);
							} else {
								tuIDListForWordID = new HashSet<Integer>();
								wordTUList.put(wordID, tuIDListForWordID);
							}

							if (!tuIDListForWordID.contains(tuID)) {
								tuIDListForWordID.add(tuID);
							}
						} else {
							this.getWordInvalid().add(tmWord);
						}
						wordIDListForTUID.add(wordID);
					}
				}

				if (wordIDListForTUID.size() > 0) {
					tuWordList.put(tuID, wordIDListForTUID);
				}
			}
		}

		Set<Long> wordIDset = wordTUList.keySet();
		HashSet<Integer> tuIDMAP;
		int[] tuIDary;
		for (Long wordIDtmp : wordIDset) {
			if (!this.getWordIndex().containsKey(wordIDtmp)) {
				tuIDMAP = wordTUList.get(wordIDtmp);
				tuIDary = new int[tuIDMAP.size()];
				int i = 0;
				for (Integer integer : tuIDMAP) {
					tuIDary[i++] = integer.intValue();
				}
				// map中就算key是int也不是顺序存放
				Arrays.sort(tuIDary);
				// if (!MergeSort.checkSort(tuIDary)) {
				// System.out.println("asdfasfd");
				// }

				this.getWordIndex().put(wordIDtmp, tuIDary);

				this.getWordTime().put(wordIDtmp, tuIDary.length);

				tuIDMAP.clear();
			}
		}
		wordIDset.clear();

		Set<Integer> tuIDset = tuWordList.keySet();
		ArrayList<Long> wordIDList;
		long[] wordIDary;
		for (Integer tuIDtmp : tuIDset) {
			if (!this.getTuIndex().containsKey(tuIDtmp)) {
				wordIDList = tuWordList.get(tuIDtmp);
				wordIDary = new long[wordIDList.size()];
				int i = 0;
				for (Long l : wordIDList) {
					wordIDary[i++] = l.longValue();
				}
				this.getTuIndex().put(tuIDtmp, wordIDary);

				wordIDList.clear();
			}
		}
		tuIDset.clear();

		wordTUList.clear();
		tuWordList.clear();

		ret = Const.SUCCESS;

		return ret;
	}

	@Override
	public int WriteTMText(int tmid) {
		int ret = Const.FAIL;
		HbaseTMTUDAO dao = null;
		try {
			dao = new HbaseTMTUDAO();
			dao.Init(true);
			dao.SETTable(tmid, true);// 存在则重建
			// for (TMTU tu : _tmFile._tmtuList) {
			// dao.InsertTMText(tu);
			// }
			dao.InsertTMTexts(this.getTmxFile()._tmtuList);

			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public int WriteTM(int tmid) {
		int ret = Const.FAIL;
		HbaseTMTUDAO dao = null;
		try {
			dao = new HbaseTMTUDAO();
			dao.Init(true);
			dao.SETTable(tmid, false);// 存在不重建
			dao.InsertTUIndex(this.getTuIndex());
			dao.InsertWordIndex(this.getWordIndex());
			dao.InsertWordLonger(this.getWordLonger());
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public int ReadTM(int tmid) {
		int ret = Const.FAIL;
		HbaseTMTUDAO dao = null;
		try {
			dao = new HbaseTMTUDAO();
			dao.Init(true);
			dao.SETTable(tmid, false);// 存在不重建
			List<HbaseRow> tuIndexRows = dao.SearchTUIndex();// _tuIndex
			List<HbaseRow> wordIndexRows = dao.SearchWordIndex();// _wordIndex
			List<HbaseRow> wordLongerRows = dao.SearchWordLonger();// _wordLonger
			dao.UnInit();

			String word;
			long wordID;
			int tuID;
			if (this.getTuIndex() == null) {
				this.setTuIndex(new HashMap<Integer, long[]>());
			} else if (this.getTuIndex().size() > 0) {
				this.getTuIndex().clear();
			}
			String wordids;
			long[] wordIDs;
			String[] sWordIDs;
			HashMap<Integer, long[]> tuIndextmp = this.getTuIndex();
			for (HbaseRow hbaseRow : tuIndexRows) {
				tuID = Util.String2Int(hbaseRow.getRowKey());
				wordids = hbaseRow.getCols().get("wordids");
				sWordIDs = wordids.split(",");
				wordIDs = new long[sWordIDs.length];
				for (int i = 0; i < sWordIDs.length; i++) {
					wordIDs[i] = Util.String2Long(sWordIDs[i]);
				}

				tuIndextmp.put(tuID, wordIDs);
			}

			if (this.getWordIndex() == null) {
				this.setWordIndex(new HashMap<Long, int[]>());
			} else if (this.getWordIndex().size() > 0) {
				this.getWordIndex().clear();
			}
			if (this.getWordTime() == null) {
				this.setWordTime(new HashMap<Long, Integer>());
			} else if (this.getWordTime().size() > 0) {
				this.getWordTime().clear();
			}
			String tuids;
			int[] tuIDs;
			String[] sTUIDs;
			for (HbaseRow hbaseRow : wordIndexRows) {
				wordID = Util.String2Long(hbaseRow.getRowKey());
				tuids = hbaseRow.getCols().get("tuids");
				sTUIDs = tuids.split(",");
				tuIDs = new int[sTUIDs.length];
				for (int i = 0; i < sTUIDs.length; i++) {
					tuIDs[i] = Util.String2Int(sTUIDs[i]);
				}
				this.getWordIndex().put(wordID, tuIDs);
				this.getWordTime().put(wordID, tuIDs.length);
			}
			if (this.getWordLonger() == null) {
				this.setWordLonger(new HashMap<String, Long>());
			} else if (this.getWordLonger().size() > 0) {
				this.getWordLonger().clear();
			}

			for (HbaseRow hbaseRow : wordLongerRows) {
				word = hbaseRow.getRowKey();
				wordID = Util.String2Long(hbaseRow.getCols().get("wordid"));
				this.getWordLonger().put(word, wordID);
			}
			if (this.getWordInvalid() == null) {
				this.setWordInvalid(new ArrayList<TMWord>());
			} else if (this.getWordInvalid().size() > 0) {
				for (TMWord tmword : this.getWordInvalid()) {
					tmword.word = null;
				}
				this.getWordInvalid().clear();
			}

			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public ArrayList<TMResult> SearchTM(String text) {
		// text分解单词列表
		ArrayList<TMWord> list = this.AnalyseWord(text);
		ArrayList<TMResult> tmresultList = null;
		// int searchTextWordCount = 0;
		long wordID;
		if (list != null) {
			if (list.size() == 1) {
				TMWord tmWord = list.get(0);
				if (tmWord.isWord && tmWord.isAbled) {
					wordID = this.GetHansonCodeByWord(tmWord.word);
					tmWord.wordID = wordID;
					tmresultList = this.GetTUListForOne(wordID);
					// searchTextWordCount = 1;
				}
			} else if (list.size() == 2) {
				TMWord first = list.get(0);
				wordID = this.GetHansonCodeByWord(first.word);
				first.wordID = wordID;
				if (this.getWordTime().containsKey(first.wordID)) {
					// 获得次数
					first.time = this.getWordTime().get(first.wordID);
				}
				TMWord second = list.get(1);
				wordID = this.GetHansonCodeByWord(second.word);
				second.wordID = wordID;

				if (this.getWordTime().containsKey(second.wordID)) {
					// 获得次数
					second.time = this.getWordTime().get(second.wordID);
				}
				if (first.time == 0) {
					tmresultList = this.GetTUListForOne(second.wordID);
					// searchTextWordCount = 1;
				} else if (second.time == 0) {
					tmresultList = this.GetTUListForOne(first.wordID);
					// searchTextWordCount = 1;
				} else if (first.time <= this.getWordMaxTimes()
						&& second.time <= this.getWordMaxTimes()) {
					if (first.wordID != second.wordID) {
						tmresultList = this.GetTUListForTwo(first.wordID,
								second.wordID);
						// searchTextWordCount = 2;
					}
				} else if (first.time <= this.getWordMaxTimes()
						&& second.time > this.getWordMaxTimes()) {
					tmresultList = this.GetTUListForOne(first.wordID);
					// searchTextWordCount = 1;
				} else if (first.time > this.getWordMaxTimes()
						&& second.time <= this.getWordMaxTimes()) {
					tmresultList = this.GetTUListForOne(second.wordID);
					// searchTextWordCount = 1;
				}
			} else {
				tmresultList = this.GetTUListForMore(list);
			}
		}
		if (tmresultList == null) {
			tmresultList = new ArrayList<TMResult>();
		}
		return tmresultList;
	}

	// 合并为一个list并相同word次数合计
	// private TMCountWordTU[] MergeTUList(ArrayList<TMCountWordTU[]> list) {
	// TMCountWordTU[] ret = null;
	// ArrayList<TMCountWordTU[]> tmp = list;
	// int time = tmp.size();
	// if (time == 0) {
	// ret = new TMCountWordTU[0];
	// } else {
	// int k = 0;
	// TMCountWordTU[] a;
	// TMCountWordTU[] b;
	// while (tmp.size() > 1 && k++ < time) {
	// for (int i = 0, j = list.size() - 1; i <= j; ++i, --j) {
	// if (i < j) {
	// a = tmp.get(i);
	// b = tmp.get(j);
	// tmp.set(i, TMMergeSort.MergeList(a, b));
	// tmp.remove(j);
	// }
	// }
	// }
	// ret = tmp.get(0);
	// }
	// return ret;
	// }

	private ArrayList<TMResult> GetTUListForOne(long wordID) {
		if (this.getWordIndex().containsKey(wordID)) {
			int[] index = this.getWordIndex().get(wordID);
			if (index.length > 0) {
				TMResult tmr;
				long[] tempary;
				ArrayList<TMResult> result = new ArrayList<TMResult>();
				if (index.length < 100) {
					// 小于100次，根据单词长度排序
					ArrayList<TMCountWordTU> list = new ArrayList<TMCountWordTU>();
					TMCountWordTU tmcount;

					for (int tuID : index) {
						if (this.getTuIndex().containsKey(tuID)) {
							tmcount = new TMCountWordTU();
							tmcount.tuID = tuID;
							tempary = this.getTuIndex().get(tuID);
							if (tempary == null) {
								tmcount.count = 0;
								break;
							} else {
								tmcount.count = tempary.length;
								list.add(tmcount);
							}
						}
					}
					Collections.sort(list, new TMWordTUTimeSortByTime());
					int indexlength = list.size();
					if (indexlength > this.getTuTimes()) {
						indexlength = this.getTuTimes();
					}
					for (int i = 0; i < indexlength; i++) {
						tmcount = list.get(i);
						tmr = new TMResult();
						tmr.tuID = tmcount.tuID;
						tmr.wordCount = 1;
						tmr.distance = tmcount.count - 1;
						result.add(tmr);
					}
				} else {
					// 大于100次，找到最前面的一部分就完事。
					int indexlength = index.length;
					if (indexlength > this.getTuTimes()) {
						indexlength = this.getTuTimes();
					}
					for (int i = 0; i < indexlength; i++) {
						if (this.getTuIndex().containsKey(index[i])) {
							tempary = this.getTuIndex().get(index[i]);
							if (tempary != null && tempary.length > 0) {
								tmr = new TMResult();
								tmr.tuID = index[i];
								tmr.wordCount = 1;
								tmr.distance = tempary.length - 1;
								result.add(tmr);
							}
						}
					}
				}

				return result;
			}

		}
		return new ArrayList<TMResult>();

	}

	private ArrayList<TMResult> GetTUListForTwo(long first, long second) {
		int[] firstindex = null;
		int[] secondindex = null;
		if (this.getWordIndex().containsKey(first)) {
			firstindex = this.getWordIndex().get(first);
		}
		if (this.getWordIndex().containsKey(first)) {
			secondindex = this.getWordIndex().get(second);
		}

		if (firstindex == null || firstindex.length == 0) {
			return this.GetTUListForOne(second);
		} else if (secondindex == null || secondindex.length == 0) {
			return this.GetTUListForOne(first);
		}

		ArrayList<TMCountWordTU> list = new ArrayList<TMCountWordTU>();
		TMCountWordTU tmcount;
		long[] tempary;
		int i = 0, j = 0;

		while (i < firstindex.length && j < secondindex.length)
			if (firstindex[i] < secondindex[j]) {
				++i;
			} else if (firstindex[i] == secondindex[j]) {

				tmcount = new TMCountWordTU();
				tmcount.tuID = firstindex[i++];
				if (this.getTuIndex().containsKey(tmcount.tuID)) {
					tempary = this.getTuIndex().get(tmcount.tuID);
					if (tempary != null) {
						tmcount.count = tempary.length;
						list.add(tmcount);
					}
				}
				++i;
				++j;
			} else {
				++j;
			}

		Collections.sort(list, new TMWordTUTimeSortByTime());

		int indexlength = list.size();
		if (indexlength > this.getTuTimes()) {
			indexlength = this.getTuTimes();
		}

		ArrayList<TMResult> listTMResult = new ArrayList<TMResult>();
		TMResult tmr;
		for (int k = 0; k < indexlength; k++) {
			tmcount = list.get(k);
			tmr = new TMResult();
			tmr.tuID = tmcount.tuID;
			tmr.wordCount = 2;
			tmr.distance = tmcount.count - 2;
			listTMResult.add(tmr);
		}

		return listTMResult;
	}

	private ArrayList<TMResult> GetTUListForMore(ArrayList<TMWord> list) {

		long wordID;

		HashSet<String> set = new HashSet<String>();

		double timescale;
		double lenscale;

		// 1、 有效单词列表,去重复
		// ArrayList<TMWord> wordlist = new ArrayList<TMWord>();
		// 2、 用来选定TUID的wordlist,time小于100或priority最小的四个
		ArrayList<TMWord> wordlistForTU = new ArrayList<TMWord>();

		// 次数大于100次的wordlist
		ArrayList<TMWord> wordGT100list = new ArrayList<TMWord>();
		// 循环所有单词,计算每个单词的相关信息
		for (TMWord tmWord : list) {
			if (tmWord.isWord) {
				// 去除重复
				if (set.contains(tmWord.word)) {
					continue;
				} else {
					set.add(tmWord.word);
				}

				wordID = this.GetHansonCodeByWord(tmWord.word);
				tmWord.wordID = wordID;
				if (this.getWordTime().containsKey(wordID)) {
					// 获得次数
					tmWord.time = this.getWordTime().get(wordID);
					if (tmWord.time > this.getWordMaxTimes()) {
						continue;
					} else if (tmWord.time > 100) {
						// 计算优先级
						// 单词越长，优先级priority越小，小于5的优先级不变，大于5的按照长度成比例
						if (tmWord.time > 1000) {
							timescale = tmWord.time / 1000.0;
						} else {
							timescale = 1000.0 / tmWord.time;
						}

						if (tmWord.word.length() > 5) {
							lenscale = tmWord.word.length() / 5.0;
						} else {
							lenscale = 5.0;
						}
						tmWord.priority = (int) (timescale * 50.0 / lenscale);

						wordGT100list.add(tmWord);
						// wordlist.add(tmWord);
					} else if (tmWord.time > 0) {
						wordlistForTU.add(tmWord);
						// wordlist.add(tmWord);
					}
				}
			}
		}

		// 大于100次的有效单词次数排序取最小四个
		Collections.sort(wordGT100list, new TMWordSortByTime());
		if (wordGT100list.size() <= 4) {
			wordlistForTU.addAll(wordGT100list);
		} else {
			wordlistForTU.addAll(wordGT100list.subList(0, 4));
		}

		wordGT100list.clear();

		// 3、所有用来选择TUID的单词次数排序后合并TUID列表
		Collections.sort(wordlistForTU, new TMWordSortByTime());

		ArrayList<TMCountWordTU[]> wordIDaryList = new ArrayList<TMCountWordTU[]>();
		TMCountWordTU[] countlist;
		TMCountWordTU tmpcount;
		int[] tmp;
		// 单词的TUID列表转换格式
		for (TMWord tmword : wordlistForTU) {
			if (this.getWordIndex().containsKey(tmword.wordID)) {
				tmp = this.getWordIndex().get(tmword.wordID);
				countlist = new TMCountWordTU[tmp.length];
				for (int i = 0; i < tmp.length; i++) {
					tmpcount = new TMCountWordTU();
					tmpcount.tuID = tmp[i];
					countlist[i] = tmpcount;
				}
				wordIDaryList.add(countlist);
			}
		}
		// 合并列表
		TMCountWordTU[] tuIDary = TMMergeSort.MergeTUList(wordIDaryList);
		wordIDaryList.clear();

		// 4、TUID列表排序，寻找TUID次数最多的 即TU中出现查询text中的单词最多的
		Arrays.sort(tuIDary, new TMWordTUTimeSortByTime());

		ArrayList<TMResult> resulttmpList = new ArrayList<TMResult>();
		ArrayList<Integer> tuIDtmpList = null;

		int precount = Integer.MAX_VALUE;
		TMCountWordTU wordTUcount;
		TMResult tmresult;
		for (int i = tuIDary.length - 1; i >= 0; --i) {
			wordTUcount = tuIDary[i];
			if (wordTUcount.count < precount) {
				if (resulttmpList.size() == 0) {
					if (tuIDtmpList != null && tuIDtmpList.size() > 0) {
						for (Integer integer : tuIDtmpList) {
							tmresult = new TMResult();
							tmresult.tuID = integer;
							tmresult.wordCount = precount;
							resulttmpList.add(tmresult);
						}
					}
					tuIDtmpList = new ArrayList<Integer>();
					tuIDtmpList.add(wordTUcount.tuID);

				} else if (resulttmpList.size() > this.getTuTimes()) {
					// 结果列表大于一定数量的时候不再继续计算
					if (tuIDtmpList != null) {
						tuIDtmpList.clear();
					}
					wordTUcount = null;
					break;
				} else {
					if (resulttmpList.size() + tuIDtmpList.size() <= this
							.getTuMaxTimes()) {
						for (Integer integer : tuIDtmpList) {
							tmresult = new TMResult();
							tmresult.tuID = integer;
							tmresult.wordCount = precount;
							resulttmpList.add(tmresult);
						}
					}
				}
				precount = wordTUcount.count;
				if (wordTUcount.count <= 2) {
					// 单词小于等于2个的不再计算之列
					if (tuIDtmpList != null) {
						tuIDtmpList.clear();
					}
					wordTUcount = null;
					break;
				}
			} else {
				tuIDtmpList.add(wordTUcount.tuID);
			}
		}
		// 这里是前面方法的补充，补充一下前面最后一次循环出现的有效数据
		if (tuIDtmpList != null
				&& resulttmpList.size() + tuIDtmpList.size() <= this
						.getTuMaxTimes()) {
			for (Integer integer : tuIDtmpList) {
				tmresult = new TMResult();
				tmresult.tuID = integer;
				tmresult.wordCount = precount;
				resulttmpList.add(tmresult);
			}
			tuIDtmpList.clear();
		}
		ArrayList<TMResult> result = new ArrayList<TMResult>();
		// 计算出距离
		int allwordcount = set.size();
		HashSet<Long> wordIDset = new HashSet<Long>();
		for (String word : set) {
			wordIDset.add(this.GetHansonCodeByWord(word));
		}
		set.clear();

		HashMap<Integer, long[]> tuIndexMaptmp;
		long[] tuIndextmp;
		for (TMResult tmres : resulttmpList) {
			// wordcount需要重新计算，之前的count仅仅是小于100次及大于100次的有效单词次数排序取最小四个的单词统计
			// if (tmres.wordCount > (list.size() + 1) / 2) {
			tuIndexMaptmp = this.getTuIndex();
			if (tuIndexMaptmp.containsKey(tmres.tuID)) {
				tuIndextmp = tuIndexMaptmp.get(tmres.tuID);
				// 单词个数转换
				tmres.wordCount = 0;
				for (long l : tuIndextmp) {
					if (wordIDset.contains(l)) {
						tmres.wordCount = tmres.wordCount + 1;
					}
				}
				if (tmres.wordCount > (allwordcount + 1) / 2) {
					tmres.distance = tuIndextmp.length;
					result.add(tmres);
				}
			}
		}

		resulttmpList.clear();
		Collections.sort(result, new TMResultSortDistanceDesc());
		return result;
	}

	@Override
	public int Init(TmxFileChunk tmFileChunk) {

		this.setTmxFileChunk(tmFileChunk);

		return Const.SUCCESS;
	}

	@Override
	public int ParseChunk(ArrayList<TMTU> list) {
		int ret = Const.FAIL;

		HashMap<String, Long> wordLonger = new HashMap<String, Long>();
		String word;
		long wordID;
		int tuID;
		StringBuffer sbindex;
		StringBuffer sbword;
		StringBuffer sbText;

		try {
			sbindex = new StringBuffer();
			sbword = new StringBuffer();
			sbText = new StringBuffer();

			ArrayList<TMWord> wordList;
			for (TMTU tu : list) {
				tuID = tu._tuid;

				sbText.append(tuID).append('\t').append(tu._tuv1.hashCode())
						.append('\t').append(tu._tuv2.hashCode()).append('\t')
						.append(tu._tuv1.replaceAll("\\t|\\r|\\n", " "))
						.append('\t')
						.append(tu._tuv2.replaceAll("\\t|\\r|\\n", " "))
						.append('\n');
				wordList = this.AnalyseWord(tu._tuv1);
				if (wordList != null && wordList.size() > 0) {
					for (TMWord tmWord : wordList) {
						wordID = 0;
						if (tmWord.isWord) {
							word = tmWord.word;
							if (tmWord.isAbled) {
								wordID = this.GetHansonCodeByWord(word);
								if (word.length() > 10) {
									if (!wordLonger.containsKey(word)) {
										wordLonger.put(word, wordID);
									}
									sbword.append(wordID).append('\t')
											.append(word).append('\n');
								}
								sbindex.append(tuID).append('\t')
										.append(wordID).append('\n');
							}
						}
					}
				}
			}
			// LoadDataInfile infile = new LoadDataInfile();
			// String indexFileName = filePath + tmid + "index.txt";
			// infile.WriteAppend(indexFileName, sbindex.toString());
			this.getTmxFileChunk().ChunkWriteAppendIndex(sbindex.toString());
			this.getTmxFileChunk().ChunkWriteAppendWord(sbword.toString());
			this.getTmxFileChunk().ChunkWriteAppendText(sbText.toString());
			// infile.WriteAppend(filePath + tmid + "word.txt",
			// sbword.toString());

			ret = Const.SUCCESS;

		} catch (Exception e) {
			Log4j.error(e);
		} finally {

		}
		return ret;
	}

	@Override
	public int ReadTMChunk(int tmID) {
		int ret = Const.FAIL;
		TMServiceDAO dao = null;
		try {

			if (this.getWordTime() == null) {
				this.setWordTime(new HashMap<Long, Integer>());
			} else if (this.getWordTime().size() > 0) {
				this.getWordTime().clear();
			}
			dao = new TMServiceDAO();
			dao.Init();
			List<TMServiceTimesBean> list = dao.SelectTimes(tmID);
			dao.UnInit();
			if (list != null && list.size() > 0) {
				for (TMServiceTimesBean tmServiceTimesBean : list) {
					if (tmServiceTimesBean != null
							&& tmServiceTimesBean.word_id >= 0
							&& tmServiceTimesBean.times > 0) {
						this.getWordTime().put(tmServiceTimesBean.word_id,
								tmServiceTimesBean.times);
					}
				}
			}

			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return ret;
	}

	@Override
	public ArrayList<TMResult> SearchTMChunk(int tmID, String text) {
		TMServiceDAO dao = null;
		ArrayList<TMResult> tmresultList = null;
		try {
			// text分解单词列表
			ArrayList<TMWord> list = this.AnalyseWord(text);
			// int searchTextWordCount = 0;
			long wordID;
			if (list != null) {
				Log4j.info("text AnalyseWord list size : " + list.size());
				dao = new TMServiceDAO();
				dao.Init();
				if (list.size() == 1) {
					TMWord tmWord = list.get(0);
					if (tmWord.isWord && tmWord.isAbled) {
						wordID = this.GetHansonCodeByWord(tmWord.word);
						tmWord.wordID = wordID;
						tmresultList = this.GetTUListForOneChunk(dao, tmID,
								wordID);
						// searchTextWordCount = 1;
					} else {
						Log4j.info("text AnalyseWord the only word("
								+ tmWord.word + ") is not abled. ");
					}
				} else if (list.size() == 2) {
					TMWord first = list.get(0);
					wordID = this.GetHansonCodeByWord(first.word);
					first.wordID = wordID;
					if (this.getWordTime().containsKey(first.wordID)) {
						// 获得次数
						first.time = this.getWordTime().get(first.wordID);
					}
					TMWord second = list.get(1);
					wordID = this.GetHansonCodeByWord(second.word);
					second.wordID = wordID;

					if (this.getWordTime().containsKey(second.wordID)) {
						// 获得次数
						second.time = this.getWordTime().get(second.wordID);
					}
					if (first.time <= 0) {
						Log4j.info("text AnalyseWord the first word("
								+ first.word + ") time 0. ");
						tmresultList = this.GetTUListForOneChunk(dao, tmID,
								second.wordID);
						// searchTextWordCount = 1;
					} else if (second.time <= 0) {
						Log4j.info("text AnalyseWord the second word("
								+ first.word + ") time 0. ");
						tmresultList = this.GetTUListForOneChunk(dao, tmID,
								first.wordID);
						// searchTextWordCount = 1;
					} else if (first.time <= this.getWordMaxTimes()
							&& second.time <= this.getWordMaxTimes()) {
						if (first.wordID != second.wordID) {
							tmresultList = this.GetTUListForTwoChunk(dao, tmID,
									first.wordID, second.wordID);
							// searchTextWordCount = 2;
						}
					} else if (first.time <= this.getWordMaxTimes()
							&& second.time > this.getWordMaxTimes()) {
						Log4j.info("text AnalyseWord the second word("
								+ first.word + ") time too large.");
						tmresultList = this.GetTUListForOneChunk(dao, tmID,
								first.wordID);
						// searchTextWordCount = 1;
					} else if (first.time > this.getWordMaxTimes()
							&& second.time <= this.getWordMaxTimes()) {
						Log4j.info("text AnalyseWord the first word("
								+ first.word + ") time too large.");
						tmresultList = this.GetTUListForOneChunk(dao, tmID,
								second.wordID);
						// searchTextWordCount = 1;
					}
				} else {
					tmresultList = this.GetTUListForMoreChunk(dao, tmID, list);
				}
			} else {
				Log4j.info("text AnalyseWord list size : 0");
			}
			if (tmresultList == null) {
				tmresultList = new ArrayList<TMResult>();
			}
		} catch (Exception e) {
			Log4j.error(e);
			tmresultList = new ArrayList<TMResult>();
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}
		return tmresultList;
	}

	private ArrayList<TMResult> GetTUListForOneChunk(TMServiceDAO dao,
			int tmID, long wordID) {
		HashMap<Long, int[]> wordmap = this.GetWordIndexChunk(dao, tmID,
				new long[] { wordID });
		if (wordmap.containsKey(wordID)) {
			int[] index = wordmap.get(wordID);
			if (index.length > 0) {
				TMResult tmr;
				long[] tempary;
				ArrayList<TMResult> result = new ArrayList<TMResult>();
				HashMap<Integer, long[]> tumap = this.GetTuIndexChunk(dao,
						tmID, index);
				if (index.length < 100) {
					// 小于100次，根据单词长度排序
					ArrayList<TMCountWordTU> list = new ArrayList<TMCountWordTU>();
					TMCountWordTU tmcount;

					for (int tuID : index) {
						if (tumap.containsKey(tuID)) {
							tmcount = new TMCountWordTU();
							tmcount.tuID = tuID;
							tempary = tumap.get(tuID);
							if (tempary == null) {
								tmcount.count = 0;
								break;
							} else {
								tmcount.count = tempary.length;
								list.add(tmcount);
							}
						}
					}
					Collections.sort(list, new TMWordTUTimeSortByTime());
					int indexlength = list.size();
					if (indexlength > this.getTuTimes()) {
						indexlength = this.getTuTimes();
					}
					for (int i = 0; i < indexlength; i++) {
						tmcount = list.get(i);
						tmr = new TMResult();
						tmr.tuID = tmcount.tuID;
						tmr.wordCount = 1;
						tmr.distance = tmcount.count - 1;
						result.add(tmr);
					}
				} else {
					// 大于100次，找到最前面的一部分就完事。
					int indexlength = index.length;
					if (indexlength > this.getTuTimes()) {
						indexlength = this.getTuTimes();
					}
					for (int i = 0; i < indexlength; i++) {
						if (tumap.containsKey(index[i])) {
							tempary = tumap.get(index[i]);
							if (tempary != null && tempary.length > 0) {
								tmr = new TMResult();
								tmr.tuID = index[i];
								tmr.wordCount = 1;
								tmr.distance = tempary.length - 1;
								result.add(tmr);
							}
						}
					}
				}

				return result;
			}

		}
		return new ArrayList<TMResult>();

	}

	private ArrayList<TMResult> GetTUListForTwoChunk(TMServiceDAO dao,
			int tmID, long first, long second) {
		HashMap<Long, int[]> wordmap = this.GetWordIndexChunk(dao, tmID,
				new long[] { first, second });
		int[] firstindex = null;
		int[] secondindex = null;
		if (wordmap.containsKey(first)) {
			firstindex = wordmap.get(first);
		}
		if (wordmap.containsKey(first)) {
			secondindex = wordmap.get(second);
		}

		if (firstindex == null || firstindex.length == 0) {
			return this.GetTUListForOneChunk(dao, tmID, second);
		} else if (secondindex == null || secondindex.length == 0) {
			return this.GetTUListForOneChunk(dao, tmID, first);
		}

		ArrayList<TMCountWordTU> list = new ArrayList<TMCountWordTU>();
		TMCountWordTU tmcount;
		long[] tempary;
		int i = 0, j = 0;

		HashMap<Integer, long[]> tumap = this.GetTuIndexChunk(dao, tmID,
				wordmap);

		while (i < firstindex.length && j < secondindex.length)
			if (firstindex[i] < secondindex[j]) {
				++i;
			} else if (firstindex[i] == secondindex[j]) {

				tmcount = new TMCountWordTU();
				tmcount.tuID = firstindex[i++];
				if (tumap.containsKey(tmcount.tuID)) {
					tempary = tumap.get(tmcount.tuID);
					if (tempary != null) {
						tmcount.count = tempary.length;
						list.add(tmcount);
					}
				}
				++i;
				++j;
			} else {
				++j;
			}

		Collections.sort(list, new TMWordTUTimeSortByTime());

		int indexlength = list.size();
		if (indexlength > this.getTuTimes()) {
			indexlength = this.getTuTimes();
		}

		ArrayList<TMResult> listTMResult = new ArrayList<TMResult>();
		TMResult tmr;
		for (int k = 0; k < indexlength; k++) {
			tmcount = list.get(k);
			tmr = new TMResult();
			tmr.tuID = tmcount.tuID;
			tmr.wordCount = 2;
			tmr.distance = tmcount.count - 2;
			listTMResult.add(tmr);
		}

		return listTMResult;
	}

	private ArrayList<TMResult> GetTUListForMoreChunk(TMServiceDAO dao,
			int tmID, ArrayList<TMWord> list) {

		long wordID;

		HashSet<String> set = new HashSet<String>();

		double timescale;
		double lenscale;

		// 1、 有效单词列表,去重复
		// ArrayList<TMWord> wordlist = new ArrayList<TMWord>();
		// 2、 用来选定TUID的wordlist,time小于100或priority最小的四个
		ArrayList<TMWord> wordlistForTU = new ArrayList<TMWord>();

		// 次数大于100次的wordlist
		ArrayList<TMWord> wordGT100list = new ArrayList<TMWord>();
		// 循环所有单词,计算每个单词的相关信息
		for (TMWord tmWord : list) {
			if (tmWord.isWord) {
				// 去除重复
				if (set.contains(tmWord.word)) {
					continue;
				} else {
					set.add(tmWord.word);
				}

				wordID = this.GetHansonCodeByWord(tmWord.word);
				tmWord.wordID = wordID;
				if (this.getWordTime().containsKey(wordID)) {
					// 获得次数
					tmWord.time = this.getWordTime().get(wordID);
					if (tmWord.time > this.getWordMaxTimes()) {
						continue;
					} else if (tmWord.time > 100) {
						// 计算优先级
						// 单词越长，优先级priority越小，小于5的优先级不变，大于5的按照长度成比例
						if (tmWord.time > 1000) {
							timescale = tmWord.time / 1000.0;
						} else {
							timescale = 1000.0 / tmWord.time;
						}

						if (tmWord.word.length() > 5) {
							lenscale = tmWord.word.length() / 5.0;
						} else {
							lenscale = 5.0;
						}
						tmWord.priority = (int) (timescale * 50.0 / lenscale);

						wordGT100list.add(tmWord);
						// wordlist.add(tmWord);
					} else if (tmWord.time > 0) {
						wordlistForTU.add(tmWord);
						// wordlist.add(tmWord);
					}
				}
			}
		}

		// 大于100次的有效单词次数排序取最小四个
		Collections.sort(wordGT100list, new TMWordSortByTime());
		if (wordGT100list.size() <= 4) {
			wordlistForTU.addAll(wordGT100list);
		} else {
			wordlistForTU.addAll(wordGT100list.subList(0, 4));
		}

		wordGT100list.clear();

		Log4j.info("the four gt100 and less or equals then 100 list(size:"
				+ wordlistForTU.size() + "):"
				+ super.LogTMWordList(wordlistForTU));

		// 3、所有用来选择TUID的单词次数排序后合并TUID列表
		Collections.sort(wordlistForTU, new TMWordSortByTime());

		ArrayList<TMCountWordTU[]> wordIDaryList = new ArrayList<TMCountWordTU[]>();
		TMCountWordTU[] countlist;
		TMCountWordTU tmpcount;
		int[] tmp;

		// 数据库查询一下所有单词对应的TUID列表
		// long[] wordIDary;
		ArrayList<Long> wordIDlist = new ArrayList<Long>();
		for (TMWord tmword : wordlistForTU) {
			wordIDlist.add(tmword.wordID);
		}
		HashMap<Long, int[]> wordmap = this.GetWordIndexChunk(dao, tmID,
				Util.LonglistToAry(wordIDlist));
		// 单词的TUID列表转换格式
		for (TMWord tmword : wordlistForTU) {
			if (wordmap.containsKey(tmword.wordID)) {
				tmp = wordmap.get(tmword.wordID);
				countlist = new TMCountWordTU[tmp.length];
				for (int i = 0; i < tmp.length; i++) {
					tmpcount = new TMCountWordTU();
					tmpcount.tuID = tmp[i];
					countlist[i] = tmpcount;
				}
				wordIDaryList.add(countlist);
			}
		}
		// 合并列表
		TMCountWordTU[] tuIDary = TMMergeSort.MergeTUList(wordIDaryList);
		wordIDaryList.clear();

		// 4、TUID列表排序，寻找TUID次数最多的 即TU中出现查询text中的单词最多的
		Arrays.sort(tuIDary, new TMWordTUTimeSortByTime());

		Log4j.info("the sorted TUID array (length:" + tuIDary.length + "):"
				+ super.LogTMCountWordTUArray(tuIDary));

		ArrayList<TMResult> resulttmpList = new ArrayList<TMResult>();
		ArrayList<Integer> tuIDtmpList = null;

		int precount = Integer.MAX_VALUE;
		TMCountWordTU wordTUcount;
		TMResult tmresult;
		for (int i = tuIDary.length - 1; i >= 0; --i) {
			wordTUcount = tuIDary[i];
			if (wordTUcount.count < precount) {
				if (resulttmpList.size() == 0) {
					if (tuIDtmpList != null && tuIDtmpList.size() > 0) {
						for (Integer integer : tuIDtmpList) {
							tmresult = new TMResult();
							tmresult.tuID = integer;
							tmresult.wordCount = precount;
							resulttmpList.add(tmresult);
						}
						tuIDtmpList.clear();
					}

					tuIDtmpList = new ArrayList<Integer>();
					tuIDtmpList.add(wordTUcount.tuID);

				} else if (resulttmpList.size() > this.getTuTimes()) {
					// 结果列表大于一定数量的时候不再继续计算
					if (tuIDtmpList != null) {
						tuIDtmpList.clear();
					}
					wordTUcount = null;
					break;
				} else {
					if (resulttmpList.size() + tuIDtmpList.size() <= this
							.getTuMaxTimes()) {
						if (tuIDtmpList != null && tuIDtmpList.size() > 0) {
							for (Integer integer : tuIDtmpList) {
								tmresult = new TMResult();
								tmresult.tuID = integer;
								tmresult.wordCount = precount;
								resulttmpList.add(tmresult);
							}
							tuIDtmpList.clear();
						}

						tuIDtmpList = new ArrayList<Integer>();
						tuIDtmpList.add(wordTUcount.tuID);
					}
				}
				precount = wordTUcount.count;
				if (wordTUcount.count <= 2) {
					// 单词小于等于2个的不再计算之列
					if (tuIDtmpList != null) {
						tuIDtmpList.clear();
					}
					wordTUcount = null;
					break;
				}
			} else {
				tuIDtmpList.add(wordTUcount.tuID);
			}
		}
		// 这里是前面方法的补充，补充一下前面最后一次循环出现的有效数据
		if (tuIDtmpList != null
				&& resulttmpList.size() + tuIDtmpList.size() <= this
						.getTuMaxTimes()) {
			for (Integer integer : tuIDtmpList) {
				tmresult = new TMResult();
				tmresult.tuID = integer;
				tmresult.wordCount = precount;
				resulttmpList.add(tmresult);
			}
			tuIDtmpList.clear();
		}

		Log4j.info("the valid(first) result list(size:" + resulttmpList.size()
				+ "):" + super.LogTMResultList(resulttmpList));

		ArrayList<TMResult> result = new ArrayList<TMResult>();
		// 计算出距离
		int allwordcount = set.size();
		HashSet<Long> wordIDset = new HashSet<Long>();
		for (String word : set) {
			wordIDset.add(this.GetHansonCodeByWord(word));
		}
		set.clear();

		// 数据库查询一下所有单词对应的TUID列表
		ArrayList<Integer> tuIDlist = new ArrayList<Integer>();
		for (TMResult tmres : resulttmpList) {
			tuIDlist.add(tmres.tuID);
		}
		HashMap<Integer, long[]> tuIndexMaptmp = this.GetTuIndexChunk(dao,
				tmID, Util.IntegerlistToAry(tuIDlist));
		long[] tuIndextmp;
		for (TMResult tmres : resulttmpList) {
			// wordcount需要重新计算，之前的count仅仅是小于100次及大于100次的有效单词次数排序取最小四个的单词统计
			if (tuIndexMaptmp.containsKey(tmres.tuID)) {
				tuIndextmp = tuIndexMaptmp.get(tmres.tuID);
				// 单词个数转换
				tmres.wordCount = 0;
				for (long l : tuIndextmp) {
					if (wordIDset.contains(l)) {
						tmres.wordCount = tmres.wordCount + 1;
					}
				}
				if (tmres.wordCount > (allwordcount + 1) / 2) {
					tmres.distance = tuIndextmp.length;
					result.add(tmres);
				}
			}
		}

		resulttmpList.clear();

		Collections.sort(result, new TMResultSortDistanceDesc());

		Log4j.info("the valid(second) result list(size:" + result.size() + "):"
				+ super.LogTMResultList(result));

		return result;
	}

	private HashMap<Long, int[]> GetWordIndexChunk(TMServiceDAO dao, int tmID,
			long[] wordIDary) {
		HashMap<Long, int[]> wordIndextmp = new HashMap<Long, int[]>();
		if (wordIDary != null && wordIDary.length > 0) {
			List<TMServiceIndexBean> beanlist = dao.SelectIndexByWordIDs(tmID,
					wordIDary);
			long wordID = 0;
			long wordIDtmp;
			int tuIDtmp;
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (TMServiceIndexBean tmServiceIndexBean : beanlist) {

				wordIDtmp = tmServiceIndexBean.word_id;
				tuIDtmp = tmServiceIndexBean.tu_id;

				if (wordID < wordIDtmp) {
					if (list.size() > 0) {
						wordIndextmp.put(wordID, Util.IntegerlistToAry(list));
					}
					list.clear();
					wordID = wordIDtmp;
				}
				list.add(tuIDtmp);
			}

			if (list.size() > 0) {
				wordIndextmp.put(wordID, Util.IntegerlistToAry(list));
			}
		}
		return wordIndextmp;
	}

	private HashMap<Integer, long[]> GetTuIndexChunk(TMServiceDAO dao,
			int tmID, int[] tuIDary) {
		HashMap<Integer, long[]> tuIndextmp = new HashMap<Integer, long[]>();
		if (tuIDary != null && tuIDary.length > 0) {
			List<TMServiceIndexBean> beanlist = dao.SelectIndexByTuIDs(tmID,
					tuIDary);
			int tuID = 0;
			int tuIDtmp;
			long wordIDtmp;
			ArrayList<Long> list = new ArrayList<Long>();
			for (TMServiceIndexBean tmServiceIndexBean : beanlist) {

				tuIDtmp = tmServiceIndexBean.tu_id;
				wordIDtmp = tmServiceIndexBean.word_id;

				if (tuID < tuIDtmp) {
					if (list.size() > 0) {
						tuIndextmp.put(tuID, Util.LonglistToAry(list));
					}
					list.clear();
					tuID = tuIDtmp;
				}
				list.add(wordIDtmp);
			}
			if (list.size() > 0) {
				tuIndextmp.put(tuID, Util.LonglistToAry(list));
			}
			list.clear();
		}
		return tuIndextmp;
	}

	private HashMap<Integer, long[]> GetTuIndexChunk(TMServiceDAO dao,
			int tmID, HashMap<Long, int[]> map) {

		if (map != null && map.size() > 0) {
			// StringBuffer tuIDSQL = new StringBuffer();
			HashSet<Integer> set = new HashSet<Integer>();
			int[] tuIDs;
			for (Long wordID : map.keySet()) {
				tuIDs = map.get(wordID);
				for (int tuID : tuIDs) {
					if (!set.contains(tuID)) {
						set.add(tuID);
					}
				}
			}
			int[] tuIDary = new int[set.size()];
			int i = 0;
			for (int tuID : set) {
				tuIDary[i++] = tuID;
			}
			return this.GetTuIndexChunk(dao, tmID, tuIDary);
		}
		HashMap<Integer, long[]> tuIndextmp = new HashMap<Integer, long[]>();
		return tuIndextmp;
	}
}
