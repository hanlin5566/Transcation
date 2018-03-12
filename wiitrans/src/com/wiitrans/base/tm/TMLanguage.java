package com.wiitrans.base.tm;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.TmxFileChunk;
import com.wiitrans.base.log.Log4j;

public abstract class TMLanguage {

	public abstract LANGUAGE_NAME GetLanguageName();

	public abstract LANGUAGE_TYPE GetLanguageType();

	public abstract String GetLanguageCountryName();

	public abstract LANGUAGE_COUNTRY GetLanguageCountry();

	public abstract int IsLetter(char c);

	public abstract String GetWordByHansonCode(long wordID);

	public abstract long GetHansonCodeByWord(String word);

	public abstract ArrayList<TMWord> AnalyseWord(String sentence);

	public abstract int Init(TmxFile tmFile, boolean isSource);

	public abstract int UnInit();

	public abstract int Parse(int tmid);

	public abstract int Init(TmxFileChunk tmFileChunk);

	public abstract int ParseChunk(ArrayList<TMTU> tmtuList);

	public abstract int WriteTMText(int tmid);

	public abstract int WriteTM(int tmid);

	public abstract int ReadTM(int tmid);

	public abstract int ReadTMChunk(int tmid);

	public abstract ArrayList<TMResult> SearchTM(String text);

	public abstract ArrayList<TMResult> SearchTMChunk(int tmID, String text);

	public String LogTMWordList(ArrayList<TMWord> list) {
		StringBuffer sb = new StringBuffer();
		if (list != null && list.size() > 0) {
			for (TMWord word : list) {
				sb.append(" ").append(word.word).append(" ").append(word.time);
			}
		}
		return sb.toString();
	}

	public String LogTMCountWordTUArray(TMCountWordTU[] ary) {
		StringBuffer sb = new StringBuffer();
		if (ary != null && ary.length > 0) {
			for (TMCountWordTU tu : ary) {
				sb.append(" TUID:").append(tu.tuID).append(" ")
						.append(tu.count);
			}
		}
		return sb.toString();
	}

	public String LogTMResultList(ArrayList<TMResult> list) {
		StringBuffer sb = new StringBuffer();
		if (list != null && list.size() > 0) {
			for (TMResult result : list) {
				sb.append(" ").append(result.tuID).append(" count/distance")
						.append(result.wordCount).append("/")
						.append(result.distance);
			}
		}
		return sb.toString();
	}

}
