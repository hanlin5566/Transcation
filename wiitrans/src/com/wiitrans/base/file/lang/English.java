package com.wiitrans.base.file.lang;

import java.util.ArrayList;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class English extends Language {

	class End {
		char[] endChars;
		String endString;

		public End(String end) {
			endChars = end.toCharArray();
			endString = end;
		}
	}

	private LANGUAGE_NAME _name = LANGUAGE_NAME.ENGLISH;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.LETTER;

	private ArrayList<Character> ignoreList;
	private ArrayList<Character> spaceListForWord;
	private ArrayList<End> endList;

	public English() {
		ignoreList = new ArrayList<Character>();
		spaceListForWord = new ArrayList<Character>();
		endList = new ArrayList<End>();

		ignoreList.add(new Character('☂'));
		spaceListForWord.add(new Character(','));
		spaceListForWord.add(new Character(';'));
		spaceListForWord.add(new Character('"'));
		spaceListForWord.add(new Character('('));
		spaceListForWord.add(new Character(')'));
		spaceListForWord.add(new Character('['));
		spaceListForWord.add(new Character(']'));
		spaceListForWord.add(new Character('{'));
		spaceListForWord.add(new Character('}'));
		spaceListForWord.add(new Character(':'));
		spaceListForWord.add(new Character('&'));
		spaceListForWord.add(new Character('#'));
		spaceListForWord.add(new Character('='));
		spaceListForWord.add(new Character('/'));
		spaceListForWord.add(new Character('_'));
		spaceListForWord.add(new Character('*'));
		spaceListForWord.add((char) 160);// 不间断空格特殊处理

		endList.add(new End(". "));
		endList.add(new End("! "));
		endList.add(new End("? "));
		// endList.add(new End("!"));
		// endList.add(new End("?"));
		// endList.add(new End(String.valueOf((char) 14)));
	}

	public English(ArrayList<Character> ignoreList,
			ArrayList<Character> spaceListForWord, ArrayList<End> endList) {
		this.ignoreList = ignoreList;
		this.spaceListForWord = spaceListForWord;
		this.endList = endList;
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
				boolean isEnd = false;
				char[] cs = null;
				for (int i = 0; i < chs.length;) {
					for (End end : endList) {
						isEnd = true;
						cs = end.endChars;
						if (i + end.endChars.length < chs.length + 1) {
							for (int j = 0; j < end.endChars.length; ++j) {
								if (end.endChars[j] != chs[i + j]) {
									isEnd = false;
									break;
								}
							}
							if (isEnd) {
								break;
							}
						} else {
							isEnd = false;
							break;
						}
					}

					if (isEnd) {
						builder.append(cs);

						sentence = new LangSentence();
						sentence.valid = true;
						sentence.text = builder.toString();

						sentences.add(sentence);
						builder = new StringBuilder();
						i += cs.length;
					} else {
						builder.append(chs[i]);
						++i;
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

		ArrayList<Word> wordsList = new ArrayList<Word>();
		if (sentence != null) {
			ArrayList<int[]> ary = super.CheckTagRange(sentence);
			for (Character c : ignoreList) {
				sentence = sentence.replace(c.toString(), "­");
			}

			for (End end : endList) {
				if (sentence.endsWith(end.endString)) {
					sentence = sentence.substring(0, sentence.length()
							- end.endChars.length);
					break;
				}
			}

			for (Character c : spaceListForWord) {
				sentence = sentence.replace(c, ' ');
			}

			String[] swords = sentence.split(" ");
			Word word;
			int charindex = 0;
			int wordindex = 0;
			for (String sword : swords) {
				if (sword != null) {
					if (sword.trim().length() > 0) {
						// System.out.println((int) (sword.charAt(0)));
						try {
							Double.parseDouble(sword);
							charindex += sword.length();
						} catch (Exception e) {
							word = new Word();
							word.word = sword;
							word.charindex = charindex;
							word.wordindex = wordindex++;
							word.isAbled = super.CheckIndex(charindex,
									word.word.length(), ary);
							wordsList.add(word);
							charindex += sword.length();
						}
					}
					// 空格占用位置
					++charindex;
				}
			}
		}
		return wordsList;
	}

	@Override
	public int IsLetter(char c) {
		return 0;
	}

	@Override
	public boolean ValidateWord(String word, int maxLen) {
		return false;
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
