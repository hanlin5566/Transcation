package com.wiitrans.base.file.notag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HWPFFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.LangSentence;
import com.wiitrans.base.file.sentence.HWPFSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class HWPFFileNoTag extends BiliFileNoTag {

	private HWPFDocument _doc = null;

	public HWPFFileNoTag() {
		_fileType = ENTITY_FILE_TYPE.HWPF;
	}

	@Override
	public int UnInit() {
		_doc = null;
		return super.UnInit();
	}

	@Override
	protected Fragmentation NewFrag() {
		return new HWPFFragmentation();
	}

	@Override
	public int Parse() {
		int ret = Const.FAIL;

		if (_sourceFilePath != null) {
			FileInputStream fileHandle = null;

			try {
				fileHandle = new FileInputStream(new File(_sourceFilePath));
				_doc = new HWPFDocument(fileHandle);

				XNode fragEntry = new XNode("EntityFrags");
				_xmlDoc.SetRoot(new XNode("File"));
				_xmlDoc.GetRoot().AddChild(fragEntry);

				ret = ParseBody(fragEntry);
				if (Const.SUCCESS == ret) {
					ret = ParseTextBox(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseHeaderFooter(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseEndnote(fragEntry);
				}
				if (Const.SUCCESS == ret) {
					ret = ParseFootnote(fragEntry);
				}

				if (Const.SUCCESS == ret) {
					ret = ParseComment(fragEntry);
				}

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
				_doc = new HWPFDocument(in);

				ret = CleanupBody(state);
				if (Const.SUCCESS == ret) {
					ret = CleanupTextBox(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupHeaderFooter(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupEndnote(state);
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFootnote(state);
				}

				if (Const.SUCCESS == ret) {
					ret = CleanupComment(state);
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

	private int ParseTextBox(XNode node) {
		return ParseFromRange(_doc.getMainTextboxRange(), FRAG_TYPE.TEXTBOX,
				node);
	}

	private int ParseHeaderFooter(XNode node) {
		return ParseFromRange(_doc.getHeaderStoryRange(),
				FRAG_TYPE.HEADER_FOOTER, node);
	}

	private int ParseEndnote(XNode node) {
		return ParseFromRange(_doc.getEndnoteRange(), FRAG_TYPE.ENDNOTE, node);
	}

	private int ParseFootnote(XNode node) {
		return ParseFromRange(_doc.getFootnoteRange(), FRAG_TYPE.FOOTNOTE, node);
	}

	private int ParseBody(XNode node) {
		return ParseFromRange(_doc.getRange(), FRAG_TYPE.BODY, node);
	}

	private int ParseComment(XNode node) {
		return ParseFromRange(_doc.getCommentsRange(), FRAG_TYPE.COMMENT, node);
	}

	protected int ParseFromRange(Range range, FRAG_TYPE type, XNode node) {
		int ret = Const.FAIL;
		// All the paragraph index and sentence index are based on 1 in any
		// Range.
		// int entityFragIndex = 1;
		int paragraphCount = range.numParagraphs();
		// 1.Paragraph
		for (int index = 0; index < paragraphCount; ++index) {

			Paragraph para = range.getParagraph(index);
			if (para != null) {
				XNode fragNode = new XNode("Frag");
				Fragmentation frag = NewFrag();
				frag._wordCount = 0;
				frag._fragIndex = index;
				frag._sentenceCount = 0;
				frag._fragType = type;

				// 3.Fill node.
				int sentenceIndex = 0;
				// String a = para.text();
				ArrayList<LangSentence> sentences = _sourceLang
						.AnalyseSentence(para.text());

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
						HWPFSentence hs = new HWPFSentence();
						// hs._state = _state;
						hs._entityFragIndex = index;
						hs._entitySentenceIndex = sentenceIndex++;
						hs._source = sent._content;
						hs._sourceWordCount = sent._count;
						// hs._sourceTagCount = sent._tagcount;
						hs._hashcode = sent._hashcode;
						hs._valid = sent._valid;
						fragcount += sent._count;
						hs.SetNode(sentNode);
						fragNode.AddChild(sentNode);

						frag._sentences.add(hs);

					}

					frag._wordCount = fragcount;
					frag._fragIndex = index;
					frag._sentenceCount = frag._sentences.size();
					frag._fragType = type;

					_filewordcount += fragcount;

				}

				_entityFrags.add(frag);
				_filesentencecount += frag._sentences.size();
				frag.SetNode(fragNode);
				node.AddChild(fragNode);

			} else {

				Log4j.error(String.format("%s get paragraph failed.",
						_sourceFilePath));
			}

		}
		_entityFragCount = _entityFrags.size();

		ret = Const.SUCCESS;

		return ret;
	}

	private int CleanupTextBox(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getMainTextboxRange(),
				GetFragsWithType(FRAG_TYPE.TEXTBOX), state);
	}

	private int CleanupHeaderFooter(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getHeaderStoryRange(),
				GetFragsWithType(FRAG_TYPE.HEADER_FOOTER), state);
	}

	private int CleanupEndnote(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getEndnoteRange(),
				GetFragsWithType(FRAG_TYPE.ENDNOTE), state);
	}

	private int CleanupFootnote(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getFootnoteRange(),
				GetFragsWithType(FRAG_TYPE.FOOTNOTE), state);
	}

	private int CleanupBody(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getRange(),
				GetFragsWithType(FRAG_TYPE.BODY), state);
	}

	private int CleanupComment(SENTENCE_STATE state) {
		return CleanupToRange(_doc.getCommentsRange(),
				GetFragsWithType(FRAG_TYPE.COMMENT), state);
	}

	protected int CleanupToRange(Range range, ArrayList<Fragmentation> frags,
			SENTENCE_STATE state) {
		int ret = Const.FAIL;

		if ((range != null) && (frags != null)) {

			int paragraphCount = range.numParagraphs();

			for (int index = 0; index < paragraphCount; ++index) {

				Paragraph para = range.getParagraph(index);
				if (para != null) {
					String paraSource = para.text();

					HWPFFragmentation frag = (HWPFFragmentation) frags
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
								// Log4j.debug(" hwpf index :" + index
								// + " ------source:" + runsSource
								// + " ----------target:" + runsTarget);
								para.replaceText(runsSource, runsTarget);
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
