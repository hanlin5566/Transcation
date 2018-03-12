package com.wiitrans.base.tm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class LoadDataInfile {
	public int CreateFile(String filePath) {
		int ret = Const.FAIL;
		File fileWrite = null;

		try {
			fileWrite = new File(filePath);

			if (fileWrite.exists()) {
				fileWrite.delete();
			}
			fileWrite.createNewFile();
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
		}

		return ret;
	}

	public int WriteAppend(String filePath, String str) {
		int ret = Const.FAIL;
		File fileWrite = null;
		BufferedWriter writer = null;
		FileOutputStream out = null;

		try {
			fileWrite = new File(filePath);

			// if (!fileWrite.exists()) {
			// fileWrite.createNewFile();
			// }
			out = new FileOutputStream(fileWrite, true);
			writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.append(str);
			writer.close();
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					Log4j.error(e);
				}
			}
		}

		return ret;
	}
}
