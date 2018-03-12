package com.wiitrans.base.tm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sun.security.krb5.internal.LoginOptions;

import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.db.model.TMServiceTextBean;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.hbase.HbaseRow;
import com.wiitrans.base.hbase.HbaseTMTUDAO;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;

public class TMZHCN extends TMLanguage {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.CHINESE;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.IDEOGRAPH;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.ZHCN;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_ZHCN;

	private long _first = 91125000000000L;
	private long _second = 2025000000;
	private long _third = 45000;

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
		int result;
		// if (c >= 0x3000 && c <= 0x9fff || c >= 0xac00 && c <= 0xd7af) {
		if (c >= 0x4e00 && c <= 0x9fbb) {
			result = c;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public String GetWordByHansonCode(long wordID) {
		return null;
	}

	@Override
	public long GetHansonCodeByWord(String word) {
		if (word == null || word.length() <= 2) {
			return 0;
		}
		long result;
		result = word.charAt(0) * _first;

		result += word.charAt(1) * _second;

		result += word.charAt(2) * _third;
		if (word.length() >= 4) {
			result += word.charAt(3);
		}
		return result;
	}

	@Override
	public ArrayList<TMWord> AnalyseWord(String sentence) {
		char[] ary = sentence.toCharArray();
		ArrayList<TMWord> list = new ArrayList<TMWord>();
		int unicode;
		TMWord tmword;
		for (int i = 0; i < ary.length; i++) {
			unicode = this.IsLetter(ary[i]);
			if (unicode > 0) {
				tmword = new TMWord();
				tmword.word = String.valueOf(ary[i]);
				tmword.isWord = true;
				tmword.isAbled = true;
				tmword.sequence = list.size() + 1;
				tmword.beginAt = i + 1;
				list.add(tmword);
			}
		}
		return list;
	}

	@Override
	public int Init(TmxFile tmFile, boolean isSource) {
		int ret = Const.FAIL;
		this.setTmxFile(tmFile);
		_isSource = isSource;
		return ret;
	}

	@Override
	public int UnInit() {
		int ret = Const.FAIL;
		this.setTmxFile(null);
		if (this.getWordIndex() != null) {
			this.getWordIndex().clear();
		}
		return ret;
	}

	private HashMap<Long, int[]> _wordIndex;

	private HashMap<Long, int[]> getWordIndex() {
		return _wordIndex;
	}

	private void setWordIndex(HashMap<Long, int[]> wordIndex) {
		this._wordIndex = wordIndex;
	}

	private TMIndexWord[] _tmIndexWord;

	private TMIndexWord[] getTMIndexWord() {
		return _tmIndexWord;
	}

	private void setTMIndexWord(TMIndexWord[] tmIndexWord) {
		this._tmIndexWord = tmIndexWord;
	}

	@Override
	public int Parse(int tmid) {
		int ret = Const.FAIL;
		// word的tuID列表，存储在_wordIndex中，
		this.setWordIndex(new HashMap<Long, int[]>());

		// parse方法中的word所在tuID列表,在方法最后转成_wordIndex
		HashMap<Long, ArrayList<Integer>> wordTUList = new HashMap<Long, ArrayList<Integer>>();

		int tuID;
		ArrayList<TMWord> wordList;
		ArrayList<Long> wordIDList;
		HashSet<Long> wordIDset3 = new HashSet<Long>();
		HashSet<Long> wordIDset4 = new HashSet<Long>();

		ArrayList<Integer> tuIDListForWordID;
		for (TMTU tu : this.getTmxFile()._tmtuList) {
			tuID = tu._tuid;
			wordList = this.AnalyseWord(tu._tuv1);

			wordIDList = new ArrayList<Long>();
			for (TMWord tmWord : wordList) {
				if (tmWord.isWord && tmWord.isAbled
						&& tmWord.word.length() == 1) {
					wordIDList.add((long) (tmWord.word.charAt(0)));
				}
			}

			if (wordIDList != null && wordIDList.size() > 0) {
				// 每4个字的unicode组成一个long的wordid
				long wordID;
				int wordlen = wordIDList.size();
				wordIDset3.clear();
				wordIDset4.clear();
				for (int i = 0; i < wordlen - 3; i++) {
					wordID = wordIDList.get(i) * _first;

					wordID += wordIDList.get(i + 1) * _second;

					wordID += wordIDList.get(i + 2) * _third;
					if (!wordIDset3.contains(wordID)) {
						wordIDset3.add(wordID);
					}
					wordID += wordIDList.get(i + 3);
					if (!wordIDset4.contains(wordID)) {
						wordIDset4.add(wordID);
					}
				}

				if (wordlen >= 3) {
					wordID = wordIDList.get(wordlen - 3) * _first;

					wordID += wordIDList.get(wordlen - 2) * _second;

					wordID += wordIDList.get(wordlen - 1) * _third;
					if (!wordIDset3.contains(wordID)) {
						wordIDset4.add(wordID);
					}
				}

				for (Long lWordID : wordIDset4) {
					if (wordTUList.containsKey(lWordID)) {
						tuIDListForWordID = wordTUList.get(lWordID);
					} else {
						tuIDListForWordID = new ArrayList<Integer>();
						wordTUList.put(lWordID, tuIDListForWordID);
					}

					if (!tuIDListForWordID.contains(tuID)) {
						tuIDListForWordID.add(tuID);
					}
				}

				wordIDList.clear();
			}
			wordIDList = null;
		}

		Set<Long> wordIDset = wordTUList.keySet();
		ArrayList<Integer> wordIDlist;
		int[] tuIDary;
		for (Long wordIDtmp : wordIDset) {
			if (!this.getWordIndex().containsKey(wordIDtmp)) {
				wordIDlist = wordTUList.get(wordIDtmp);
				tuIDary = new int[wordIDlist.size()];
				int i = 0;
				for (Integer integer : wordIDlist) {
					tuIDary[i++] = integer.intValue();
				}
				// map中就算key是int也不是顺序存放
				Arrays.sort(tuIDary);

				this.getWordIndex().put(wordIDtmp, tuIDary);

				tuIDary = null;
			}
		}
		wordIDset.clear();

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
			dao.InsertWordIndex(this.getWordIndex());
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
			List<HbaseRow> wordIndexRows = dao.SearchWordIndex();// _wordIndex
			dao.UnInit();

			long wordID;

			ArrayList<TMIndexWord> list = new ArrayList<TMIndexWord>();
			TMIndexWord tmp;
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
				tmp = new TMIndexWord();
				tmp.wordID = wordID;
				tmp.tuIDs = tuIDs;
				list.add(tmp);
			}

			TMIndexWord[] ary = new TMIndexWord[wordIndexRows.size()];
			list.toArray(ary);
			this.setTMIndexWord(ary);

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
		ArrayList<String> wordlist = this.getWordFor34(list);
		HashSet<Long> wordIDset3 = new HashSet<Long>();
		HashSet<Long> wordIDset4 = new HashSet<Long>();
		long wordID;
		ArrayList<TMIndexWord> tmindexwordlist = new ArrayList<TMIndexWord>();
		for (String searchword : wordlist) {
			if (searchword != null && searchword.length() >= 3) {
				wordID = searchword.charAt(0) * _first;

				wordID += searchword.charAt(1) * _second;

				wordID += searchword.charAt(2) * _third;

				if (!wordIDset3.contains(wordID)) {
					wordIDset3.add(wordID);
					if (searchword.length() == 3) {
						TMIndexWord tmindexword = new TMIndexWord();
						tmindexword.word = searchword;
						tmindexword.wordID = wordID;
						this.getTMIndexWord(tmindexword);
						if (tmindexword.tuIDs != null
								&& tmindexword.tuIDs.length > 0) {
							tmindexwordlist.add(tmindexword);
						}
					}
				}

				if (searchword.length() == 4) {
					wordID += searchword.charAt(3);
					if (!wordIDset4.contains(wordID)) {
						wordIDset4.add(wordID);
						TMIndexWord tmindexword = new TMIndexWord();
						tmindexword.word = searchword;
						tmindexword.wordID = wordID;
						this.getTMIndexWord(tmindexword);
						if (tmindexword.tuIDs != null
								&& tmindexword.tuIDs.length > 0) {
							tmindexwordlist.add(tmindexword);
						}
					}
				}

			}
		}
		ArrayList<TMCountWordTU[]> wordIDaryList = new ArrayList<TMCountWordTU[]>();
		TMCountWordTU[] countlist;
		TMCountWordTU tmpcount;
		for (TMIndexWord tmindexword : tmindexwordlist) {
			countlist = new TMCountWordTU[tmindexword.tuIDs.length];
			for (int i = 0; i < tmindexword.tuIDs.length; i++) {
				tmpcount = new TMCountWordTU();
				tmpcount.tuID = tmindexword.tuIDs[i];
				countlist[i] = tmpcount;
			}
			wordIDaryList.add(countlist);
		}

		// 合并列表
		TMCountWordTU[] tuIDary = TMMergeSort.MergeTUList(wordIDaryList);
		wordIDaryList.clear();

		// 4、TUID列表排序，寻找TUID次数最多的 即TU中出现查询text中的单词最多的
		Arrays.sort(tuIDary, new TMWordTUTimeSortByTimeDESC());
		ArrayList<TMResult> result = new ArrayList<TMResult>();
		for (int i = 0; i < tuIDary.length; i++) {
			TMResult tmresult = new TMResult();
			tmresult.tuID = tuIDary[i].tuID;
			tmresult.wordCount = tuIDary[i].count;
			result.add(tmresult);
		}
		return result;
	}

