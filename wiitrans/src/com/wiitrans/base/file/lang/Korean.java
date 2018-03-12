package com.wiitrans.base.file.lang;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class Korean extends Language {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.KOREAN;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.IDEOGRAPH;

	private ArrayList<Character> ignoreList;
	private ArrayList<Character> symbolForSentence;

	public Korean() {
		ignoreList = new ArrayList<Character>();
		symbolForSentence = new ArrayList<Character>();

		ignoreList.add(new Character('☂'));
		symbolForSentence.add(new Character('.'));
		symbolForSentence.add(new Character('!'));
		symbolForSentence.add(new Character('?'));
	}

	public Korean(ArrayList<Character> ignoreList,
			ArrayList<Character> symbolForSentence) {
		this.ignoreList = ignoreList;
		this.symbolForSentence = symbolForSentence;
	}

	@Override
	public LANGUAGE_NAME GetName() {
		return _name;
	}

	@Override
	public LANGUAGE_TYPE GetType() {
		return _type;
	}

	@Override
	public ArrayList<LangSentence> AnalyseSentence(String text) {

		if (text == null || text.isEmpty()) {
			return new ArrayList<LangSentence>();
		}
		ArrayList<LangSentence> sentences = new ArrayList<LangSentence>();
		LangSentence sentence = null;
		ArrayList<LangSentence> tempList = this.AnalyseText(text);
		for (LangSentence temptext : tempList) {
			if (temptext.valid) {
				StringBuilder builder = new StringBuilder();
				char[] chs = temptext.text.toCharArray();

				for (int i = 0; i < chs.length; ++i) {

					builder.append(chs[i]);

					switch (chs[i]) {
					case '.':
						sentence = new LangSentence();
						sentence.valid = true;
						sentence.text = builder.toString();
						sentences.add(sentence);
						builder = new StringBuilder();
						break;
					case '?':
						sentence = new LangSentence();
						sentence.valid = true;
						sentence.text = builder.toString();
						sentences.add(sentence);
						builder = new StringBuilder();
						break;
					case '!':
						sentence = new LangSentence();
						sentence.valid = true;
						sentence.text = builder.toString();
						sentences.add(sentence);
						builder = new StringBuilder();
						break;
					default:
						break;
					}
				}
				if (builder.length() > 0) {
					sentence = new LangSentence();
					sentence.valid = true;
					sentence.text = builder.toString();
					sentences.add(sentence);
				}
			} else {
				sentences.add(temptext);
			}
		}

		return sentences;
	}

	@Override
	public ArrayList<Word> AnalyseWord(String sentence) {
		ArrayList<Word> wordList = new ArrayList<Word>();
		Word word;
		if (sentence != null) {
			ArrayList<int[]> ary = super.CheckTagRange(sentence);
			char[] chars = sentence.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (IsLetter(chars[i]) > 0) {
					word = new Word();
					word.word = String.valueOf(chars[i]);
					word.charindex = i;
					word.wordindex = i;
					word.isAbled = super.CheckIndex(i, 1, ary);
					wordList.add(word);
				}
			}
		}
		return wordList;
	}

	@Override
	public int IsLetter(char c) {
		int result;
		// if (c >= 0x4e00 && c <= 0x9fbb) {
		if (c >= 0x3000 && c <= 0x9fff || c >= 0xac00 && c <= 0xd7af) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	public boolean ValidateWord(String word, int maxLen) {
		if (word == null || word.length() == 0) {
			return false;
		}

		if (word.length() > maxLen) {
			return false;
		}

		return true;
	}

	@Override
	public String GetWordByHansonCode(long wordID) {
		return null;
	}

	@Override
	public long GetHansonCodeByWord(String word) {
		return 0;
	}
}