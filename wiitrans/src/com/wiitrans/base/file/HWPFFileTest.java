package com.wiitrans.base.file;

import java.util.HashMap;

import com.wiitrans.base.file.frag.VirtualFragmentation;
import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.notag.HWPFFileNoTag;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class HWPFFileTest {

	public static void main(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		HWPFFileNoTag file = new HWPFFileNoTag();
		// file._tagId = "☂";
		file.Init("/root/Desktop/file/测试.doc",
				"/root/Desktop/file/测试_cleanup.doc",
				"/root/Desktop/file/测试.doc.xml", "测试.doc", new Chinese(),
				new English());
		//
		// file.Init("/root/Desktop/file/西班牙语-带艺术字-doc.doc",
		// "/root/Desktop/file/西班牙语-带艺术字-doc_cleanup.doc",
		// "/root/Desktop/file/西班牙语-带艺术字-doc.xml", new English(),
		// new Chinese(), SENTENCE_STATE.T);
		// file.Init("/root/Desktop/file/俄语-带公式-doc.doc",
		// "/root/Desktop/file/俄语-带公式-doc_cleanup.doc",
		// "/root/Desktop/file/俄语-带公式-doc.xml", new English(), new Chinese(),
		// SENTENCE_STATE.T);
		// file.Init("/root/Desktop/file/test (1).doc",
		// "/root/Desktop/file/test (1)_cleanup.doc",
		// "/root/Desktop/file/test (1).xml", new Chinese(), new English(),
		// SENTENCE_STATE.T);

		// 解析源文件
		// file.Parse();

		// for (Fragmentation frag : file._entityFrags) {
		// for (Sentence sent : frag._sentences) {
		// sent._state = SENTENCE_STATE.T;
		// // sent._translate = sent._source + "--------------------";
		// sent._translate = "(    " + sent._entityFragIndex + ","
		// + sent._entitySentenceIndex + "    )";
		// }
		// }

		// 保存双语文件

		// file.Save();

		// 解析双语文件
		file.ParseBili();

		// file.Save();
		VirtualFragmentation vFrag = file._virtualFrags.get(0);
		HashMap<String, Sentence> sentences = vFrag._sentencesMap;
		Sentence sentence = sentences.get("0");
		sentence._sourceDigit = sentence._sourceDigit + "1";
		sentence._targetDigit = sentence._targetDigit + "2";
		file.Save();

		// for (VirtualFragmentation vFrag : file._virtualFrags) {
		// for (Sentence sent : vFrag._sentences) {
		// // sent._translate = sent._source + "";
		// if (sent._sourceWordCount > 0) {
		// sent._translate = "111111";
		// }
		// }
		// }

		// 生成目标文件
		file.Cleanup(SENTENCE_STATE.E);
	}
}
