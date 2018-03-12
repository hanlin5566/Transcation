package com.wiitrans.base.tm;

public class Levenshtein {
	// 相似度
	public int Similarity(long[] row, long[] col) {
		int RowLen = row.length;
		int ColLen = col.length;
		int max = Math.max(RowLen, ColLen);
		return 100 - ((100 * Distance(row, col)) / max);
	}

	// 距离
	public int Distance(long[] row, long[] col) {
		int RowLen = row.length; // length of sRow
		int ColLen = col.length; // length of sCol
		int RowIdx; // iterates through sRow
		int ColIdx; // iterates through sCol
		long Row_i; // ith character of sRow
		long Col_j; // jth character of sCol
		int cost; // cost

		// Step 1

		if (RowLen <= 0) {
			return ColLen;
		}

		if (ColLen <= 0) {
			return RowLen;
		}

		// / Create the two vectors
		int[] v0 = new int[RowLen + 1];
		int[] v1 = new int[RowLen + 1];
		int[] vTmp;

		// / Step 2
		// / Initialize the first vector
		for (RowIdx = 1; RowIdx <= RowLen; RowIdx++) {
			v0[RowIdx] = RowIdx;
		}

		// Step 3

		// / Fore each column
		for (ColIdx = 1; ColIdx <= ColLen; ColIdx++) {
			// / Set the 0'th element to the column number
			v1[0] = ColIdx;

			Col_j = col[ColIdx - 1];

			// Step 4

			// / Fore each row
			for (RowIdx = 1; RowIdx <= RowLen; RowIdx++) {
				Row_i = row[RowIdx - 1];

				// Step 5

				if (Row_i == Col_j) {
					cost = 0;
				} else {
					cost = 1;
				}

				// Step 6

				// / Find minimum
				int m_min = v0[RowIdx] + 1;
				int b = v1[RowIdx - 1] + 1;
				int c = v0[RowIdx - 1] + cost;

				if (b < m_min) {
					m_min = b;
				}
				if (c < m_min) {
					m_min = c;
				}

				v1[RowIdx] = m_min;
			}

			// / Swap the vectors
			vTmp = v0;
			v0 = v1;
			v1 = vTmp;

		}

		// Step 7

		// / Value between 0 - 100
		// / 0==perfect match 100==totaly different
		// /
		// / The vectors where swaped one last time at the end of the last loop,
		// / that is why the result is now in v0 rather than in v1
		// System.Console.WriteLine("operate=" + v0[RowLen]);
		return v0[RowLen];
	}
}
