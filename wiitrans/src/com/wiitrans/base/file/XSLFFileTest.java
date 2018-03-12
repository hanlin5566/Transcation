package com.wiitrans.base.file;

import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.notag.XSLFFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class XSLFFileTest {

	public static void main(String[] args) {
//		AppConfig app = new AppConfig();
//		app.Parse();
		WiitransConfig.getInstance(0);

		XSLFFileNoTag file = new XSLFFileNoTag();
		// file._tagId = "☂";
		file.Init("/root/Desktop/file/en-anim-0729(1).pptx",
				"/root/Desktop/file/en-anim-0729(1)_cleanup.pptx",
				"/root/Desktop/file/en-anim-0729(1).pptx.xml",
				"en-anim-0729(1).pptx", new English(), new Chinese());

		// file.Init("/root/Desktop/ebola.doc", "/root/Desktop/ebola-hwpf.doc",
		// "/root/Desktop/test.xml", new English(), new Chinese());

		// 解析源文件
		file.Parse();

		// for (Fragmentation frag : file._entityFrags) {
		// for (Sentence sent : frag._sentences) {
		// sent._state = SENTENCE_STATE.T;
		// // sent._translate = sent._source + "--------------------";
		// sent._translate = "(    " + sent._entityFragIndex + ","
		// + sent._entitySentenceIndex + "    )";
		// }
		// }

		// 保存双语文件
		file.Save();

		// 解析双语文件
		file.ParseBili();

		// file.Save();
		// VirtualFragmentation vFrag = file._virtualFrags.get(0);
		// HashMap<String, Sentence> sentences = vFrag._sentencesMap;
		// Sentence sentence = sentences.get("3");
		// sentence._translate = "asdfasdfa";

		// file.Save();

		// 生成目标文件
		file.Cleanup(SENTENCE_STATE.E);
	}
}