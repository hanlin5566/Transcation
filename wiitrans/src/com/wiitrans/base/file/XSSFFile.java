package com.wiitrans.base.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.wiitrans.base.file.BiliFile.ENTITY_FILE_TYPE;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.XSSFFragmentation;
import com.wiitrans.base.file.sentence.HSSFSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.XSSFSentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

// .XLSX

//[POI]
//Sheet->Row->Cell
//XSSFWorkbook->XSSFSheet->XSSFRow->XSSFCell->XSSFRichTextString->RichTextString
//XSSFWorkbook->XSSFSheet->XSSFRow->XSSFCell->XSSFComment->XSSFRichTextString->RichTextString

//[UFile]
//File->Frag(fragType=Sheet, fragIndex=SheetIndex)->Sentence(RichTextString/)

public class XSSFFile extends SSFFile {

	private XSSFWorkbook _book = null;

	public XSSFFile() {
		_fileType = ENTITY_FILE_TYPE.XSSF;
	}

	@Override
	protected Fragmentation NewFrag() {
		return new XSSFFragmentation();
	}

	@Override
	protected Sentence NewSentence() {
		return new XSSFSentence();
	}

	@Override
	public Workbook GetBook() {
		if (_book == null) {
			if (_sourceFilePath != null) {
				FileInputStream fileHandle = null;

				try {
					fileHandle = new FileInputStream(new File(_sourceFilePath));
					_book = new XSSFWorkbook(fileHandle);

				} catch (Exception e) {
					Log4j.error(e);

				} finally {
					if (fileHandle != null) {
						try {
							fileHandle.close();

						} catch (Exception e) {
							Log4j.error(e);
						}
						fileHandle = null;
					}
				}
			}
		}
		return _book;
	}

	@Override
	public short GetFontIndex(RichTextString rtString, int fontIndex) {
		XSSFRichTextString hrtString = (XSSFRichTextString) rtString;
		XSSFFont font = hrtString.getFontOfFormattingRun(fontIndex);
		return font.getIndex();
	}

	@Override
	public RichTextString NewRichTextString(String newTxt) {
		return new XSSFRichTextString(newTxt);
	}

	@Override
	public int Cleanup() {

		int ret = Const.FAIL;

		if ((_sourceFilePath != null) && (_targetFilePath != null)) {
			FileInputStream in = null;
			ByteArrayOutputStream ostream = null;
			FileOutputStream out = null;

			try {
				in = new FileInputStream(new File(_sourceFilePath));
				XSSFWorkbook book = new XSSFWorkbook(in);

				ret = CleanupBook(book);

				ostream = new ByteArrayOutputStream();
				out = new FileOutputStream(_targetFilePath, false);
				book.write(ostream);
				out.write(ostream.toByteArray());

			} catch (Exception e) {
				Log4j.error(e);

			} finally {
				if (in != null) {
					try {
						in.close();

					} catch (Exception e) {
						Log4j.error(e);
					}
					in = null;
				}

				if (out != null) {
					try {
						out.close();

					} catch (Exception e) {
						Log4j.error(e);
					}
					out = null;
				}

				if (ostream != null) {
					try {
						ostream.close();

					} catch (Exception e) {
						Log4j.error(e);
					}
					ostream = null;
				}
			}
		} else {
			Log4j.error("Path is not exist.");
		}

		return ret;
	}
}
