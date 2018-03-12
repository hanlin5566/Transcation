package com.wiitrans.base.misc;

import java.util.ArrayList;
import java.util.List;

import com.wiitrans.base.db.SentCheckWordDAO;
import com.wiitrans.base.db.VariationCheckWord;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.lang.CheckEnglish;
import com.wiitrans.base.file.lang.English;
import com.wiitrans.base.file.lang.Language;
import com.wiitrans.base.file.lang.Word;
import com.wiitrans.base.http.DownloadURLFile;
import com.wiitrans.base.xml.WiitransConfig;

public class UtilTest {
	public static void main(String[] args) {
		WiitransConfig.getInstance(0);

		DownloadURLFile
				.downloadFromUrl(
						"http://fsbjweb.eciol.com/DownloadWeb.ashx?fileParam=4775224FA77BC463B3DFFC985DEABCCDAE6BA6E04FE5E5C0BAC77067B6B1469373B981FF629327FAF01227E57960B470A05A1812155D5A725F0DB1D8C8EC190E336EE5C97E1EE99A33F7517879EFF89D3BEF949F14F971D6C3EA51EC1D84DA8F",
						"/root/Desktop/downloadforurl/");

		SentCheckWordDAO dao1 = new SentCheckWordDAO();
		dao1.Init();
		dao1.InsertWord("ebola");
		dao1.Commit();

		List<VariationCheckWord> varlist = new ArrayList<VariationCheckWord>();

		VariationCheckWord variation = null;
		variation = new VariationCheckWord();
		variation.word = "a123";
		variation.variation = "b456";
		variation.type = 9;
		varlist.add(variation);
		variation = new VariationCheckWord();
		variation.word = "c123";
		variation.variation = "d456";
		variation.type = 11;
		varlist.add(variation);
		SentCheckWordDAO dao = new SentCheckWordDAO();
		dao.Init();
		dao.InsertVariation(varlist);
		dao.Commit();
	}

	public static void main1212(String[] args) {
		WiitransConfig.getInstance(0);

		CheckEnglish en = new CheckEnglish();

		String text = "aaa aslfh lask2jg 3asgkj aslgkdh4 ;ljsagh 'asgh  aslg'kj  lksgssdlfk' ad;sl asdg;h 4uh asljajsb abasdklfhasdlkgjhsakjghasdskfuhgasdkjlfaskdfj";
		ArrayList<Word> list = en.AnalyseWord(text);
		for (Word word : list) {
			System.out.print(word.word);
			System.out.print(' ');
			System.out.print(word.wordindex);
			System.out.print(' ');
			System.out.print(word.charindex);
			System.out.print(' ');
			System.out.print(word.isAbled);
			System.out.print(' ');
			System.out.println(text.substring(word.charindex, word.charindex
					+ word.word.length()));
		}

		Language enus = new English();
		// String aaa = enus.Encode((char) 127);
		char aa = '·';
		System.out.println((int) aa);
		String a = enus.Decode("");
		System.out.println(enus.Decode("{0}"));
		System.out.println(enus.Decode("{12}"));
		System.out.println(enus.Decode("{345}"));
		System.out.println(enus.Decode("{6789}"));
		System.out.println(enus.Decode("{12345}"));
		System.out.println(enus.Decode("1{12288}2"));
		System.out.println(enus.Decode("11{23}}2. 2 "));
		System.out.println(enus.Decode(""));
	}

	public static void main2(String[] args) {
		WiitransConfig.getInstance(0);
		int count = Util.CheckTermCount("asdfasasdfas", "as", 5);
		System.out.println();
	}

	public static void main_(String[] args) {
		int count = new FileUtil()
				.GetTagCount(
						"In☂{___:2}☂ response☂{___:2}☂ to the stock☂{___:2}☂ market☂{___:2}☂.{___:13}",
						"☂");
		System.out.println(count);
	}

}
