package com.wiitrans.base.tm;

import java.util.ArrayList;

public class TMMergeSort {
	public static TMCountWordTU[] MergeTUList(ArrayList<TMCountWordTU[]> list) {
		TMCountWordTU[] ret = null;
		ArrayList<TMCountWordTU[]> tmp = list;
		int time = tmp.size();
		if (time == 0) {
			ret = new TMCountWordTU[0];
		} else {
			int k = 0;
			TMCountWordTU[] a;
			TMCountWordTU[] b;
			while (tmp.size() > 1 && k++ < time) {
				for (int i = 0, j = list.size() - 1; i <= j; ++i, --j) {
					if (i < j) {
						a = tmp.get(i);
						b = tmp.get(j);
						tmp.set(i, TMMergeSort.MergeList(a, b));
						tmp.remove(j);
					}
				}
			}
			ret = tmp.get(0);
		}
		return ret;
	}

	public static TMCountWordTU[] MergeList(TMCountWordTU[] a, TMCountWordTU[] b) {
		TMCountWordTU[] result;
		// 检查传入的数组是否是有序的

		// result = new int[a.length + b.length][2];
		ArrayList<TMCountWordTU> list = new ArrayList<TMCountWordTU>();

		// i：用于标示a数组 j：用于标示b数组 k：用于标示传入的数组
		int i = 0, j = 0;// k = 0;

		while (i < a.length && j < b.length)
			if (a[i].tuID < b[j].tuID) {
				list.add(a[i++]);
			} else if (a[i].tuID == b[j].tuID) {
				a[i].count += b[j++].count;
			} else {
				list.add(b[j++]);
			}

		// 后面连个while循环是用来保证两个数组比较完之后剩下的一个数组里的元素能顺利传入
		while (i < a.length)
			list.add(a[i++]);
		while (j < b.length)
			list.add(b[j++]);

		result = new TMCountWordTU[list.size()];
		list.toArray(result);
		list.clear();
		return result;
	}

	// 检查数组是否是顺序存储的
	public static boolean checkSort(TMCountWordTU[] a) {
		// 这个标示位是一种优化程序
		boolean change = true;
		for (int i = 0; i < a.length - 1 && change; ++i) {
			for (int j = i + 1; j < a.length; ++j)
				if (a[j - 1].tuID > a[j].tuID) {
					return false;
				} else {
					change = true;
				}
		}
		return true;
	}
}
