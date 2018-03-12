/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFComment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.wiitrans.base.file.font.TagFont;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.frag.XWPFFragmentation;
import com.wiitrans.base.file.sentence.XWPFSentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

// .DOCX

// [POI]
// XWPFDocument->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFHeader->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFFooter->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFFootnote->XWPFParagraphs->XWPFRun->getText/setText
// XWPFDocument->XWPFTable->XWPFTableRow->XWPFTableCell->XWPFParagraphs->XWPFRun->getText/setText

// [UFile]
// File->Frag(fragType=Header/Footer..., fragIndex=ParagraphIndex)->Run->Sentence

public class XWPFFile extends BiliFile {

	private XWPFDocument _doc = null;

	public XWPFFile() {
		_fileType = ENTITY_FILE_TYPE.XWPF;
	}

	@Override
	protected Fragmentation NewFrag() {
		return new XWPFFragmentation();
	}

	@Override
	public int Parse() {
		int ret = Const.FAIL;

		if (_sourceFilePath != null) {
			FileInputStream fileHandle = null;

			try {
				fileHandle = new FileInputStream(new File(_sourceFilePath));
				_doc = new XWPFDocument(fileHandle);

				XNode fragEntry = new XNode("EntityFrags");
				_xmlDoc.SetRoot(new XNode("File"));
				_xmlDoc.GetRoot().AddChild(fragEntry);

				ret = ParseBody(fragEntry);
				if (Const.SUCCESS == ret) {
					ret = ParseTable(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseHeader(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseFooter(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseFootnote(fragEntry);
				}

				// if (Const.SUCCESS == ret) {
				// ret = ParseComment(fragEntry);
				// }

				_xmlDoc.GetRoot().SetAttr("sentencecount", _filesentencecount);
				_xmlDoc.GetRoot().SetAttr("wordcount", _filewordcount);

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

		return ret;
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
				_doc = new XWPFDocument(in);

				ret = CleanupBody();
				if (Const.SUCCESS == ret) {
					ret = CleanupTable();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupHeader();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFooter();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFootnote();
				}

				ostream = new ByteArrayOutputStream();
				out = new FileOutputStream(_targetFilePath, false);
				_doc.write(ostream);
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

	private int ParseBody(XNode node) {
		return ParseFromParagraph(_doc.getParagraphs(), FRAG_TYPE.BODY, node);
	}

	private int ParseTable(XNode node) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFTable> tables = _doc.getTables();
		for (XWPFTable table : tables) {
			List<XWPFTableRow> rows = table.getRows();
			for (XWPFTableRow row : rows) {
				List<XWPFTableCell> cells = row.getTableCells();
				for (XWPFTableCell cell : cells) {
					paras.addAll(cell.getParagraphs());
				}
			}
		}

		return ParseFromParagraph(paras, FRAG_TYPE.TABLE, node);
	}

	private int ParseHeader(XNode node) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFHeader> arrHeader = _doc.getHeaderList();
		for (XWPFHeader header : arrHeader) {
			paras.addAll(header.getParagraphs());
		}

		return ParseFromParagraph(paras, FRAG_TYPE.HEADER, node);
	}

	private int ParseFooter(XNode node) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFooter> arrFooter = _doc.getFooterList();
		// for (XWPFFooter footer : arrFooter) {
		// paras.addAll(footer.getParagraphs());
		// }
		for (XWPFFooter footer : arrFooter) {
			for (XWPFTable table : footer.getTables()) {
				List<XWPFTableRow> rows = table.getRows();
				for (XWPFTableRow row : rows) {
					List<XWPFTableCell> cells = row.getTableCells();
					for (XWPFTableCell cell : cells) {
						paras.addAll(cell.getParagraphs());
					}
				}
			}
		}

		return ParseFromParagraph(paras, FRAG_TYPE.FOOTER, node);
	}

	private int ParseFootnote(XNode node) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFootnote> arrFootnote = _doc.getFootnotes();
		for (XWPFFootnote footnote : arrFootnote) {
			paras.addAll(footnote.getParagraphs());
		}

		return ParseFromParagraph(paras, FRAG_TYPE.FOOTNOTE, node);
	}

	// private int ParseComment(XNode node) {
	//
	// ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
	// XWPFComment[] arrComment = _doc.getComments();
	// for (XWPFComment comment : arrComment) {
	// // String aa = comment.getText();
	// // System.out.print(aa);
	// }
	//
	// return ParseFromParagraph(paras, FRAG_TYPE.FOOTNOTE, node);
	// }

	private int ParseFromParagraph(List<XWPFParagraph> arrPara, FRAG_TYPE type,
			XNode node) {
		int ret = Const.FAIL;

		int entityFragIndex = 0;
		for (XWPFParagraph para : arrPara) {

			XNode fragNode = new XNode("Frag");
			XWPFFragmentation frag = new XWPFFragmentation();
			frag._wordCount = 0;
			frag._fragIndex = entityFragIndex;
			frag._sentenceCount = 0;
			frag._fragType = type;

			// Text in all runs.
			StringBuilder runsText = new StringBuilder();

			// 2.XWPFRun
			int runIndex = 0;
			List<XWPFRun> arrRuns = para.getRuns();
			ArrayList<TagFont> tagFontList = new ArrayList<TagFont>();
			TagFont tagFont;
			// for (XWPFRun run : arrRuns) {
			for (int i = 0; i < arrRuns.size(); ++i) {
				XWPFRun run = arrRuns.get(i);
				String runTxt = run.getText(0);
				if (runTxt != null) {
					runTxt = _xmlcharutil.Encode(runTxt);

					// Content is empty.
					if (runTxt == null) {
						Log4j.log(String.format(
								"%s paragraph index %d run index %d is empty.",
								_sourceFilePath, entityFragIndex, runIndex));
					} else {
						// Content is not empty.
						if (runIndex > 0) {
							runsText.append(_tagId);
						}
						runsText.append(runTxt);
						++runIndex;
					}
				}
				tagFont = new TagFont();
				XNode fontNode = new XNode("Font");
				tagFont._index = i;
				tagFont._family = run.getFontFamily();
				tagFont._size = run.getFontSize();
				tagFont._bold = run.isBold();
				tagFont._italic = run.isItalic();
				tagFont._underline = run.getUnderline().toString();
				tagFont._color = run.getColor();
				tagFont._straike = run.isStrike();
				tagFontList.add(tagFont);
				tagFont.SetNode(fontNode);
				fragNode.AddChild(fontNode);
			}

			// frag._fontList = tagFontList;

			// 3.Fill node.
			int sentenceIndex = 0;

			// ArrayList<String> sentences =
			// _sourceLang.AnalyseSentence(runsText
			// .toString());

			ArrayList<String> sentences = null;

			ArrayList<Content> sents = new ArrayList<Content>();

			Content content;

			for (String sentence : sentences) {
				content = new Content();
				if (sentence != null && sentence.length() > 0) {
					content._tagcount = this.GetTagCount(sentence);

					content._content = _fileutil.TagPair(sentence,
							content._tagcount, _tagId.charAt(0));
					content._count = (short) (_sourceLang
							.AnalyseWord(_xmlcharutil.Decode(sentence)).size());

					content._hashcode = Util.GetHashCode(sentence);

				}
				sents.add(content);
			}

			if ((sents != null) && (!sents.isEmpty())) {
				int fragcount = 0;
				for (Content sent : sents) {
					XNode sentNode = new XNode("Sentence");
					XWPFSentence xs = new XWPFSentence();
					// xs._state = _state;
					xs._entityFragIndex = entityFragIndex;
					xs._entitySentenceIndex = sentenceIndex++;
					xs._source = sent._content;
					xs._sourceWordCount = sent._count;
					// xs._sourceTagCount = sent._tagcount;
					xs._hashcode = sent._hashcode;
					fragcount += sent._count;
					xs.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(xs);

				}

				frag._wordCount = fragcount;
				frag._fragIndex = entityFragIndex;
				frag._sentenceCount = frag._sentences.size();
				frag._fragType = type;

				// _entityFrags.add(frag);

				// _filesentencecount += frag._sentences.size();
				_filewordcount += fragcount;

			} else {
				// Log4j.error("Sentence is null or empty.");
			}
			_entityFrags.add(frag);
			_filesentencecount += frag._sentences.size();
			frag.SetNode(fragNode);
			node.AddChild(fragNode);

			entityFragIndex++;

		}
		_entityFragCount = _entityFrags.size();
		ret = Const.SUCCESS;

		return ret;
	}

	private int CleanupBody() {
		return CleanupToParagraph(_doc.getParagraphs(),
				GetFragsWithType(FRAG_TYPE.BODY));
	}

	private int CleanupTable() {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFTable> tables = _doc.getTables();
		for (XWPFTable table : tables) {
			List<XWPFTableRow> rows = table.getRows();
			for (XWPFTableRow row : rows) {
				List<XWPFTableCell> cells = row.getTableCells();
				for (XWPFTableCell cell : cells) {
					paras.addAll(cell.getParagraphs());
				}
			}
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.TABLE));
	}

	private int CleanupHeader() {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFHeader> arrHeader = _doc.getHeaderList();
		for (XWPFHeader header : arrHeader) {
			paras.addAll(header.getParagraphs());
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.HEADER));
	}

