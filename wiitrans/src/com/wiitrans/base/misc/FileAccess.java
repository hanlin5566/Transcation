package com.wiitrans.base.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.wiitrans.base.log.Log4j;

public class FileAccess {
	public static void CreateDirectory(boolean needTMXDTD, String path,
			String dtd) {
		if (path == null || path.trim().length() == 0) {
			return;
		}
		try {

			File file = new File(path);
			if (!file.exists()) {
				if (!file.isDirectory()) {
					file.mkdir();
					if (needTMXDTD) {
						String filename = path + "tmx14.dtd";
						File tmxdtdfile = new File(filename);
						if (!tmxdtdfile.exists()) {
							FileAccess.Copy(dtd, filename);
						}
					}
				}
			}
		} catch (Exception e) {
			Log4j.error(e);
		}
	}

	public static boolean Move(File srcFile, String destPath) {
		// Destination directory
		File dir = new File(destPath);

		// Move file to new directory
		boolean success = srcFile.renameTo(new File(dir, srcFile.getName()));

		return success;
	}

	public static boolean Move(String srcFile, String destPath) {
		// File (or directory) to be moved
		File file = new File(srcFile);

		// Destination directory
		File dir = new File(destPath);

		// Move file to new directory
		boolean success = file.renameTo(new File(dir, file.getName()));

		return success;
	}

	public static void Copy(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					//System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			Log4j.error("tmx14.dtd must exist. ");
			Log4j.error(e);
		}
	}

	public static void Copy(File oldfile, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			// File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldfile);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					//System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			Log4j.error("tmx14.dtd must exist. ");
			Log4j.error(e);
		}
	}
}
