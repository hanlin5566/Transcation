package com.wiitrans.base.tm;

import java.util.Comparator;

//使用他来减小空间使用
public class TMCountWordTU {
	public int tuID = 0;
	public int count = 1;
}

class TMWordTUTimeSortByTime implements Comparator<TMCountWordTU> {

	@Override
	public int compare(TMCountWordTU o1, TMCountWordTU o2) {
		return o1.count - o2.count;
	}

}

class TMWordTUTimeSortByTimeDESC implements Comparator<TMCountWordTU> {

	@Override
	public int compare(TMCountWordTU o1, TMCountWordTU o2) {
		return o2.count - o1.count;
	}

}