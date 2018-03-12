package com.wiitrans.base.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HSSFFragmentation;
import com.wiitrans.base.file.sentence.HSSFSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

// .XLS

//[POI]
//Sheet->Row->Cell
//HSSFWorkbook->HSSFSheet->HSSFRow->HSSFCell->HSSFRichTextString->RichTextString
//HSSFWorkbook->HSSFSheet->HSSFRow->HSSFCell->HSSFComment->HSSFRichTextString->RichTextString

//[UFile]
//File->Frag(fragType=Sheet, fragIndex=SheetIndex)->Sentence()

public class HSSFFile extends SSFFile {
	
	private HSSFWorkbook _book = null;
	
	public HSSFFile() {
		_fileType = ENTITY_FILE_TYPE.HSSF;
	}

	@Override
	protected Fragmentation NewFrag() {
		return new HSSFFragmentation();
	}
	
	@Override
	protected Sentence NewSentence() {
		return new HSSFSentence();
	}
	
	@Override
	public Workbook GetBook()
	{
		if(_book == null)
		{
			if (_sourceFilePath != null) {
				FileInputStream fileHandle = null;

				try {
					fileHandle = new FileInputStream(new File(_sourceFilePath));
					_book = new HSSFWorkbook(fileHandle);

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
	public short GetFontIndex(RichTextString rtString, int fontIndex)
	{		
		HSSFRichTextString hrtString = (HSSFRichTextString)rtString;
		return hrtString.getFontOfFormattingRun(fontIndex);
	}
	
	@Override
	public RichTextString NewRichTextString(String newTxt)
	{
		return new HSSFRichTextString(newTxt);
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
				HSSFWorkbook book = new HSSFWorkbook(in);

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