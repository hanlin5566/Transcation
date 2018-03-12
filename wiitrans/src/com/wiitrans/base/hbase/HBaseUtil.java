package com.wiitrans.base.hbase;

public class HBaseUtil {
	public static String GetHash2FromString(String str) {
		int hash = 0;
		for (int c : str.toCharArray()) {
			hash = hash * 13131 + c;
		}

		int hash2char = hash % 100;
		if (hash2char < 0) {
			hash2char += 100;
		}
		return String.valueOf(hash2char);
	}
}
