package com.wiitrans.base.file.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.log.Log4j;

public class DetectLanguage {
	public Language Detect(LANGUAGE_NAME name) {
		// TODO
		Language lang = null;
		switch (name) {
		case CHINESE:
			lang = new Chinese();
			break;
		case ENGLISH:
			lang = new English();
			break;
		case KOREAN:
			lang = new Korean();
			break;
		default:
			break;
		}
		return lang;
	}

	public Language Detect(String text) {
		// TODO
		Language lang = null;
		switch (text.toLowerCase()) {
		case "chinese":
			lang = new Chinese();
			break;
		case "chs":
			lang = new Chinese();
			break;
		case "zh-cn":
			lang = new Chinese();
			break;
		case "english":
			lang = new English();
			break;
		case "en":
			lang = new English();
			break;
		case "en-us":
			lang = new English();
			break;
		case "korean":
			lang = new Korean();
			break;
		case "ko":
			lang = new Korean();
			break;
		case "ko-kr":
			lang = new Korean();
			break;
		default:
			break;
		}
		return lang;
	}

	public static void main_(String[] args) {

		Language lang = new Chinese();
		String text = "";
		try {
			File file = new File("/root/Desktop/新建 文本文档 (1).txt");
			long len = file.length();
			if (len < 300000000) {
				byte[] tempbytes = new byte[(int) len];
				int byteread = 0;
				InputStream inStream = new FileInputStream(file);
				while ((byteread = inStream.read(tempbytes)) != -1) {
					text = new String(tempbytes);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error(e);
		}
		ArrayList<LangSentence> sentences = lang.AnalyseSentence(text);
		for (LangSentence sentence : sentences) {
			if (sentence.valid) {
				System.out.println("++++++++++++++++++++++++++++++++++++");
				System.out.println(sentence.text.trim());
				ArrayList<Word> words = lang.AnalyseWord(sentence.text);
				System.out.println("************************************");
				for (Word word : words) {
					System.out.print(word.word);
				}

				System.out.println(words.size());
				System.out.println("-------------------------------------");
			}
		}
		int a = 12;
	}

	public static void main(String[] args) {

		Language lang1 = new English();

		ArrayList<LangSentence> list = lang1
				.AnalyseText("You will  also see an updated symbol \n&lt;a href=&quot;http://www.163.com;&quot;/&gt; set, a symbol set designed to be overlaid on imagery,topographic, and parcel basemaps. &nbsp; The editing map document was updated to show you have to set up feature templates. ");

		// String sentence =
		// "You will  also see an updated symbol &lt;a href=&quot;http://www.163.com;&quot;/&gt; set, a symbol set designed to be overlaid on imagery,topographic, and parcel basemaps. &nbsp; The editing map document was updated to show you have to set up feature templates. ";
		// String text = "n\r PO list item /_ .a asdf!sadf?asgd!\nasfd";
		String text = "nach dem londoner groß­brand von 1666 war hooke als ver­messer und architekt maßgeblich am";
		ArrayList<LangSentence> sentences = lang1.AnalyseSentence(text);
		for (LangSentence sentence : sentences) {
			if (sentence.valid) {
				ArrayList<Word> aa = lang1.AnalyseWord(sentence.text);
				int count = aa.size();
				for (Word word : aa) {
					System.out.println(word.word
							+ " "
							+ word.charindex
							+ " "
							+ sentence.text.substring(word.charindex,
									word.charindex + word.word.length()));

				}
			}
		}

		// Language lang = new English();

	}

	public static void main__(String[] args) {

		// TODO Auto-generated method stub
		Language lang1 = new Chinese();
		// LANGUAGE_NAME name = lang.GetName();
		// LANGUAGE_TYPE type = lang.GetType();
		ArrayList<Word> aa = lang1
				.AnalyseWord("독일독일 연방 공화국(獨逸聯邦共和國, 독일어: Bundesrepublik Deutschland 분데스레푸블리크 도이칠란트[*]) 또는 독일(獨逸, 독일어: Deutschland 도이칠란트[*] 듣기 (도움말•정보), 문화어: 도이췰란드)은 중앙 유럽에 있는 나라이다. 북쪽으로 덴마크와 북해, 발트 해, 동쪽으로 폴란드와 체코, 남쪽으로 오스트리아와 스위스, 서쪽으로 프랑스, 룩셈부르크, 벨기에, 네덜란드와 국경을 맞대고 있다. 독일 영토는 357,021 제곱킬로미터이며, 기후는 주로 온대 기후를 보인다. 인구는 2010년 1월 기준으로 8,180만여 명 이상으로[1] 유럽 연합에서 인구가 가장 많은 나라이며, 이민자 인구가 전 세계에서 세 번째로 많다.[2] 공용어는 독일어이고, 소수 민족어나 러시아어, 영어, 소르브어, 덴마크어, 프리지아어도 쓰인다.");
		int count = aa.size();
		for (Word word : aa) {
			System.out.print(word.word);
		}
		Language lang = new English();
		String text = "";
		try {
			// BufferedReader br = new BufferedReader(new FileReader(
			// "/root/Desktop/ebola.txt"));
			// String line = "";
			// StringBuilder buffer = new StringBuilder();
			// while ((line = br.readLine()) != null) {
			// buffer.append(line);
			// }
			// text = buffer.toString();

			File file = new File("/root/Desktop/ebola (copy).txt");
			long len = file.length();
			if (len < 300000000) {
				byte[] tempbytes = new byte[(int) len];
				int byteread = 0;
				InputStream inStream = new FileInputStream(file);
				while ((byteread = inStream.read(tempbytes)) != -1) {
					// System.out.write(tempbytes, 0, byteread);
					text = new String(tempbytes);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log4j.error(e);
		}
		ArrayList<LangSentence> sentences = lang.AnalyseSentence(text);
		for (LangSentence sentence : sentences) {
			System.out.println("++++++++++++++++++++++++++++++++++++");
			System.out.println(sentence);
			ArrayList<Word> words = lang.AnalyseWord(sentence.text);
			System.out.println("************************************");
			for (Word word : words) {
				System.out.print(word.word);
				System.out.print(" ");
			}

			System.out.println(words.size());
			System.out.println("-------------------------------------");
		}
		int a = 12;
	}
}
