package com.wiitrans.base.tm;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class TMRURU extends TMENUS {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.RUSSIAN;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.RURU;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_RURU;
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
		if (c == 'а' || c == 'б' || c == 'в' || c == 'г' || c == 'д'
				|| c == 'е' || c == 'ё' || c == 'ж' || c == 'з' || c == 'и'
				|| c == 'й' || c == 'к' || c == 'л' || c == 'м' || c == 'н'
				|| c == 'о' || c == 'п' || c == 'р' || c == 'с' || c == 'т'
				|| c == 'у' || c == 'ф' || c == 'х' || c == 'ц' || c == 'ч'
				|| c == 'ш' || c == 'щ' || c == 'ъ' || c == 'ы' || c == 'ь'
				|| c == 'э' || c == 'ю' || c == 'я') {
			result = 1;
		} else if (c == 'А' || c == 'Б' || c == 'В' || c == 'Г' || c == 'Д'
				|| c == 'Е' || c == 'Ё' || c == 'Ж' || c == 'З' || c == 'И'
				|| c == 'Й' || c == 'К' || c == 'Л' || c == 'М' || c == 'Н'
				|| c == 'О' || c == 'П' || c == 'Р' || c == 'С' || c == 'Т'
				|| c == 'У' || c == 'Ф' || c == 'Х' || c == 'Ц' || c == 'Ч'
				|| c == 'Ш' || c == 'Щ' || c == 'Ъ' || c == 'Ы' || c == 'Ь'
				|| c == 'Э' || c == 'Ю' || c == 'Я') {
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
				} else if (letter == 10) {
					word += 'а';
				} else if (letter == 11) {
					word += 'б';
				} else if (letter == 12) {
					word += 'в';
				} else if (letter == 13) {
					word += 'г';
				} else if (letter == 14) {
					word += 'д';
				} else if (letter == 15) {
					word += 'е';
				} else if (letter == 16) {
					word += 'ё';
				} else if (letter == 17) {
					word += 'ж';
				} else if (letter == 18) {
					word += 'з';
				} else if (letter == 19) {
					word += 'и';
				} else if (letter == 20) {
					word += 'й';
				} else if (letter == 21) {
					word += 'к';
				} else if (letter == 22) {
					word += 'л';
				} else if (letter == 23) {
					word += 'м';
				} else if (letter == 24) {
					word += 'н';
				} else if (letter == 25) {
					word += 'о';
				} else if (letter == 26) {
					word += 'п';
				} else if (letter == 27) {
					word += 'р';
				} else if (letter == 28) {
					word += 'с';
				} else if (letter == 29) {
					word += 'т';
				} else if (letter == 30) {
					word += 'у';
				} else if (letter == 31) {
					word += 'ф';
				} else if (letter == 32) {
					word += 'х';
				} else if (letter == 33) {
					word += 'ц';
				} else if (letter == 34) {
					word += 'ч';
				} else if (letter == 35) {
					word += 'ш';
				} else if (letter == 36) {
					word += 'щ';
				} else if (letter == 37) {
					word += 'ъ';
				} else if (letter == 38) {
					word += 'ы';
				} else if (letter == 39) {
					word += 'ь';
				} else if (letter == 40) {
					word += 'э';
				} else if (letter == 41) {
					word += 'ю';
				} else if (letter == 42) {
					word += 'я';
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
				if (cs[i] >= '0' && cs[i] <= '9') {
					letters[i] = cs[i] - '0';
				} else if (cs[i] == 'а' || cs[i] == 'А') {
					letters[i] = 10;
				} else if (cs[i] == 'б' || cs[i] == 'Б') {
					letters[i] = 11;
				} else if (cs[i] == 'в' || cs[i] == 'В') {
					letters[i] = 12;
				} else if (cs[i] == 'г' || cs[i] == 'Г') {
					letters[i] = 13;
				} else if (cs[i] == 'д' || cs[i] == 'Д') {
					letters[i] = 14;
				} else if (cs[i] == 'е' || cs[i] == 'Е') {
					letters[i] = 15;
				} else if (cs[i] == 'ё' || cs[i] == 'Ё') {
					letters[i] = 16;
				} else if (cs[i] == 'ж' || cs[i] == 'Ж') {
					letters[i] = 17;
				} else if (cs[i] == 'з' || cs[i] == 'З') {
					letters[i] = 18;
				} else if (cs[i] == 'и' || cs[i] == 'И') {
					letters[i] = 19;
				} else if (cs[i] == 'й' || cs[i] == 'Й') {
					letters[i] = 20;
				} else if (cs[i] == 'к' || cs[i] == 'К') {
					letters[i] = 21;
				} else if (cs[i] == 'л' || cs[i] == 'Л') {
					letters[i] = 22;
				} else if (cs[i] == 'м' || cs[i] == 'М') {
					letters[i] = 23;
				} else if (cs[i] == 'н' || cs[i] == 'Н') {
					letters[i] = 24;
				} else if (cs[i] == 'о' || cs[i] == 'О') {
					letters[i] = 25;
				} else if (cs[i] == 'п' || cs[i] == 'П') {
					letters[i] = 26;
				} else if (cs[i] == 'р' || cs[i] == 'Р') {
					letters[i] = 27;
				} else if (cs[i] == 'с' || cs[i] == 'С') {
					letters[i] = 28;
				} else if (cs[i] == 'т' || cs[i] == 'Т') {
					letters[i] = 29;
				} else if (cs[i] == 'у' || cs[i] == 'У') {
					letters[i] = 30;
				} else if (cs[i] == 'ф' || cs[i] == 'Ф') {
					letters[i] = 31;
				} else if (cs[i] == 'х' || cs[i] == 'Х') {
					letters[i] = 32;
				} else if (cs[i] == 'ц' || cs[i] == 'Ц') {
					letters[i] = 33;
				} else if (cs[i] == 'ч' || cs[i] == 'Ч') {
					letters[i] = 34;
				} else if (cs[i] == 'ш' || cs[i] == 'Ш') {
					letters[i] = 35;
				} else if (cs[i] == 'щ' || cs[i] == 'Щ') {
					letters[i] = 36;
				} else if (cs[i] == 'ъ' || cs[i] == 'Ъ') {
					letters[i] = 37;
				} else if (cs[i] == 'ы' || cs[i] == 'Ы') {
					letters[i] = 38;
				} else if (cs[i] == 'ь' || cs[i] == 'Ь') {
					letters[i] = 39;
				} else if (cs[i] == 'э' || cs[i] == 'Э') {
					letters[i] = 40;
				} else if (cs[i] == 'ю' || cs[i] == 'Ю') {
					letters[i] = 41;
				} else if (cs[i] == 'я' || cs[i] == 'Я') {
					letters[i] = 42;
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
