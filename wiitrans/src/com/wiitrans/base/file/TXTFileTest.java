package com.wiitrans.base.file;

import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.notag.TXTFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class TXTFileTest {

	public static void main(String[] args) {
//		AppConfig app = new AppConfig();
//		app.Parse();
		WiitransConfig.getInstance(0);

		// char c = 1;
		// String s = "aa" + String.valueOf(c);
		// s = s + "bb";
		// try {
		// File f = new File("/root/Desktop/tmsearchfile/1-1.txt");
		// FileInputStream in = new FileInputStream(f);
		// String dc = Charset.defaultCharset().name();
		// UnicodeInputStream uin = new UnicodeInputStream(in, dc);
		// BufferedReader br = new BufferedReader(new InputStreamReader(uin));
		// String line = br.readLine();
		// while (line != null) {
		// System.out.println(line);
		// line = br.readLine();
		// }
		// } catch (Exception e) {
		// // TODO: handle exception
		// }

		// String a = new BiliFile().FilterText("\n\r\n", "\n\r",
		// FILTER_POSITION.EMPTY);

		// File file1 = new java.io.File("/root/Desktop/tmsearchfile/1-1.txt");
		TXTFileNoTag file = new TXTFileNoTag();
		// file._tagId = "☂";
		// file.Init("/root/Desktop/fenxi/ebola_UTF8.txt",
		// "/root/Desktop/fenxi/ebola_UTF8_cleanup.txt",
		// "/root/Desktop/fenxi/ebola_UTF8.xml", new Chinese(),
		// new English(), SENTENCE_STATE.T);
		file.Init("/root/Desktop/txtfile/unicodebigcleanup.txt",
				"/root/Desktop/txtfile/unicodebigcleanupcleanup.txt",
				"/root/Desktop/txtfile/unicodebigcleanup.xml",
				"unicodebigcleanup.txt", new Chinese(), new English());
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

		// HashMap<Long, ArrayList<int[]>> a = file.GetHashCodeMap();

		// file.Save();
		// VirtualFragmentation vFrag = file._virtualFrags.get(0);
		// HashMap<String, Sentence> sentences = vFrag._sentencesMap;
		// Sentence sentence = sentences.get("3");
		// sentence._translate = "asdfasdfa";

		// file.Save();

		// 生成目标文件
		file.Cleanup(SENTENCE_STATE.E);;
	}
}
