package com.wiitrans.base.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.notag.HSLFFileNoTag;
import com.wiitrans.base.file.notag.HSSFFileNoTag;
import com.wiitrans.base.file.notag.HWPFFileNoTag;
import com.wiitrans.base.file.notag.TXTFileNoTag;
import com.wiitrans.base.file.notag.XSSFFileNoTag;
import com.wiitrans.base.file.notag.XWPFFileNoTag;
import com.wiitrans.base.file.sdlxliff.SDLXliffFileNoTag;
import com.wiitrans.base.file.ttx.TTXFileNoTag;

public class FileUtil {
    public String TagPair(String text, int tagCount, char tag) {
	String result = null;
	if (tagCount % 2 == 0) {
	    result = text;
	} else {
	    result = text + tag;
	}
	return result;
    }

    public String UnTagPair(String text, int tagCount) {
	String result = null;
	if (text != null) {
	    if (tagCount % 2 == 0) {
		result = text;
	    } else {
		if (text.length() > 0) {
		    result = text.substring(0, text.length() - 1);
		} else {
		    result = "";
		}
	    }
	}
	return result;
    }

    public String TagPair(String text) {
	String result = null;
	result = text;
	return result;
    }

    public String UnTagPair(String text) {
	String result = null;
	if (text != null) {
	    result = text;
	}
	return result;
    }

    public String GetExtFromFileName(String filename) {
	if (filename == null) {
	    return null;
	}
	String ext = null;
	try {
	    int index = filename.lastIndexOf('.');
	    if (index > 1) {
		ext = filename.substring(index + 1, filename.length());
	    }
	} catch (Exception e) {

	}
	return ext;
    }

    public int GetTagCount(String sentence, String tagID) {
	char[] tags = tagID.toCharArray();
	char[] charInSentence = sentence.toCharArray();
	int count = 0;
	for (char c : charInSentence) {
	    for (char tag : tags) {
		if (c == tag) {
		    count++;
		    break;
		}
	    }
	}
	return count;
    }

    public BiliFileNoTag GetBiliFileNoTagByExt(String ext) {
	BiliFileNoTag file = null;
	switch (ext.toLowerCase()) {
	case "doc": {
	    file = new HWPFFileNoTag();
	    break;
	}
	case "docx": {
	    file = new XWPFFileNoTag();
	    break;
	}
	case "ppt": {
	    file = new HSLFFileNoTag();
	    break;
	}
	case "pptx": {
	    // 暂时不支持pptx
	    // file = new XSLFFileNoTag();
	    break;
	}
	case "xls": {
	    file = new HSSFFileNoTag();
	    break;
	}
	case "xlsx": {
	    file = new XSSFFileNoTag();
	    break;
	}
	case "txt": {
	    file = new TXTFileNoTag();
	    break;
	}
	case "ttx": {
	    file = new TTXFileNoTag();
	    break;
	}
	case "sdlxliff": {
	    file = new SDLXliffFileNoTag();
	    break;
	}
	default:
	    break;
	}
	return file;
    }

    public BiliFile GetBiliFileByExt(String ext) {
	BiliFile file = null;
	switch (ext.toLowerCase()) {
	case "doc": {
	    file = new HWPFFile();
	    break;
	}
	case "docx": {
	    file = new XWPFFile();
	    break;
	}
	case "ppt": {
	    file = new HSLFFile();
	    break;
	}
	case "pptx": {
	    file = new XSLFFile();
	    break;
	}
	case "xls": {
	    file = new HSSFFile();
	    break;
	}
	case "xlsx": {
	    file = new XSSFFile();
	    break;
	}
	case "txt": {
	    file = new TXTFile();
	    break;
	}
	default:
	    break;
	}
	return file;
    }

    public static void copyFile(File source, File target) throws Exception{
	// //复制原文
	int buff = 0;
	InputStream in = new FileInputStream(source);
	OutputStream out = new FileOutputStream(target);
	byte[] bytes = new byte[1024];
	while ((buff = in.read(bytes)) != -1) {
	    out.write(bytes, 0, buff);
	}
	in.close();
	out.close();
    }
    
    public static void copyFileForTxt(File source, File target,String charSet) throws Exception{
	// //复制原文
	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source),charSet));
	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target),charSet));
	String str = null;
	while((str = reader.readLine()) != null){
	   str = str.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
	   writer.write(str);
	}
	writer.flush();
	reader.close();
	writer.close();
    }
}
