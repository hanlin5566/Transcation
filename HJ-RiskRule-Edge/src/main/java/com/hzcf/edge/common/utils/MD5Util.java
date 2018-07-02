package com.hzcf.edge.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	public static final String KEY_MD5 = "MD5";
	public static final String KEY_SHA = "SHA";

	public static String getMD5Result(String inputStr) {
		BigInteger bigInteger = null;
		try {
			MessageDigest md = MessageDigest.getInstance(KEY_MD5);
			byte[] inputData = inputStr.getBytes();
			md.update(inputData);
			bigInteger = new BigInteger(md.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bigInteger.toString(16);
	}

	/**
	 * 百融
	 * @param plainText
	 * @return
	 */
	public static String cell32(String plainText) {
		String re_md5 = new String();

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte[] b = md.digest();
			StringBuffer buf = new StringBuffer("");

			for(int offset = 0; offset < b.length; ++offset) {
				int i = b[offset];
				if(i < 0) {
					i += 256;
				}

				if(i < 16) {
					buf.append("0");
				}

				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();
		} catch (NoSuchAlgorithmException var7) {
			var7.printStackTrace();
		}

		return re_md5;
	}

	public static String getSHAResult(String inputStr) {
		BigInteger sha = null;
		byte[] inputData = inputStr.getBytes();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(KEY_SHA);
			messageDigest.update(inputData);
			sha = new BigInteger(messageDigest.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sha.toString(32);
	}

	public static String encodePwd(String userPwd,String userSalt) {
		return getMD5Result(getSHAResult(getMD5Result(userSalt)
				+ getSHAResult(userPwd))
				+ userSalt);
	}

	public static void main(String args[]) {
		try {
			String pwd = encodePwd("1B5BNJKpMagePdB4", "8765");
			System.out.println(pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
