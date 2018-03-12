package com.wiitrans.base.file.lang;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.misc.Const;

public abstract class Language {

	public abstract LANGUAGE_NAME GetName();

	public abstract LANGUAGE_TYPE GetType();

	public abstract ArrayList<LangSentence> AnalyseSentence(String text);

	public abstract ArrayList<Word> AnalyseWord(String sentence);

	public abstract int IsLetter(char c);

	public abstract boolean ValidateWord(String word, int maxLen);

	public abstract String GetWordByHansonCode(long wordID);

	public abstract long GetHansonCodeByWord(String word);

	public ArrayList<LangSentence> AnalyseText(String text) {
		if (text == null || text.isEmpty()) {
			return new ArrayList<LangSentence>();
		}

		ArrayList<LangSentence> list = new ArrayList<LangSentence>();

		LangSentence sentence = null;

		StringBuilder builder = new StringBuilder();
		char[] chs = text.toCharArray();

		for (int i = 0; i < chs.length; ++i) {
			if (CheckCharacter(chs[i]) == Const.CHAR_SEPARATE) {
				if (builder.length() > 0) {
					sentence = new LangSentence();
					sentence.valid = true;
					sentence.text = builder.toString();
					list.add(sentence);
				}

				sentence = new LangSentence();
				sentence.valid = false;
				sentence.text = String.valueOf(chs[i]);
				sentence.code = this.Encode(chs[i]);
				list.add(sentence);

				builder = new StringBuilder();
			} else {
				builder.append(chs[i]);
			}
		}

		if (builder.length() > 0) {
			sentence = new LangSentence();
			sentence.valid = true;
			sentence.text = builder.toString();
			list.add(sentence);
		}

		return list;
	}

	private int CheckCharacter(char c) {
		int result = Const.CHAR_NORMAL;
		if (c >= 0 && c <= 31) {
			switch (c) {
			case 0: {
				result = Const.CHAR_EMPTY;
				break;
			}
			default: {
				result = Const.CHAR_SEPARATE;
				break;
			}
			}
		} else if (c == 127) {
			result = Const.CHAR_SEPARATE;
		} else if (c == 183) {
			result = Const.CHAR_SEPARATE;
		} else if (c == 12288) {
			result = Const.CHAR_SEPARATE;
		}
		return result;
	}

	public String Encode(char c) {
		if (c > 12288 || c < 12288 && c > 183 || c < 183 && c > 127 || c < 127
				&& c > 31) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("{").append((int) c).append("}");

		return sb.toString();
	}

	public String Encode(String text) {
		if (text == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			if (CheckCharacter(c) != Const.CHAR_NORMAL) {
				sb.append("{").append((int) c).append("}");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public String Decode(String text) {
		if (text == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		char c, c1, c2, c3, c4, c5, c6, c0;
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			if (c == '{') {
				c1 = c2 = c3 = c4 = c5 = c6 = 0;

				if (text.length() > i + 2) {
					c1 = text.charAt(i + 1);
					if (c1 <= '9' && c1 >= '0') {
						c2 = text.charAt(i + 2);
						if (c2 == '}') {
							c0 = (char) (c1 - '0');
							if (CheckCharacter(c0) != Const.CHAR_NORMAL) {
								sb.append(c0);
								i += 2;
								continue;
							}

						} else if (c2 <= '9' && c2 >= '0'
								&& text.length() > i + 3) {
							c3 = text.charAt(i + 3);
							if (c3 == '}') {
								c0 = (char) ((c1 - '0') * 10 + c2 - '0');
								if (CheckCharacter(c0) != Const.CHAR_NORMAL) {
									sb.append(c0);
									i += 3;
									continue;
								}
							} else if (c3 <= '9' && c3 >= '0'
									&& text.length() > i + 4) {
								c4 = text.charAt(i + 4);
								if (c4 == '}') {
									c0 = (char) ((c1 - '0') * 100 + (c2 - '0')
											* 10 + c3 - '0');
									if (CheckCharacter(c0) != Const.CHAR_NORMAL) {
										sb.append(c0);
										i += 4;
										continue;
									}
								} else if (c4 <= '9' && c4 >= '0'
										&& text.length() > i + 5) {
									c5 = text.charAt(i + 5);
									if (c5 == '}') {
										c0 = (char) ((c1 - '0') * 1000
												+ (c2 - '0') * 100 + (c3 - '0')
												* 10 + c4 - '0');
										if (CheckCharacter(c0) != Const.CHAR_NORMAL) {
											sb.append(c0);
											i += 5;
											continue;
										}
									} else if (c5 <= '9' && c5 >= '0'
											&& text.length() > i + 6) {
										c6 = text.charAt(i + 6);
										if (c6 == '}') {
											c0 = (char) ((c1 - '0') * 10000
													+ (c2 - '0') * 1000
													+ (c3 - '0') * 100
													+ (c4 - '0') * 10 + c5 - '0');
											if (CheckCharacter(c0) != Const.CHAR_NORMAL) {
												sb.append(c0);
												i += 6;
												continue;
											}
										}
									}
								}
							}
						}
					}

				}

			}

			sb.append(c);

		}
		return sb.toString();
	}

	public boolean CheckIndex(int index, int length, ArrayList<int[]> ary) {
		if (index >= 0 & ary != null && ary.size() > 0) {
			int begin = index;
			int end = index + length - 1;
			for (int[] tag : ary) {
				if (tag.length == 2) {
					if (tag[0] <= begin && tag[1] >= begin || tag[0] <= end
							&& tag[1] >= end) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public ArrayList<int[]> CheckTagRange(String text) {
		ArrayList<int[]> ary = new ArrayList<int[]>();
		int begin = -1, end = -1;

		if (text != null && text.length() > 0) {
			char[] cs = text.toCharArray();
			for (int i = 0; i < cs.length; i++) {
				if (begin < 0 && end < 0) {
					if (cs[i] == '<') {
						begin = i;
					} else {
						continue;
					}
				} else if (begin >= 0 && end < 0) {
					if (cs[i] == '>') {
						end = i;
					} else {
						continue;
					}
				} else if (begin >= 0 && end >= 0) {
					if (cs[i] == '<') {
						ary.add(new int[] { begin, end });
						begin = i;
						end = -1;
					} else if (cs[i] == '>') {
						end = i;
					}
				}
			}
			if (begin >= 0 && end >= 0) {
				ary.add(new int[] { begin, end });
			}
		}

		return ary;
	}
}
