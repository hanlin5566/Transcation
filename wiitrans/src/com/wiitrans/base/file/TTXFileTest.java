package com.wiitrans.base.file;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.file.ttx.TTXFileNoTag;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class TTXFileTest {
	public static void main(String[] args) {
//	    String str = "<anchor id=\"2050\"/><cf font=\"Arial\" size=\"9\" complexscriptsfont=\"Arial\" complexscriptssize=\"9\" fontcolour=\"0x555555\">当今的制造企业必须更迅速地响应不断变化的市场和运营环境，同时又不能牺牲效率。";
//	    String str = "<anchor id=\"2050\"/><cf font=\"Arial\" size=\"9\" complexscriptsfont=\"Arial\" complexscriptssize=\"9\" fontcolour=\"0x555555\">Today's manufacturing companies must become more responsive to changing market and operational conditions without sacrificing efficiency.";
//	    Pattern tagPattern = Pattern.compile("\\<.*?\\>");
//		Matcher tagMatch = tagPattern.matcher(str);
//		//匹配到标签
//		if(tagMatch.matches()){
//		    System.out.println("match");
//		}
//	    String str = "<anchor id=\"2050\"/><cf font=\"Arial\" size=\"9\" complexscriptsfont=\"Arial\" complexscriptssize=\"9\" fontcolour=\"0x555555\">当今的制造企业必须更迅速地响应不断变化的市场和运营环境，同时又不能牺牲效率。";
//	    Pattern tagPattern = Pattern.compile("\\<[^\\>]*\\>");
//	    Matcher tagMatch = tagPattern.matcher(str);
//	    if(Pattern.matches(".*?\\<[^\\>]*\\>.*?", str)){
//		 //匹配到标签
//	        int pre_end = 0;
//	        while(tagMatch.find()){
//	    	int s = tagMatch.start();
//	    	int e = tagMatch.end();
//	    	if(s != pre_end){
//	    	    //中间内容
//	    	    String content = str.substring(pre_end, s); 
//		    System.out.println(content);
//	    	}
//	    	String tag = str.substring(s, e);
//					System.out.println(tag);
//	    	pre_end = e;
//	        }
//	        //补尾
//	        if(pre_end < str.length()){
//	    	String endContent = str.substring(pre_end, str.length());
//					System.out.println(endContent);
//	        }
//	    }
	    
//		AppConfig app = new AppConfig();
//		app.Parse();
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
		// System.out.println(Util.clearTag(str));
		/**
		 * test parse
		 */
		TTXFileNoTag ttxFile = new TTXFileNoTag();
		String fileName = "Copy guidelines, voice and tone_April16.docx";
		ttxFile.Init("/opt/doc/" + fileName + ".ttx", "/opt/doc/file/"
				+ fileName + "_cleanup.ttx", "/opt/doc/file/" + fileName
				+ ".xml", fileName + ".ttx",new English() , new Chinese());
		// 解析源文件
		ttxFile.Parse();
		// 保存双语文件
		ttxFile.Save();
		// 解析双语文件
		ttxFile.ParseBili();
		// 生成目标文件
		ttxFile.Cleanup(SENTENCE_STATE.E);

		/**
		 * test collectTag
		 */
		// File file = new File("/opt/doc/ttx_");
		// TTXFileTest test = new TTXFileTest();
		// test.collectTag(file);
		// Set<String> set = Util.tagSet;
		// for (String tag : set) {
		// System.err.println(tag);
		// }
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
			if ("ttx".equals(ext)) {
				// System.out.println("parse "+filePath);
				TTXFileNoTag ttxFile = new TTXFileNoTag();
				ttxFile.Init(filePath, "/opt/doc/file/" + file.getName()
						+ "_cleanup.ttx", "/opt/doc/file/" + file.getName()
						+ "_cleanup.xml", filePath, new English(),
						new Chinese());
				// 解析源文件
				ttxFile.Parse();
				// 保存双语文件
				ttxFile.Save();
				// 解析双语文件
				ttxFile.ParseBili();
				// 生成目标文件
				ttxFile.Cleanup(SENTENCE_STATE.E);
			} else {
				System.out.println("not ttx file" + filePath);
			}
		}
	}
}
