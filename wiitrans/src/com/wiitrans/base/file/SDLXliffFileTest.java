package com.wiitrans.base.file;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.istack.internal.logging.Logger;
import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.sdlxliff.SDLXliffFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.file.ttx.TTXFileNoTag;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class SDLXliffFileTest {
	public static void main(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		/**
		 * test reg
		 */
		// String str =
		// "<csf style=\"Default Paragraph Font\">This BPP is applicable for <cf fontcolour=\"0x0\">ALL</cf> locations.</csf>";
		// Pattern pattern = Pattern.compile("\\<[^\\>]*\\>");
		// Matcher match = pattern.matcher(str);
		// while (match.find()) {
		// System.out.print(match.group());
		// }
		// Log4j.debug(Util.clearTag(str));
		/**
		 * test parse
		 */
		SDLXliffFileNoTag sdlFile = new SDLXliffFileNoTag();
		String fileName = "WaveA-July13-FINAL-COMMENTS_with_highlighs.docx";
		sdlFile.Init("/opt/sdlxliff/" + fileName + ".sdlxliff",
				"/opt/sdlxliff/file/" + fileName + "_cleanup.sdlxliff",
				"/opt/sdlxliff/file/" + fileName + ".xml", fileName
						

				, new English(), new Chinese());
		long s = System.currentTimeMillis();
		// 解析源文件
		sdlFile.Parse();
		Log4j.debug("parse:" + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// 保存双语文件
		sdlFile.Save();
		Log4j.debug("save:" + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// 解析双语文件
		sdlFile.ParseBili();
		Log4j.debug("ParseBili:" + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// 生成目标文件
		sdlFile.Cleanup(SENTENCE_STATE.E);
		Log4j.debug("Cleanup:" + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();

//		 File file = new File("/opt/sdlxliff/meqxliff.sdlxliff");
//		 SDLXliffFileTest test = new SDLXliffFileTest();
//		 test.collectTag(file);
	}

	private void collectTag(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				collectTag(files[i]);
			}
		} else {
			String filePath = file.getPath();
			String ext = filePath.substring(filePath.lastIndexOf('.') + 1,
					filePath.length());
			if ("sdlxliff".equals(ext)) {
				Log4j.debug("parse " + filePath);
				Log4j.error("filePath " + filePath);
				SDLXliffFileNoTag sdlFile = new SDLXliffFileNoTag();
				sdlFile.Init(
						filePath,
						"/opt/sdlxliff/file/" + file.getName()
								+ "_cleanup.sdlxliff",
						"/opt/sdlxliff/file/" + file.getName() + "_cleanup.xml",
						filePath, new English(), new Chinese());
				long s = System.currentTimeMillis();
				// 解析源文件
				sdlFile.Parse();
				if (System.currentTimeMillis() - s > 1000) {
					Log4j.error("parse:" + (System.currentTimeMillis() - s));
				}
				s = System.currentTimeMillis();
				// 保存双语文件
				sdlFile.Save();
				Log4j.debug("save:" + (System.currentTimeMillis() - s));
				s = System.currentTimeMillis();
				// 解析双语文件
				sdlFile.ParseBili();
				Log4j.debug("ParseBili:" + (System.currentTimeMillis() - s));
				s = System.currentTimeMillis();
				// 生成目标文件
				sdlFile.Cleanup(SENTENCE_STATE.E);
				Log4j.debug("Cleanup:" + (System.currentTimeMillis() - s));
				s = System.currentTimeMillis();
			} else {
				System.err.println("not sdlxliff file" + filePath);
			}
		}
	}
}
