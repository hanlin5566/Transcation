package com.wiitrans.base.file;

import java.io.File;
import java.util.ArrayList;

import com.wiitrans.base.file.lang.Chinese;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.tm.TMENUS;
import com.wiitrans.base.tm.TMZHCN;
import com.wiitrans.base.xml.AppConfig;
import com.wiitrans.base.xml.WiitransConfig;

public class TmxFileTest {
	public static void main(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);
		try {
			File source = new File("/root/Desktop/tmsvr/65.tmx");
			File target = new File("/root/Desktop/tmsvr/65-1.tmx");
			FileUtil.copyFileForTxt(source, target, "utf-8");
		} catch (Exception e) {
			Log4j.error(e);
		}

	}

	public static void main_(String[] args) {
		// AppConfig app = new AppConfig();
		// app.Parse();
		WiitransConfig.getInstance(0);

		BiliFileNoTag file = new BiliFileNoTag();
		file.Init(null, null, "/root/Desktop/file/3026.xml", "3026.ppt",
				new English(), new Chinese());
		file.ParseBili();

		TmxFile tmxFile = new TmxFile();
		tmxFile.Init(file, "/root/Desktop/file/3026.tmx", new TMENUS(),
				new TMZHCN());
		tmxFile.Parse();
		tmxFile.WriteTMXFile();

		// TmxFile tmxFile = new TmxFile();
		// // tmxFile.Init(null, "/root/Desktop/file/1.4b_4.tmx", "", "");
		// tmxFile.Init(null, "/root/Desktop/tmsearchfile/fr-cn1.tmx",
		// "", "");
		// tmxFile.ReadTMXFile();
		// // tmxFile.WriteTMXFile();
		//
		// TMENUS tmenus = new TMENUS();
		//
		// ArrayList<TMWord> list = tmenus
		// .AnalyseWord(" ----assd  aag a43sdf bsaed 0");
		//
		// tmenus.Init(tmxFile, true);
		// tmenus.Parse(11);
		// System.out.println("");

	}
}
