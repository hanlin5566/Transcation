package com.wiitrans.base.tm;

import java.util.Comparator;

public class TMWord {
	public String word = null;
	public boolean isAbled = false;
	public int beginAt = 0;
	public int sequence = 0;
	public boolean isWord = false;
	public long wordID = 0;
	public int time = 0;
	public int priority = 0;// 单词越长，优先级priority越小，小于5的优先级不变，大于5的按照长度成比例
	// public int
}

class TMWordSortByTime implements Comparator<TMWord> {

	@Override
	public int compare(TMWord o1, TMWord o2) {
		// TODO Auto-generated method stub
		return o1.priority - o2.priority;
	}

}
