package com.wiitrans.base.tm;

import java.util.ArrayList;

public class TMTU {
	public int _tuid;
	public String _tuv1;
	public String _tuv2;
	public ArrayList<Integer> wordList;

	// public int _time = 0;
	public void Uninit() {
		if (_tuv1 != null) {
			_tuv1 = null;
		}
		if (_tuv2 != null) {
			_tuv2 = null;
		}
		if (wordList != null) {
			if (wordList.size() > 0) {
				wordList.clear();
			}
			wordList = null;
		}
	}
}
