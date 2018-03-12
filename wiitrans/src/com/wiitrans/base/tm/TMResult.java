package com.wiitrans.base.tm;

import java.util.Comparator;

import org.json.JSONObject;

public class TMResult {
	public int tuID = 0;
	public int wordCount = 0;// 相同字数
	// public int matching = 0;
	public int distance = 0;
	public int similarity = 0;
	public String source = null;
	public String target = null;
	public JSONObject obj = null;
}

class TMResultSortDistanceDesc implements Comparator<TMResult> {

	@Override
	public int compare(TMResult o1, TMResult o2) {
		if (o2.wordCount == o1.wordCount) {
			return o1.distance - o2.distance;
		} else {
			return o2.wordCount - o1.wordCount;
		}
	}

}

class TMResultSortDistanceAsc implements Comparator<TMResult> {

	@Override
	public int compare(TMResult o1, TMResult o2) {
		if (o1.wordCount == o2.wordCount) {
			return o2.distance - o1.distance;
		} else {
			return o1.wordCount - o2.wordCount;
		}
	}
}

class TMResultSortSimilarityDesc implements Comparator<TMResult> {

	@Override
	public int compare(TMResult o1, TMResult o2) {
		if (o2.wordCount == o1.wordCount) {
			return o2.similarity - o1.similarity;
		} else {
			return o2.wordCount - o1.wordCount;
		}
	}

}

class TMResultSortSimilarityAsc implements Comparator<TMResult> {

	@Override
	public int compare(TMResult o1, TMResult o2) {
		if (o1.wordCount == o2.wordCount) {
			return o1.similarity - o2.similarity;
		} else {
			return o1.wordCount - o2.wordCount;
		}
	}
}
