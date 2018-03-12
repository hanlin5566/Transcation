package com.wiitrans.base.misc;

import com.wiitrans.base.log.Log4j;

public class MergeSort {
	public static int[] MergeList(int a[], int b[]) {
		int result[];
		// 检查传入的数组是否是有序的
		if (checkSort(a) && checkSort(b)) {
			result = new int[a.length + b.length];

			// i：用于标示a数组 j：用于标示b数组 k：用于标示传入的数组
			int i = 0, j = 0, k = 0;

			while (i < a.length && j < b.length)
				if (a[i] <= b[j]) {
					result[k++] = a[i++];
				} else {
					result[k++] = b[j++];
				}

			// 后面连个while循环是用来保证两个数组比较完之后剩下的一个数组里的元素能顺利传入
			while (i < a.length)
				result[k++] = a[i++];
			while (j < b.length)
				result[k++] = b[j++];

			return result;
		} else {
			Log4j.error("非有序數組，不可排序！");
			return null;
		}
	}

	// 检查数组是否是顺序存储的
	public static boolean checkSort(int a[]) {
		// 这个标示位是一种优化程序
		boolean change = true;
		for (int i = 0; i < a.length - 1 && change; ++i) {
			for (int j = i + 1; j < a.length; ++j)
				if (a[j - 1] > a[j]) {
					return false;
				} else {
					change = true;
				}
		}
		return true;
	}

	// 打印函数
	// public static void print(int b[]) {
	// for (int i = 0; i < b.length; i++) {
	// System.out.print(b[i] + (i % 10 == 9 ? "\n" : "\t"));
	// }
	// }

	// public static void main(String args[]) {
	// int a[] = { 1, 2, 2, 3, 5, 6, 7, 7 };
	// int b[] = { 1, 2, 4, 5, 8, 8, 9, 10, 11, 12, 12, 13, 14 };
	// int c[] = MergeList(a, b);
	// if (c != null)
	// print(c);
	// else
	// System.out.println("");
	// }
}
