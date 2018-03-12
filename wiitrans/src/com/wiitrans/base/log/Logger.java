/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.log;

//public class Logger {
//
//	private static PrintStream _ps = null;
//
//	public static void log(String msg) {
//		if (msg != null && msg.trim().length() > 0) {
//			System.out.print("[LOG]");
//			System.out.println(msg);
//		}
//	}
//
//	public static void error(String msg) {
//		if (msg != null && msg.trim().length() > 0) {
//			System.err.print("[ERROR]");
//			System.err.println(msg);
//		}
//	}
//
//	public static void error(Exception ex) {
//		ex.printStackTrace();
//	}
//
//	public static void initOuter(String filePath) {
//		try {
//			_ps = new PrintStream(new FileOutputStream(filePath));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void setOut() {
//		System.setOut(_ps);
//	}
//
//	public static void setErr() {
//		System.setErr(_ps);
//	}
//}
