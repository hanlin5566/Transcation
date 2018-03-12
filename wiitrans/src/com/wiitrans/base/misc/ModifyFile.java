package com.wiitrans.base.misc;

import java.io.File;
import java.io.FileOutputStream;

public class ModifyFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File file = new File("/root/Desktop/Test/test");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			out.write(6);
			out.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