	private void getTMIndexWord(TMIndexWord tmindexword) {
		if (tmindexword != null && tmindexword.wordID > 0
				&& tmindexword.word != null) {
			TMIndexWord[] ary = this.getTMIndexWord();
			if (ary != null && ary.length > 0) {
				if (tmindexword.wordID < ary[0].wordID) {
					tmindexword.tuIDs = new int[0];
				} else if (tmindexword.wordID == ary[0].wordID) {
					tmindexword.tuIDs = ary[0].tuIDs;
				} else if (tmindexword.wordID > ary[ary.length - 1].wordID) {
					tmindexword.tuIDs = new int[0];
				} else if (tmindexword.wordID == ary[ary.length - 1].wordID) {
					tmindexword.tuIDs = ary[ary.length - 1].tuIDs;
				} else {
					if (tmindexword.word.length() == 4
							&& tmindexword.wordID % 45000 > 0) {
						int index = this.getMatchWordID(tmindexword.wordID,
								ary, 0, ary.length - 1,
								tmindexword.word.length() == 4);
						if (index >= 0) {
							tmindexword.tuIDs = ary[index].tuIDs;
						} else {
							tmindexword.tuIDs = new int[0];
						}
					} else if (tmindexword.word.length() == 3
							&& tmindexword.wordID % 45000 == 0) {
						int index = this.getMatchWordID(tmindexword.wordID,
								ary, 0, ary.length - 1,
								tmindexword.word.length() == 4);
						ArrayList<TMCountWordTU[]> wordIDaryList = new ArrayList<TMCountWordTU[]>();
						int[] tmp;
						TMCountWordTU[] countlist;
						TMCountWordTU tmpcount;
						for (int i = index; ary[i].wordID < tmindexword.wordID + 45000; i++) {
							tmp = ary[i].tuIDs;
							countlist = new TMCountWordTU[tmp.length];
							for (int j = 0; j < tmp.length; j++) {
								tmpcount = new TMCountWordTU();
								tmpcount.tuID = tmp[j];
								countlist[j] = tmpcount;
							}
							wordIDaryList.add(countlist);
						}
						TMCountWordTU[] tuIDary = TMMergeSort
								.MergeTUList(wordIDaryList);
						wordIDaryList.clear();
						if (tuIDary.length > 0) {
							int[] tuIDs = new int[tuIDary.length];
							for (int i = 0; i < tuIDary.length; i++) {
								tuIDs[i] = tuIDary[i].tuID;
							}
							tmindexword.tuIDs = tuIDs;
						} else {
							tmindexword.tuIDs = new int[0];
						}
					} else {
						tmindexword.tuIDs = new int[0];
					}
				}

			} else {
				tmindexword.tuIDs = new int[0];
			}
		}
	}

