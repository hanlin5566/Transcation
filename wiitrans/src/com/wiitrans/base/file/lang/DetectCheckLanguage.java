package com.wiitrans.base.file.lang;

public class DetectCheckLanguage {
	public static CheckLanguage Detect(Language lang) {
		if (lang == null) {
			return null;
		}
		CheckLanguage resultlang = null;
		switch (lang.GetName()) {
		case ENGLISH:
			resultlang = new CheckEnglish();
			break;
		default:
			break;
		}
		return resultlang;
	}

	public static CheckLanguage Detect(String text) {
		CheckLanguage lang = null;
		switch (text.toLowerCase()) {
		case "english":
			lang = new CheckEnglish();
			break;
		case "en":
			lang = new CheckEnglish();
			break;
		case "en-us":
			lang = new CheckEnglish();
			break;
		default:
			break;
		}
		return lang;
	}
}
