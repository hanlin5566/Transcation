package com.wiitrans.base.http;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.wiitrans.base.log.Log4j;

public class DownloadURLFile {
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		boolean res = downloadFromUrl(
				"http://images.17173.com/2010/www/roll/201003/0301sohu01.jpg",
				"d:/");
		System.out.println(res);
	}

	public static boolean downloadFromUrl(String url, String dir) {

		try {
			URL httpurl = new URL(url);
			String fileName = getFileNameFromUrl(url);
			System.out.println(fileName);
			File f = new File(dir + fileName);
			FileUtils.copyURLToFile(httpurl, f);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
			return false;
		}
		return true;
	}

	public static boolean downloadFromUrl(String url, String dir,
			String fileName) {

		try {
			URL httpurl = new URL(url);
			System.out.println(fileName);
			File f = new File(dir + fileName);
			FileUtils.copyURLToFile(httpurl, f);
		} catch (Exception e) {
			// e.printStackTrace();
			Log4j.error(e);
			return false;
		}
		return true;
	}

	public static String getFileNameFromUrl(String url) {
		String name = new Long(System.currentTimeMillis()).toString() + ".X";
		int index = url.lastIndexOf("/");
		if (index > 0) {
			name = url.substring(index + 1);
			if (name.trim().length() > 0) {
				return name;
			}
		}
		return name;
	}
}
