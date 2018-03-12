package com.wiitrans.base.misc;

public class QuickSort {

	public int[] Sort(int[] array) {

		int[] result = array.clone();
		this.Sort(result, 0, result.length - 1);
		return result;

	}

	public boolean Sort(int[] array, int begin, int end) {
		if (begin < 0) {
			return false;
		} else if (end >= array.length) {
			return false;
		}

		recursive(array, begin, end);

		return true;
	}

	// 递归算法
	private void recursive(int[] array, int begin, int end) {
		int left = begin;
		int right = end;
		// 中间值
		int middle_value = array[left];
		// 中间值位置，从最左第一个数开始
		int middle = left++;
		// 值互换临时变量
		int temp;
		while (left <= right) {
			// 从左开始寻找一个大于中间值的值的位置
			if (array[left] <= middle_value) {
				++left;
				continue;
			}
			// 从右开始寻找一个小于中间值的值的位置
			if (middle_value < array[right]) {
				--right;
				continue;
			}
			// 值互换
			temp = array[left];
			array[left] = array[right];
			array[right] = temp;
			// 位置靠近
			++left;
			--right;
		}
		// 排序好之后,把中间值换到中间位置
		temp = array[left - 1];
		array[left - 1] = array[middle];
		array[middle] = temp;
		// 中间位置变成新的
		middle = left - 1;
		if (begin < middle - 1) {
			// 左半部分递归
			recursive(array, begin, middle - 1);
		}
		if (middle + 1 < end) {
			// 右半部分递归
			recursive(array, middle + 1, end);
		}
	}
}
