package com.wiitrans.base.tm;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.lang.MakeWordTree;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.Word;
import com.wiitrans.base.xml.WiitransConfig;

public class LanguageTest {
	public static void main(String[] args) {
		WiitransConfig app = WiitransConfig.getInstance(0);

		// String abc = "asdf<dsfr gre hewtg < th rth > h,. >asdf<as>df>";
		// String abc = "观自在<菩<萨，行<深般>若波罗>密,<,,多时；照见五蕴<皆空>度一>切苦厄。";
		String abc = "<csf name=\"g\" tagid=\"pt1\"><cf name=\"g\" tagid=\"pt2\">dear colleagues,</cf tagid=\"pt2\"> </csf tagid=\"pt1\"><ent/> <ent/> <csf name=\"g\" tagid=\"pt5\"><cf name=\"g\" tagid=\"pt6\">keeping information confidential and secure starts with you!</cf tagid=\"pt6\"></csf tagid=\"pt5\">";
		int index = abc.indexOf('<', 5);
		ArrayList<int[]> list = new English().CheckTagRange(abc);
		for (int[] is : list) {
			System.out.println(abc.substring(is[0], is[1] + 1));
		}

		ArrayList<Word> aaa = new English().AnalyseWord(abc);
		System.out.println();
	}

	public static void main11231231(String[] args) {
		WiitransConfig app = WiitransConfig.getInstance(0);
		// String filename = "1.4b_4.tmx";
		String filename = "1521.tmx";
		TmxFile tmxFile = new TmxFile();

		tmxFile.Init(null, "/root/Desktop/tmsearchfile/" + filename, "", "");

		tmxFile.ReadTMXFile();
		tmxFile.Init(null, "/root/Desktop/tmsearchfile/123.tmx",
				tmxFile._sourceLang, tmxFile._targetLang);
		tmxFile.WriteTMXFile();

		TMZHCN tmzhcn = new TMZHCN();

		// ArrayList<TMWord> list = tmzhcn
		// .AnalyseWord(" ----assd  aag a43sdf bsaed 0");

		tmzhcn.Init(tmxFile, true);
		// tmzhcn.Parse(2);
		// tmzhcn.WriteTMText(2);
		// tmzhcn.WriteTM(2);
		tmzhcn.ReadTM(2);

		System.out.println("");
		tmzhcn.SearchTM("Incorrect");
		tmzhcn.SearchTM("Configuration Ship");
		tmzhcn.SearchTM("Incorrect Basis of Estimate Edit Non-Standard Configuration Ship To Contact Email");
		tmzhcn.SearchTM("Edit Non-Standard Configuration ");
	}

	public static void main__(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		// String filename = "1.4b_4.tmx";
		String filename = "4 Propel 2015_Prj TM_CN.tmx";
		TmxFile tmxFile = new TmxFile();

		tmxFile.Init(null, "/root/Desktop/tmsearchfile/" + filename, "", "");

		tmxFile.ReadTMXFile();
		tmxFile.Init(null, "/root/Desktop/tmsearchfile/123.tmx",
				tmxFile._sourceLang, tmxFile._targetLang);
		tmxFile.WriteTMXFile();

		TMENUS tmenus = new TMENUS();

		ArrayList<TMWord> list = tmenus
				.AnalyseWord(" ----assd  aag a43sdf bsaed 0");

		tmenus.Init(tmxFile, true);
		tmenus.Parse(2);

		// tmenus.WriteTM(2);

		System.out.println("");
		tmenus.SearchTM("Incorrect");
		tmenus.SearchTM("Configuration Ship");
		tmenus.SearchTM("Incorrect Basis of Estimate Edit Non-Standard Configuration Ship To Contact Email");
		tmenus.SearchTM("Edit Non-Standard Configuration ");
	}

	public static void main_(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		Levenshtein l = new Levenshtein();
		long[] aaa = { 1, 2, 3, 4, 5 };
		long[] bbb = { 1, 2, 3, 5, 4 };
		long[] ccc = { 1, 6, 3, 4, 5 };
		System.out.print(l.Distance(aaa, bbb));
		System.out.print(l.Distance(aaa, ccc));
	}
}
