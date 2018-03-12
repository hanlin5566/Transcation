package com.wiitrans.base.file.lang;

import java.util.ArrayList;

import com.sun.swing.internal.plaf.synth.resources.synth;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.tm.TMWord;

public class CheckEnglish extends CheckLanguage {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.ENGLISH;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;

	@Override
	public LANGUAGE_NAME GetName() {
		return _name;
	}

	@Override
	public LANGUAGE_TYPE GetType() {
		return _type;
	}

	@Override
	public ArrayList<Word> AnalyseWord(String sentence) {
		ArrayList<Word> list = new ArrayList<Word>();

		boolean isAbled = true;
		boolean symbol = false;

		Word word;
		ArrayList<int[]> ary = super.CheckTagRange(sentence);

		if (sentence != null && sentence.trim().length() > 0) {
			char[] chars = sentence.toCharArray();

			int length = sentence.length(), i = 0, j = 0;
			String sword;
			char letter = '\0';// , preLetter;
			int property = 0, preProperty;// 字母的性质
			int maxLen = 30;

			while (j < length) {
				sword = null;
				// preLetter = letter;
				letter = chars[j];
				preProperty = property;

				// 当前字符是否字母
				property = this.IsLetter(letter);

				if (property == 0 && preProperty == 0) {
					// 第j个字符和第j-1个字符都不是字母
					++j;

					isAbled = true;
				} else if (property > 0 && preProperty == 0) {
					// 单词开始
					i = j;
					isAbled = true;
				} else if (property == 0 && preProperty > 0) {
					// 单词结束
					// word = sentence.substring(i, j - i);
					sword = sentence.substring(i, j);

					word = validateWord(sword, maxLen);
					if (word != null && word.isAbled) {
						word.wordindex = list.size();
						word.charindex = i;
						if (isAbled) {
							word.isAbled = super.CheckIndex(word.charindex,
									word.word.length(), ary);
						} else {
							word.isAbled = false;
						}
						word.symbol = symbol;
						list.add(word);
						
						symbol = false;
					}
					++j;
					i = j;
				} else if (property == 3 || preProperty == 3) {
					// 第j个字符或第j-1个字符是数字
					++j;
					isAbled = false;
				} else if (property == 4 || preProperty == 4) {
					// 第j个字符或第j-1个字符是符号
					++j;
					isAbled = true;
					symbol = true;
				} else {
					// 第j个字符和第j-1个字符都是字母
					++j;
				}

			}

			if (property > 0) {
				sword = sentence.substring(i, j);

				word = validateWord(sword, maxLen);
				if (word != null && word.isAbled) {
					word.wordindex = list.size();
					word.charindex = i;
					if (isAbled) {
						word.isAbled = super.CheckIndex(word.charindex,
								word.word.length(), ary);
					} else {
						word.isAbled = false;
					}
					word.symbol = symbol;
					list.add(word);
					
					symbol = false;
				}
			}
		}
		return list;
	}

	private Word validateWord(String sword, int maxLen) {

		// 此方法只能从单词开始的位子操作
		if (sword == null || sword.length() == 0) {
			Word word = new Word();
			word.isAbled = false;
			return word;
		}

		sword = sword.toLowerCase();
		Word tmword = new Word();
		tmword.word = sword;
		tmword.isAbled = true;
		if (sword.length() > maxLen) {
			// 如果大于最大长度，不算单词
			tmword.isAbled = false;
			return tmword;
		}

		return tmword;
	}

	@Override
	public int IsLetter(char c) {
		// 1小写2大写3数字0非字母
		int result;
		if (c >= 'a' && c <= 'z') {
			result = 1;
		} else if (c >= 'A' && c <= 'Z') {
			result = 2;
		} else if (c >= '0' && c <= '9') {
			result = 3;
		} else if (c == '\'') {
			result = 4;
		} else {
			result = 0;
		}
		return result;
	}
}
