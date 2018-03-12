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

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

import com.wiitrans.base.file.BiliFile.FILTER_POSITION;
import com.wiitrans.base.file.BiliFile.ReplaceMeta;
//import com.wiitrans.base.file.FileConst.FILE_LANGUAGE;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HWPFFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.DetectLanguage;
import com.wiitrans.base.file.sentence.HWPFSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

// .DOC

// [POI]
// HWPFDocument->Different Ranges->Paragraph->Run->getText/replaceText

// [UFile]
// File->Frag(fragType=Range, fragIndex=ParagraphIndex)->Sentence

public class HWPFFile extends BiliFile {

	private HWPFDocument _doc = null;

	public HWPFFile() {
		_fileType = ENTITY_FILE_TYPE.HWPF;
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
	public int Cleanup() {
		int ret = Const.FAIL;

		if ((_sourceFilePath != null) && (_targetFilePath != null)) {
			FileInputStream in = null;
			ByteArrayOutputStream ostream = null;
			FileOutputStream out = null;

			try {
				in = new FileInputStream(new File(_sourceFilePath));
				_doc = new HWPFDocument(in);

				ret = CleanupBody();
				if (Const.SUCCESS == ret) {
					ret = CleanupTextBox();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupHeaderFooter();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupEndnote();
				}
				if (Const.SUCCESS == ret) {
					ret = CleanupFootnote();
				}

				if (Const.SUCCESS == ret) {
					ret = CleanupComment();
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

				// Text in all runs.
				StringBuilder runsText = new StringBuilder();
				int runCount = para.numCharacterRuns();
				// 2.CharacterRun
				for (int runIndex = 0; runIndex < runCount; ++runIndex) {
					CharacterRun run = para.getCharacterRun(runIndex);
					if (run != null) {
						String runTxt = run.text();
						// runTxt = _xmlcharutil.Encode(FilterText(runTxt,
						// "\r\n",
						// FILTER_POSITION.EMPTY));
						runTxt = _xmlcharutil.Encode(runTxt);
						// Content is empty.
						if (runTxt == null) {
							Log4j.log(String
									.format("%s paragraph index %d run index %d is empty.",
											_sourceFilePath, index, runIndex));
						} else {
							// Content is not empty.
							if (runIndex > 0) {
								runsText.append(_tagId);
							}
							runsText.append(runTxt);
						}
					}
				}

				// 3.Fill node.
				int sentenceIndex = 0;
				// ArrayList<String> sentences = _sourceLang
				// .AnalyseSentence(runsText.toString());

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
								.AnalyseWord(_xmlcharutil.Decode(sentence))
								.size());

						content._hashcode = Util.GetHashCode(sentence);
					}

					sents.add(content);
				}

				if ((sents != null) && (!sents.isEmpty())) {
					int fragcount = 0;
					for (Content sent : sents) {
						XNode sentNode = new XNode("Sentence");
						HWPFSentence hs = new HWPFSentence();
						//hs._state = _state;
						hs._entityFragIndex = index;
						hs._entitySentenceIndex = sentenceIndex++;
						hs._source = sent._content;
						hs._sourceWordCount = sent._count;
						// hs._sourceTagCount = sent._tagcount;
						hs._hashcode = sent._hashcode;
						fragcount += sent._count;
						hs.SetNode(sentNode);
						fragNode.AddChild(sentNode);

						frag._sentences.add(hs);

					}

					frag._wordCount = fragcount;
					frag._fragIndex = index;
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

			} else {

				Log4j.error(String.format("%s get paragraph failed.",
						_sourceFilePath));
			}

		}
		_entityFragCount = _entityFrags.size();

		ret = Const.SUCCESS;

		return ret;
	}

	private int CleanupTextBox() {
		return CleanupToRange(_doc.getMainTextboxRange(),
				GetFragsWithType(FRAG_TYPE.TEXTBOX));
	}

	private int CleanupHeaderFooter() {
		return CleanupToRange(_doc.getHeaderStoryRange(),
				GetFragsWithType(FRAG_TYPE.HEADER_FOOTER));
	}

	private int CleanupEndnote() {
		return CleanupToRange(_doc.getEndnoteRange(),
				GetFragsWithType(FRAG_TYPE.ENDNOTE));
	}