	private int getMatchWordID(long wordID, TMIndexWord[] ary, int left,
			int right, boolean isequals) {
		if (left + 1 < right) {
			int middle = (left + right) / 2;
			if (ary[middle].wordID > wordID) {
				return this.getMatchWordID(wordID, ary, left, middle, isequals);
			} else if (ary[middle].wordID < wordID) {
				return this
						.getMatchWordID(wordID, ary, middle, right, isequals);
			} else {
				return middle;
			}
		} else {
			if (isequals) {
				return -1;
			} else {
				return right;
			}
		}
	}

	private ArrayList<String> getWordFor34(ArrayList<TMWord> list) {
		// 每个文字char的编码
		ArrayList<Long> wordIDList = new ArrayList<Long>();
		for (TMWord tmWord : list) {
			if (tmWord.isWord && tmWord.isAbled && tmWord.word.length() == 1) {
				wordIDList.add((long) (tmWord.word.charAt(0)));
			}
		}
		if (wordIDList != null && wordIDList.size() > 2) {
			int[] lengthAry = this.getLengthAry(wordIDList.size());
			int first = 0;
			ArrayList<String> result = new ArrayList<String>();
			StringBuffer sb;
			//TMWord tmWordtmp;
			for (int len : lengthAry) {
				sb = new StringBuffer();
				for (int i = first; i < first + len; i++) {
					sb.append((char) (wordIDList.get(i).longValue()));
				}
				first += len;
				result.add(sb.toString());
			}

			return result;
		} else {
			return new ArrayList<String>();
		}
	}

