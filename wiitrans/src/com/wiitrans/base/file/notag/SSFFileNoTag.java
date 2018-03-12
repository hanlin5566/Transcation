package com.wiitrans.base.file.notag;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.LangSentence;
import com.wiitrans.base.file.sentence.HSSFSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class SSFFileNoTag extends BiliFileNoTag {

	public Workbook GetBook() {
		return null;
	}

	@Override
	public int Parse() {

		int ret = Const.FAIL;

		Workbook book = GetBook();
		if (book != null) {
			ParseBook(book);
		}

		return ret;
	}

	private int ParseBook(Workbook book) {

		int ret = Const.FAIL;

		XNode fragEntry = new XNode("EntityFrags");
		_xmlDoc.SetRoot(new XNode("File"));
		_xmlDoc.GetRoot().AddChild(fragEntry);

		int sheetCount = book.getNumberOfSheets();
		for (int fragIndex = 0; fragIndex < sheetCount; ++fragIndex) {
			Sheet sheet = book.getSheetAt(fragIndex);
			ret = ParseSheet(sheet, fragIndex, fragEntry);
			if (ret != Const.SUCCESS) {
				break;
			}
			_entityFragCount++;
		}

		_xmlDoc.GetRoot().SetAttr("sentencecount", _filesentencecount);
		_xmlDoc.GetRoot().SetAttr("wordcount", _filewordcount);

		ret = Const.SUCCESS;

		return ret;
	}

	private int ParseSheet(Sheet sheet, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.SHEET;

		for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet
				.getLastRowNum(); ++rowIndex) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				ret = ParseRow(row, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}
			} else {
				ret = Const.SUCCESS;
			}
		}

		if (ret == Const.SUCCESS) {
			_filesentencecount += frag._sentences.size();
			_entityFrags.add(frag);
			frag.SetNode(fragNode);
			fragEntry.AddChild(fragNode);
		}

		// ret = Const.SUCCESS;

		return ret;
	}

	private int ParseRow(Row row, int fragIndex, Fragmentation frag,
			XNode fragNode) {
		int ret = Const.FAIL;
		for (int cellIndex = row.getFirstCellNum(); cellIndex < row
				.getLastCellNum(); ++cellIndex) {
			Cell cell = row.getCell(cellIndex);
			if (cell != null) {
				ret = ParseCell(cell, cellIndex, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int ParseCell(Cell cell, int rowIndex, int fragIndex,
			Fragmentation frag, XNode fragNode) {
		int ret = Const.FAIL;

		int type = cell.getCellType();
		switch (type) {
		case Cell.CELL_TYPE_STRING: {

			RichTextString rtString = cell.getRichStringCellValue();
			ret = ParseRichTextString(rtString, fragIndex, frag, fragNode);
			break;
		}
		default: {
			ret = Const.SUCCESS;
			break;
		}
		}

		return ret;
	}

	private int ParseRichTextString(RichTextString rtString, int fragIndex,
			Fragmentation frag, XNode fragNode) {
		int ret = Const.FAIL;
		if (rtString != null) {
			ArrayList<LangSentence> sentences = _sourceLang
					.AnalyseSentence(rtString.getString());

			ArrayList<Content> sents = new ArrayList<Content>();

			Content content;

			for (LangSentence sentence : sentences) {
				content = new Content();

				if (sentence != null && sentence.text.length() > 0) {
					content._tagcount = 0;// this.GetTagCount(sentence);
					content._valid = sentence.valid;
					if (sentence.valid) {
						content._content = sentence.text;
						content._count = (short) (_sourceLang
								.AnalyseWord(sentence.text).size());

						content._hashcode = Util.GetHashCode(sentence.text);
					} else {
						content._content = sentence.code;

						content._count = 0;

						content._hashcode = 0;
					}
				}

				sents.add(content);
			}

			if ((sents != null) && (!sents.isEmpty())) {
				int cellcount = 0;
				for (Content sent : sents) {
					XNode sentNode = new XNode("Sentence");
					Sentence hs = NewSentence();
					// hs._state = _state;
					hs._entityFragIndex = fragIndex;
					hs._entitySentenceIndex = frag._sentences.size();
					hs._source = sent._content;
					hs._sourceWordCount = sent._count;
					// hs._sourceTagCount = sent._tagcount;
					hs._hashcode = sent._hashcode;
					hs._valid = sent._valid;
					cellcount += sent._count;
					hs.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(hs);

				}

				frag._wordCount += cellcount;
				frag._fragIndex = fragIndex;
				frag._sentenceCount = frag._sentences.size();
				frag._fragType = FRAG_TYPE.SHEET;

				_filewordcount += cellcount;

			}
		}
		ret = Const.SUCCESS;

		return ret;
	}

	public int CleanupBook(Workbook book, SENTENCE_STATE state) {

		int ret = Const.FAIL;

		int sheetCount = book.getNumberOfSheets();
		for (int fragIndex = 0; fragIndex < sheetCount; ++fragIndex) {
			Sheet sheet = book.getSheetAt(fragIndex);
			ret = CleanupSheet(sheet, fragIndex, state);
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		return ret;
	}

	public int CleanupSheet(Sheet sheet, int fragIndex, SENTENCE_STATE state) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = _entityFrags.get(fragIndex);

		// 1. Sheel
		for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet
				.getLastRowNum(); ++rowIndex) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				// 2. Row
				for (int cellIndex = row.getFirstCellNum(); cellIndex < row
						.getLastCellNum(); ++cellIndex) {
					// 3. Cell
					Cell cell = row.getCell(cellIndex);
					if (cell != null) {
						int type = cell.getCellType();
						switch (type) {
						// 目前只处理了单元格为String形式
						case Cell.CELL_TYPE_STRING: {

							RichTextString srcRtString = cell
									.getRichStringCellValue();

							String paraSource = srcRtString.getString();

							if (srcRtString != null) {
								ArrayList<LangSentence> sentences = _sourceLang
										.AnalyseSentence(paraSource);

								ArrayList<String> fragText = getFragContent(
										frag, sentOffset, sentences.size(),
										state);
								sentOffset = sentOffset + sentences.size();
								if ((fragText != null)
										&& (2 == fragText.size())) {
									String runsSource = fragText.get(0);
									String runsTarget = fragText.get(1);

									if (!runsSource.equals(runsTarget)) {
										if (runsSource.trim().length() > 0
												&& paraSource.trim().length() > 0
												&& 0 == runsSource
														.trim()
														.compareTo(
																paraSource
																		.trim())) {
											cell.setCellValue(runsTarget);
										}
									}

									// cell.setCellValue("1111111");
								}
							}
							break;
						}
						default: {

							// ret = Const.SUCCESS;
							break;
						}
						}
					}
				}
			} else {

				// ret = Const.SUCCESS;
			}
		}
		ret = Const.SUCCESS;
		return ret;
	}

	protected short GetFontIndex(RichTextString rtString, int fontIndex) {
		return 0;
	}

	protected RichTextString NewRichTextString(String newTxt) {
		return null;
	}

	protected Sentence NewSentence() {
		return new HSSFSentence();
	}
}