package com.wiitrans.base.tm;

import java.util.ArrayList;
import java.util.Collections;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;

public class DetectTMLanguage {
	public static TMLanguage Detect(LANGUAGE_COUNTRY name) {
		// DEDE, ENUS, ESES, FRFR, RURU, JAJP, ZHCN, ZHTW, KOKR, DEAT, DECH,
		// DELU, ENAU, ENCA, ENGB, ENIN, ENSG, ESMX, ESUS, FRCA, FRCH, FRLU,
		// ZHHK, ZHSG
		TMLanguage lang = null;
		switch (name) {
		case DEDE:
			lang = new TMDEDE();
			break;
		case ENUS:
			lang = new TMENUS();
			break;
		case ESES:
			lang = new TMESES();
			break;
		case FRFR:
			lang = new TMFRFR();
			break;
		case RURU:
			lang = new TMRURU();
			break;
		case ITIT:
			lang = new TMITIT();
			break;
		case JAJP:
			lang = new TMJAJP();
			break;
		case ZHCN:
			lang = new TMZHCN();
			break;
		case ZHTW:
			lang = new TMZHTW();
			break;
		case KOKR:
			lang = new TMKOKR();
			break;
		default:
			break;
		}
		return lang;
	}

	public static TMLanguage Detect(String text) {
		// TODO
		TMLanguage lang = null;
		switch (text.toLowerCase().replace("-", "")) {
		case "dede":
			lang = new TMDEDE();
			break;
		case "enus":
			lang = new TMENUS();
			break;
		case "eses":
			lang = new TMESES();
			break;
		case "frfr":
			lang = new TMFRFR();
			break;
		case "ruru":
			lang = new TMRURU();
			break;
		case "itit":
			lang = new TMITIT();
			break;
		case "ja":
			lang = new TMJAJP();
			break;
		case "jajp":
			lang = new TMJAJP();
			break;
		case "zhcn":
			lang = new TMZHCN();
			break;
		case "zhtw":
			lang = new TMZHTW();
			break;
		case "kokr":
			lang = new TMKOKR();
			break;
		default:
			break;
		}
		return lang;
	}

	public static void TMResultSortDistanceAsc(ArrayList<TMResult> list) {
		Collections.sort(list, new TMResultSortSimilarityAsc());
	}

	public static void TMResultSortDistanceDesc(ArrayList<TMResult> list) {
		Collections.sort(list, new TMResultSortSimilarityDesc());
	}
}
