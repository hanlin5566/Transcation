package com.hzcf.variable.engine.algorithms;
/**
 * Create by hanlin on 2017年12月28日
 **/
public class Test {
	
	//decode key
//	static String s1 = "HJOPE-VMSHtpua7jh%252FPIKMeJE4m95sPVScWTlM3ToeF1FW0mip2gfyQa5lFb1n97vPzzWXFkWcgVNYVa%252FZOYjJbI2j8OW%252BaXBvDnxI1eZ%252FNLuM5UHu0YpCAtgbIp95lvcJjABNH%252FVkp1%252BUv%252F4%253D";
	//redis key
	static String s2 = "HJOPE-VMSHtpua7jh%2FPIKMeJE4m95sPVScWTlM3ToeF1FW0mip2gfyQa5lFb1n97vPzzWXFkWcgVNYVa%2FZOYjJbI2j8OW%2BaXBvDnxI1eZ%2FNLuM5UHu0YpCAtgbIp95lvcJjABNH%2FVkp1%2BUv%2F4%3D";
	//cooike key
	static String s1 = "HJOPE-VMSHtpua7jh%2FPIKMeJE4m95sPVScWTlM3ToeF1FW0mip2gfyQa5lFb1n97vPzzWXFkWcgVNYVa%2FZOYjJbI2j8OW%2BaXBvDnxI1eZ%2FNLuM5UHu0YpCAtgbIp95lvcJjABNH%2FVkp1%2BUv%2F4%3D";
	public static void main(String[] args) {
		
		String property = System.getProperty("java.class.path");
		System.out.println(property);
		//拿短的字符串循环，避免溢出
		int length = s1.length()<s2.length()?s1.length():s1.length();
		for (int i=0;i<length;i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if(c1 != c2){
				System.out.println("c1:"+c1+" c2:"+c2+" index:"+i);
			}
		}
	}
}