	private int CleanupFootnote() {
		return CleanupToRange(_doc.getFootnoteRange(),
				GetFragsWithType(FRAG_TYPE.FOOTNOTE));
	}

	private int CleanupBody() {
		return CleanupToRange(_doc.getRange(), GetFragsWithType(FRAG_TYPE.BODY));
	}

	private int CleanupComment() {
		return CleanupToRange(_doc.getCommentsRange(),
				GetFragsWithType(FRAG_TYPE.COMMENT));
	}

	protected int CleanupToRange(Range range, ArrayList<Fragmentation> frags) {
		int ret = Const.FAIL;

		if ((range != null) && (frags != null)) {
			ArrayList<ReplaceMeta> arrRunMeta = new ArrayList<ReplaceMeta>();

			int paragraphCount = range.numParagraphs();

			for (int index = 0; index < paragraphCount; ++index) {

				Paragraph para = range.getParagraph(index);
				if (para != null) {
					String paraSource = para.text();

					if (paraSource == null || paraSource.trim().length() == 0) {
						continue;
					}

					HWPFFragmentation frag = (HWPFFragmentation) frags
							.get(index);

					ArrayList<String> fragText = getFragContent(frag, 0,
							frag._sentenceCount);

					if ((fragText != null) && (2 == fragText.size())) {
						String runsSource = fragText.get(0);
						String runsTarget = fragText.get(1);

						ArrayList<ReplaceMeta> arrMeta = ContentToMeta(
								runsSource, runsTarget);

						if (arrMeta != null && arrMeta.size() > 0) {
							int runCount = para.numCharacterRuns();
							// int metaIndex = 0;
							// 2.CharacterRun
							for (int runIndex = 0; runIndex < runCount; ++runIndex) {
								// if (runIndex < arrMeta.size()) {
								CharacterRun run = para
										.getCharacterRun(runIndex);
								String sourceText = run.text();
								// sourceText = _xmlcharutil
								// .Encode(FilterText(sourceText,
								// "\r\n",
								// FILTER_POSITION.EMPTY));
								sourceText = _xmlcharutil.Encode(sourceText);
								if (sourceText != null) {

									ReplaceMeta meta = arrMeta.get(runIndex);
									if (!meta._sourceText
											.equals(meta._targetText)) {
										if (meta._sourceText.trim().length() > 0
												&& sourceText.trim().length() > 0
												&& 0 == meta._sourceText
														.trim()
														.compareTo(
																sourceText
																		.trim())) {
											meta._obj = run;
											arrRunMeta.add(meta);
											// metaIndex++;
										}
									}
								}
								// }
							}

						}
					}
				}
			}

			// 3.Replace the old text in CharacterRun.
			for (int arrIndex = arrRunMeta.size() - 1; arrIndex >= 0; --arrIndex) {
				ReplaceMeta meta = arrRunMeta.get(arrIndex);
				if (_xmlcharutil.Decode(meta._sourceText).trim().length() > 0) {
					((CharacterRun) (meta._obj)).replaceText(meta._sourceText,
							_xmlcharutil.Decode(meta._targetText), 0);
					// ((CharacterRun)
					// (meta._obj)).replaceText(meta._sourceText,
					// "abcd", 0);
				}
			}

			// if (arrRunMeta.size() == 2) {
			// CharacterRun run0 = ((CharacterRun) (arrRunMeta.get(0)._obj));
			// CharacterRun run1 = ((CharacterRun) (arrRunMeta.get(1)._obj));
			// String text = run1.text();
			// CharacterProperties properties = run1.cloneProperties();
			// run1.delete();

			// byte highlight = run0.getHighlightedColor();
			// int underline = run0.getUnderlineCode();
			// boolean markeddeleted = run0.isMarkedDeleted();
			// int color = run0.getColor();
			//
			// run0.setHighlighted(run1.getHighlightedColor());
			// run0.setUnderlineCode(run1.getUnderlineCode());
			// run0.markDeleted(run1.isMarkedDeleted());
			// run0.setColor(run1.getColor());
			//
			// run1.setHighlighted(highlight);
			// run1.setUnderlineCode(underline);
			// run1.markDeleted(markeddeleted);
			// run1.setColor(color);

			// }
		}

		ret = Const.SUCCESS;

		return ret;
	}
}
