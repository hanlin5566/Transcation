package com.wiitrans.base.file.lang;

import java.util.ArrayList;

import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public abstract class CheckLanguage {
	public abstract LANGUAGE_NAME GetName();

	public abstract LANGUAGE_TYPE GetType();

	public abstract ArrayList<Word> AnalyseWord(String sentence);

	public abstract int IsLetter(char c);

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
