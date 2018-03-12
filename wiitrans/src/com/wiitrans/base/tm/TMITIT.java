package com.wiitrans.base.tm;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class TMITIT extends TMENUS {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.ITALIAN;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.ITIT;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_ITIT;
	private long _bit60 = 1152921504606846976L;

	@Override
	public LANGUAGE_NAME GetLanguageName() {
		return _name;
	}

	@Override
	public LANGUAGE_TYPE GetLanguageType() {
		return _type;
	}

	@Override
	public String GetLanguageCountryName() {
		return _slangcountry;
	}

	@Override
	public LANGUAGE_COUNTRY GetLanguageCountry() {
		return _langcountry;
	}

	@Override
	public int IsLetter(char c) {
		// 1小写2大写3数字0非字母
		int result;
		if (c >= 'a' && c <= 'z' || c == 'à' || c == 'è' || c == 'é'
				|| c == 'ì' || c == 'í' || c == 'î' || c == 'ò' || c == 'ó'
				|| c == 'ù' || c == 'ú') {
			result = 1;
		} else if (c >= 'A' && c <= 'Z') {
			result = 2;
		} else if (c >= '0' && c <= '9') {
			result = 3;
		} else if (c == '-' || c == '\'') {
			result = 4;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public String GetWordByHansonCode(long wordID) {
		String word = "";

		ArrayList<Short> letters = new ArrayList<Short>();

		if (wordID > 0L && wordID < _bit60) {
			letters.add((short) ((wordID & 1134907106097364992L) / 18014398509481984L));
			letters.add((short) ((wordID & 17732923532771328L) / 281474976710656L));
			letters.add((short) ((wordID & 277076930199552L) / 4398046511104L));
			letters.add((short) ((wordID & 4329327034368L) / 68719476736L));
			letters.add((short) ((wordID & 67645734912L) / 1073741824L));
			letters.add((short) ((wordID & 1056964608L) / 16777216L));
			letters.add((short) ((wordID & 16515072L) / 262144L));
			letters.add((short) ((wordID & 258048L) / 4096L));
			letters.add((short) ((wordID & 4032L) / 64L));
			letters.add((short) (wordID & 63L));

			for (short letter : letters) {
				if (letter >= 0 && letter <= 9) {
					word += (char) (letter + '0');
				} else if (letter >= 10 && letter <= 35) {
					word += (char) (letter + 'a' - 10);
				} else if (letter == 36) {
					word += 'à';
				} else if (letter == 37) {
					word += 'è';
				} else if (letter == 38) {
					word += 'é';
				} else if (letter == 39) {
					word += 'ì';
				} else if (letter == 40) {
					word += 'í';
				} else if (letter == 41) {
					word += 'î';
				} else if (letter == 42) {
					word += 'ò';
				} else if (letter == 43) {
					word += 'ó';
				} else if (letter == 44) {
					word += 'ù';
				} else if (letter == 45) {
					word += 'ú';
				} else if (letter == 61) {
					word += '-';
				} else if (letter == 62) {
					word += '\'';
				} else {
					word += " ";
				}
			}
		}

		return word;
	}

	@Override
	public long GetHansonCodeByWord(String word) {
		long result = 0;
		if (word.length() <= 10) {
			// word = word.PadRight(10, '-');
			word = word.concat("----------").substring(0, 10);

			char[] cs = word.toCharArray();

			long[] letters = new long[cs.length];

			for (int i = 0; i < cs.length; i++) {
				if (cs[i] >= 'a' && cs[i] <= 'z') {
					letters[i] = cs[i] - 'a' + 10;
				} else if (cs[i] >= 'A' && cs[i] <= 'Z') {
					letters[i] = cs[i] - 'A' + 10;
				} else if (cs[i] >= '0' && cs[i] <= '9') {
					letters[i] = cs[i] - '0';
				} else if (cs[i] == 'à') {
					letters[i] = 36;
				} else if (cs[i] == 'è') {
					letters[i] = 37;
				} else if (cs[i] == 'é') {
					letters[i] = 38;
				} else if (cs[i] == 'ì') {
					letters[i] = 39;
				} else if (cs[i] == 'í') {
					letters[i] = 40;
				} else if (cs[i] == 'î') {
					letters[i] = 41;
				} else if (cs[i] == 'ò') {
					letters[i] = 42;
				} else if (cs[i] == 'ó') {
					letters[i] = 43;
				} else if (cs[i] == 'ù') {
					letters[i] = 44;
				} else if (cs[i] == 'ú') {
					letters[i] = 45;
				} else if (cs[i] == '-') {
					letters[i] = 61;
				} else if (cs[i] == '\'') {
					letters[i] = 62;
				} else {
					letters[i] = 63;
				}
			}

			result += letters[9] * 1L;
			result += letters[8] * 64L;
			result += letters[7] * 4096L;
			result += letters[6] * 262144L;
			result += letters[5] * 16777216L;
			result += letters[4] * 1073741824L;
			result += letters[3] * 68719476736L;
			result += letters[2] * 4398046511104L;
			result += letters[1] * 281474976710656L;
			result += letters[0] * 18014398509481984L;
		} else {
			// long bit60 = 1152921504606846976L;

			char[] cs = word.toLowerCase().toCharArray();

			// unchecked
			// {
			for (char item : cs) {
				result = result * 31 + item;
			}
			// }

			result = result % _bit60;

			if (result < 0) {
				result = result + _bit60 + _bit60;
			} else {
				result = result + _bit60;
			}
		}

		return result;
	}

}