	private int[] getLengthAry(int count) {
		if (count >= 6) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			int tmp = count;
			while (tmp > 9) {
				list.add(4);
				tmp -= 4;
			}

			switch (tmp) {
			case 6:
				list.add(3);
				list.add(3);
				break;
			case 7:
				list.add(4);
				list.add(3);
				break;
			case 8:
				list.add(4);
				list.add(4);
				break;
			case 9:
				list.add(3);
				list.add(3);
				list.add(3);
				break;
			default:
				break;
			}
			int[] result = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				result[i] = list.get(i);
			}
			return result;
		} else if (count == 3) {
			return new int[] { 3 };
		} else if (count == 4 || count == 5) {
			return new int[] { 4 };
		} else {
			return new int[0];
		}
	}

	@Override
	public int Init(TmxFileChunk tmFileChunk) {
		this.setTmxFileChunk(tmFileChunk);
		return Const.SUCCESS;
	}

	@Override
	public int ParseChunk(ArrayList<TMTU> list) {
		int ret = Const.FAIL;

		long wordID;
		ArrayList<Long> wordIDList;
		int tuID;
		StringBuffer sbindex;
		StringBuffer sbText;

		try {
			sbindex = new StringBuffer();
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

				wordIDList = new ArrayList<Long>();
				for (TMWord tmWord : wordList) {
					if (tmWord.isWord && tmWord.isAbled
							&& tmWord.word.length() == 1) {
						wordIDList.add((long) (tmWord.word.charAt(0)));
					}
				}

				if (wordIDList != null && wordIDList.size() > 0) {
					// 每4个字的unicode组成一个long的wordid
					int wordlen = wordIDList.size();
					for (int i = 0; i < wordlen - 2; i++) {
						wordID = wordIDList.get(i) * _first;

						wordID += wordIDList.get(i + 1) * _second;

						wordID += wordIDList.get(i + 2) * _third;
						if (i < wordlen - 3) {
							wordID += wordIDList.get(i + 3);
						}

						sbindex.append(tuID).append('\t').append(wordID)
								.append('\n');
					}
					wordIDList.clear();
				}
				wordIDList = null;
			}

			this.getTmxFileChunk().ChunkWriteAppendIndex(sbindex.toString());
			this.getTmxFileChunk().ChunkWriteAppendText(sbText.toString());

			ret = Const.SUCCESS;

		} catch (Exception e) {
			Log4j.error(e);
		} finally {

		}
		return ret;
	}

	@Override
	public int ReadTMChunk(int tmid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<TMResult> SearchTMChunk(int tmID, String text) {
		// text分解单词列表
		ArrayList<TMWord> list = this.AnalyseWord(text);
		ArrayList<String> wordlist = this.getWordFor34(list);
		HashSet<Long> wordIDset = new HashSet<Long>();
		long wordID;
		for (String searchword : wordlist) {
			if (searchword != null && searchword.length() >= 3) {
				wordID = searchword.charAt(0) * _first;

				wordID += searchword.charAt(1) * _second;

				wordID += searchword.charAt(2) * _third;

				if (!wordIDset.contains(wordID)) {
					if (searchword.length() == 4) {
						wordID += searchword.charAt(3);
					}
					wordIDset.add(wordID);
				}
			}
		}
		if (wordIDset.size() > 0) {
			long[] ary = new long[wordIDset.size()];
			int i = 0;
			for (long l : wordIDset) {
				ary[i++] = l;
			}
			TMServiceDAO dao = null;
			try {
				dao = new TMServiceDAO();
				dao.Init();
				List<TMServiceTextBean> beanlist = dao.SelectTextByCHNWordIDs(
						tmID, ary);
				if (beanlist != null && beanlist.size() > 0) {
					ArrayList<TMResult> resultlist = new ArrayList<TMResult>();
					TMResult result;
					for (TMServiceTextBean bean : beanlist) {
						result = new TMResult();
						result.tuID = bean.tu_id;
						result.source = bean.source;
						result.target = bean.target;
						resultlist.add(result);
					}
					return resultlist;
				}
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
				if (dao != null) {
					dao.UnInit();
				}
			}
		}
		return new ArrayList<TMResult>();
	}
}
