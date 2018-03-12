package com.wiitrans.base.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckDigit {

	public static void main(String[] args) {
		String source = "1llsdf23ase3l123kdfj550.5lk5j4d55asdf0.5lkajsdf-239";
		String target = "1lls1df2asdfasdf3a1slkdfj155l1kjasdf0.5lk2ajsd3f-239";
		long s = System.currentTimeMillis();
		ArrayList<String> sourceDigitList = CheckDigit.getDigit(source);//切原文
		System.out.println(sourceDigitList);
		if (sourceDigitList.size() > 0) {
			ArrayList<String> targetDigitList = CheckDigit.getDigit(target);//切译文
			System.out.println(targetDigitList);
			Map<String, List<String>> result = CheckDigit.matchDiff(sourceDigitList, targetDigitList);//比对差异
			System.out.println(System.currentTimeMillis() - s);
			System.out.println(result.get("source"));//原文差异结果
			System.out.println(result.get("target"));//译文差异结果
//			matchDiff(source, target);
		}
		
	}

	public static Map<String, List<String>> matchDiff(String source, String target) {
	    Map<String, List<String>> result = new HashMap<String, List<String>>();
	    ArrayList<String> sourceDigitList = CheckDigit.getDigit(source);//切原文
	    ArrayList<String> targetDigitList = CheckDigit.getDigit(target);//切译文
	    result = CheckDigit.matchDiff(sourceDigitList, targetDigitList);//比对差异
	    return result;
	}

	public static Map<String, List<String>> matchDiff(List<String> sourceList,
			List<String> targetList) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		ArrayList<String> sourceList_remove = new ArrayList<String>();
		// ����۲�����
		//Collections.sort(sourceList, new SortNumAsc());
		//Collections.sort(targetList, new SortNumAsc());
		// System.out.println(sourceList);
		// System.out.println(targetList);

		for (int i = 0; i < sourceList.size(); i++) {
			String s = sourceList.get(i);
			int index = targetList.indexOf(s);
			if (index >= 0) {
				targetList.remove(index);
			} else {
				sourceList_remove.add(s);
			}
		}
		// List<String> sourceList_copy = new ArrayList<String>();
		// sourceList_copy.addAll(sourceList);
		// sourceList.removeAll(targetList);//Դ����
		// targetList.removeAll(sourceList_copy);//Ŀ�����
		result.put("source", sourceList_remove);
		result.put("target", targetList);
		return result;
	}

	public static ArrayList<String> getDigit(String str) {
		char[] b = str.toCharArray();
		ArrayList<String> result = new ArrayList<String>();
		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			// ���� ���ſ�ʼ������һλ�ǲ�Ϊ�������
			if ('-' == b[i] && i + 1 < b.length && Character.isDigit(b[i + 1])) {
				// �������֮ǰ������
				if (strBuff.length() > 0) {
					result.add(strBuff.toString());
					strBuff = new StringBuffer();
				}
				strBuff.append(b[i]);
				strBuff.append(b[++i]);// ������һλ
			} else if ('.' == b[i] && i > 1 && Character.isDigit(b[i - 1])
					&& i + 1 < b.length && Character.isDigit(b[i + 1])) {// С��,ǰ�������֣����Һ���������
				strBuff.append(b[i]);
				strBuff.append(b[++i]);// ������һλ
			} else if (Character.isDigit(b[i])) {
				strBuff.append(b[i]);
			} else {
				if (strBuff.length() > 0) {
					result.add(strBuff.toString());
					strBuff = new StringBuffer();
				}
			}
		}
		// ���һλ
		if (strBuff.length() > 0) {
			result.add(strBuff.toString());
			strBuff = new StringBuffer();
		}
		return result;
	}

	class SortNumAsc implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return new Double(o1).compareTo(new Double(o2));
		}
	}
}
