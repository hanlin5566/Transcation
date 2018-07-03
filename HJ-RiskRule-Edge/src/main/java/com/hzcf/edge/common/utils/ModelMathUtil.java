package com.hzcf.edge.common.utils;

import java.math.BigDecimal;

/**
 * 模型--字符串相似性算法
 * Created by Rayming on 2017/6/19.
 */
public class ModelMathUtil {

    public static double getSimilitude(String a, String b) {

        if (a.length() == 0 || b.length() == 0)
            return 0.000;

        //相同字符
        String longestCommonString = a.length()>b.length()? longestCommonSubstring(a, b): longestCommonSubstring(b,a);
        //String longestCommonString = longestCommonSubstring(a, b);
        //保留3位小数
        BigDecimal bigDecimal = new BigDecimal((longestCommonString.length() * 1.0) / Math.min(a.length(), b.length()));
        
        return bigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 字符串相似字符
     *
     * @param strA
     * @param strB
     * @return
     */
    private static String longestCommonSubstring(String strA, String strB) {
        char[] chars_strA = strA.toCharArray();
        char[] chars_strB = strB.toCharArray();
        int m = chars_strA.length;
        int n = chars_strB.length;
        int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (chars_strA[i - 1] == chars_strB[j - 1])
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                else
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
            }
        }

        char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[n] == matrix[n - 1])
                n--;
            else if (matrix[m][n] == matrix[m - 1][n])
                m--;
            else {
                result[currentIndex] = chars_strA[m - 1];
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }
}
