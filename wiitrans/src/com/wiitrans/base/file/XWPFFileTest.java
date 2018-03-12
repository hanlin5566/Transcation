package com.wiitrans.base.file;

//import com.sun.xml.internal.ws.api.pipe.Engine;
//import com.wiitrans.base.file.FileConst.FILE_LANGUAGE;
import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.notag.XWPFFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class XWPFFileTest {

	public static void main(String[] args) {
//		AppConfig app = new AppConfig();
//		app.Parse();

		WiitransConfig.getInstance(0);
		XWPFFileNoTag file = new XWPFFileNoTag();
		// file._tagId = "☂";
		file.Init("/root/Desktop/file/123.docx",
				"/root/Desktop/file/123_cleanup.docx",
				"/root/Desktop/file/123.docx.xml",
				"123.docx", new English(), new Chinese());

		// file.Init("/root/Desktop/file/yejiao.docx",
		// "/root/Desktop/file/yejiao_cleanup.docx",
		// "/root/Desktop/file/yejiao.xml", new English(),
		// new Chinese(), SENTENCE_STATE.T);
		file.Parse();

		// for (Fragmentation frag : file._entityFrags) {
		// for (Sentence sent : frag._sentences) {
		// sent._state = SENTENCE_STATE.T;
		// sent._translate = sent._source + "--------------------";
		// }
		// }

		 file.Save();

		file.ParseBili();

		file.Cleanup(SENTENCE_STATE.E);
	}
}
