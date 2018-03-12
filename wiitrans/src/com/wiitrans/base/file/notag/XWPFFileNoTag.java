package com.wiitrans.base.file.notag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.XWPFFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.LangSentence;
import com.wiitrans.base.file.sentence.XWPFSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class XWPFFileNoTag extends BiliFileNoTag {

	private XWPFDocument _doc = null;

	public XWPFFileNoTag() {
		_fileType = ENTITY_FILE_TYPE.XWPF;
	}

	@Override
	public int UnInit() {
		_doc = null;
		return super.UnInit();
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
	public int Cleanup(SENTENCE_STATE state) {
		int ret = Const.FAIL;

		if ((_sourceFilePath != null) && (_targetFilePath != null)) {
			FileInputStream in = null;
			ByteArrayOutputStream ostream = null;
			FileOutputStream out = null;

			try {
				in = new FileInputStream(new File(_sourceFilePath));
				_doc = new XWPFDocument(in);

				ret = CleanupBody(state);
				if (Const.SUCCESS == ret) {
					ret = CleanupTable(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupHeader(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFooter(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFootnote(state);
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

			// 3.Fill node.
			int sentenceIndex = 0;
			// ArrayList<Content> sents = Split(runsText.toString());

			ArrayList<LangSentence> sentences = _sourceLang
					.AnalyseSentence(para.getText());

			ArrayList<Content> sents = new ArrayList<Content>();

			Content content;

			for (LangSentence sentence : sentences) {
				content = new Content();

				if (sentence != null && sentence.text.length() > 0) {
					content._tagcount = 0;

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
					xs._valid = sent._valid;
					fragcount += sent._count;
					xs.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(xs);

				}

				frag._wordCount = fragcount;
				frag._fragIndex = entityFragIndex;
				frag._sentenceCount = frag._sentences.size();
				frag._fragType = type;

				_filewordcount += fragcount;

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

	private int CleanupBody(SENTENCE_STATE state) {
		return CleanupToParagraph(_doc.getParagraphs(),
				GetFragsWithType(FRAG_TYPE.BODY), state);
	}

	private int CleanupTable(SENTENCE_STATE state) {

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

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.TABLE),
				state);
	}

	private int CleanupHeader(SENTENCE_STATE state) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFHeader> arrHeader = _doc.getHeaderList();
		for (XWPFHeader header : arrHeader) {
			paras.addAll(header.getParagraphs());
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.HEADER),
				state);
	}

	private int CleanupFooter(SENTENCE_STATE state) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFooter> arrFooter = _doc.getFooterList();

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

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.FOOTER),
				state);
	}

	private int CleanupFootnote(SENTENCE_STATE state) {

		ArrayList<XWPFParagraph> paras = new ArrayList<XWPFParagraph>();
		List<XWPFFootnote> arrFootnote = _doc.getFootnotes();
		for (XWPFFootnote footnote : arrFootnote) {
			paras.addAll(footnote.getParagraphs());
		}

		return CleanupToParagraph(paras, GetFragsWithType(FRAG_TYPE.FOOTNOTE),
				state);
	}

	private int CleanupToParagraph(List<XWPFParagraph> arrPara,
			ArrayList<Fragmentation> frags, SENTENCE_STATE state) {
		int ret = Const.FAIL;

		if ((arrPara != null) && (frags != null)) {
			int paragraphCount = arrPara.size();

			// 1.XWPFParagraph
			for (int index = 0; index < paragraphCount; ++index) {

				XWPFParagraph para = arrPara.get(index);
				if (para != null) {

					String paraSource = para.getText();

					XWPFFragmentation frag = (XWPFFragmentation) frags
							.get(index);

					ArrayList<String> fragText = getFragContent(frag, 0,
							frag._sentenceCount, state);

					if ((fragText != null) && (2 == fragText.size())) {
						String runsSource = fragText.get(0);
						String runsTarget = fragText.get(1);
						if (!runsSource.equals(runsTarget)) {
							if (runsSource.trim().length() > 0
									&& paraSource.trim().length() > 0
									&& 0 == runsSource.trim().compareTo(
											paraSource.trim())) {
								// while (para.getRuns().size() > 0) {
								// para.removeRun(0);
								// }
								List<XWPFRun> list = para.getRuns();
								XWPFRun docxrun;
								for (int i = list.size() - 1; i >= 0; --i) {
									if (!para.removeRun(i)) {
										docxrun = list.get(i);
										if (docxrun != null) {
											// docxrun.removeBreak();
											// docxrun.removeCarriageReturn();
											// docxrun.removeTab();
											docxrun.setText("");
											// String aaa = docxrun.getText(0);
											// System.out.print(aaa);

										}
									}
								}
								XWPFRun run = para.createRun();
								run.setText(runsTarget);
								para.addRun(run);
							}
						}
					}
				}
			}
		}

		ret = Const.SUCCESS;

		return ret;
	}
}