	private int CleanupFooter() {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFooter> arrFooter = _doc.getFooterList();
		// for (XWPFFooter footer : arrFooter) {
		// paras.addAll(footer.getParagraphs());
		// }

		for (XWPFFooter footer : arrFooter) {
			for (XWPFTable table : footer.getTables()) {
				List<XWPFTableRow> rows = table.getRows();
				for (XWPFTableRow row : rows) {
					List<XWPFTableCell> cells = row.getTableCells();
					for (XWPFTableCell cell : cells) {
						paras.addAll(cell.getParagraphs());
					}
				}
			}
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.FOOTER));
	}

	private int CleanupFootnote() {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFootnote> arrFootnote = _doc.getFootnotes();
		for (XWPFFootnote footnote : arrFootnote) {
			paras.addAll(footnote.getParagraphs());
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.FOOTNOTE));
	}

	private int CleanupToParagraph(List<XWPFParagraph> arrPara,
			ArrayList<Fragmentation> frags) {
		int ret = Const.FAIL;

		if ((arrPara != null) && (frags != null)) {
			int paragraphCount = arrPara.size();

			// 1.XWPFParagraph
			for (int index = 0; index < paragraphCount; ++index) {

				XWPFParagraph para = arrPara.get(index);
				if (para != null) {

					String paraSource = para.getText();

					if (paraSource == null) {
						continue;
					}

					XWPFFragmentation frag = (XWPFFragmentation) frags
							.get(index);

					ArrayList<String> fragText = getFragContent(frag, 0,
							frag._sentenceCount);

					if ((fragText != null) && (2 == fragText.size())) {
						String runsSource = fragText.get(0);
						String runsTarget = fragText.get(1);

						ArrayList<ReplaceMeta> arrMeta = ContentToMeta(
								runsSource, runsTarget);

						ArrayList<ReplaceMeta> arrRunMeta = new ArrayList<ReplaceMeta>();

						int metaIndex = 0;
						List<XWPFRun> runs = para.getRuns();
						for (XWPFRun run : runs) {

							String sourceText = run.getText(0);
							sourceText = _xmlcharutil.Encode(sourceText);
							if (sourceText != null) {
								ReplaceMeta meta = arrMeta.get(metaIndex);
								if (0 == meta._sourceText.trim().compareTo(
										sourceText.trim())) {
									if (!meta._sourceText
											.equals(meta._targetText)) {
										if (meta._sourceText.trim().length() > 0
												&& sourceText.trim().length() > 0) {
											meta._obj = run;
											arrRunMeta.add(meta);
										}
									}
									metaIndex++;
								}
							}

						}

						// 3.Replace the old text in XWPFRun.
						for (int arrIndex = arrRunMeta.size() - 1; arrIndex >= 0; --arrIndex) {
							ReplaceMeta meta = arrRunMeta.get(arrIndex);
							((XWPFRun) (meta._obj)).setText(
									_xmlcharutil.Decode(meta._targetText), 0);
							// ((XWPFRun) (meta._obj)).setText(
							// _xmlcharutil.Decode("11111"), 0);
						}
					}
				}
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}
}
