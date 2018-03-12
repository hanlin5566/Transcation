package com.wiitrans.frag.bolt;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONObject;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.tuple.Values;

import com.wiitrans.base.db.SentCheckWordDAO;
import com.wiitrans.base.db.model.SentCheckWordBean;
import com.wiitrans.base.file.lang.CheckEnglish;
import com.wiitrans.base.file.lang.CheckLanguage;
import com.wiitrans.base.file.lang.Word;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.tm.LoadDataInfile;
import com.wiitrans.base.xml.WiitransConfig;

public class SentCheckWord {
	public static void main(String[] args) {
		WiitransConfig.getInstance(0);
		new SentCheckWord().ChechWord(111, 222,
				"<look sdf and sdf> listen sdg andh sadf say. ",
				new CheckEnglish(), null, null);
	}

	public String ChechWord(int fid, int sentid, String sentence,
			CheckLanguage lang, JSONObject obj, BasicOutputCollector collector) {
		if (lang == null || lang.GetName() != LANGUAGE_NAME.ENGLISH) {
			return sentence;
		}

		ArrayList<Word> list = lang.AnalyseWord(sentence);
		TreeMap<Integer, Word> checkmap = new TreeMap<Integer, Word>();
		for (Word word : list) {
			if (word.isAbled) {
				if (word.word.length() > 1) {
					checkmap.put(word.wordindex, word);
				}
			}
		}

		SentCheckWordDAO dao = null;
		try {
			StringBuffer sbKeyword = new StringBuffer();
			Set<Integer> set = checkmap.keySet();
			for (Integer key : set) {
				Word word = checkmap.get(key);
				sbKeyword.append(fid).append('\t').append(sentid).append('\t')
						.append(word.word.replaceAll("\\t|\\r|\\n", " "))
						.append('\t').append(word.wordindex).append('\t')
						.append(word.charindex).append('\n');
			}

			LoadDataInfile infile = new LoadDataInfile();
			String filePath = WiitransConfig.getInstance(0).FRAG.BUNDLE_TEMPFILE_PATH
					+ "fid" + fid + "_sentid" + sentid + "datafile.txt";
			infile.CreateFile(filePath);
			infile.WriteAppend(filePath, sbKeyword.toString());

			dao = new SentCheckWordDAO();
			dao.Init();
			dao.DeleteSentCheckWord(fid, sentid);
			dao.ImportSentCheckWord(filePath);
			dao.Commit();

			ArrayList<SentCheckWordBean> beanlist = dao.CheckVariation(fid,
					sentid);
			// 去掉有效变型
			for (SentCheckWordBean sentCheckWordBean : beanlist) {
				Integer key = sentCheckWordBean.index_word;
				if (checkmap.containsKey(key)) {
					checkmap.remove(key);
				}
			}

			// 去掉有效原型
			beanlist = dao.Check(fid, sentid);
			for (SentCheckWordBean sentCheckWordBean : beanlist) {
				Integer key = sentCheckWordBean.index_word;
				if (checkmap.containsKey(key)) {
					checkmap.remove(key);
				}
			}
			// 去掉符号
			beanlist = dao.CheckSymbol(fid, sentid);
			for (SentCheckWordBean sentCheckWordBean : beanlist) {
				Integer key = sentCheckWordBean.index_word;
				if (checkmap.containsKey(key)) {
					checkmap.remove(key);
				}
			}

			if (checkmap != null && checkmap.size() > 0) {

				StringBuilder sb = new StringBuilder();
				sb.append(sentence);

				StringBuffer sbUnknown = new StringBuffer();
				JSONObject learn = new JSONObject();
				// 到序取key
				Set<Integer> wordset = checkmap.descendingKeySet();
				for (Integer key : wordset) { 
					Word word = checkmap.get(key);
					learn.put(word.word, word.word);
					sb.insert(word.charindex + word.word.length(), "┫");
					sb.insert(word.charindex, "┣");

					sbUnknown.append(word.word.replaceAll("\\t|\\r|\\n", " "))
							.append('\n');
				}
				obj.put("learn", learn);
				collector.emit(new Values(obj.toString()));

				LoadDataInfile infile1 = new LoadDataInfile();
				String filePath1 = WiitransConfig.getInstance(0).FRAG.BUNDLE_TEMPFILE_PATH
						+ "unknown_fid"
						+ fid
						+ "_sentid"
						+ sentid
						+ "datafile.txt";
				infile1.CreateFile(filePath1);
				infile1.WriteAppend(filePath1, sbUnknown.toString());

				// dao = new SentCheckWordDAO();
				// dao.Init();
				dao.ImportUnknown(filePath1);
				dao.Commit();

				return sb.toString();
			} else {
				return sentence;
			}

		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (dao != null) {
				dao.UnInit();
			}
		}

		return null;
	}
}
