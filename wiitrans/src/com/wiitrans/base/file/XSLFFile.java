package com.wiitrans.base.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TableCell;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.wiitrans.base.file.BiliFile.ReplaceMeta;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.frag.XSLFFragmentation;
import com.wiitrans.base.file.sentence.XSLFSentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

// .PPTX

//[POI]
//XMLSlideShow->XSLFSlide->XSLFShape->XSLFTextShape->XSLFTextParagraph->XSLFTextRun->getText/setText
//									->XSLFTable->XSLFTableRow->XSLFTableCell->XSLFTextParagraph

//[UFile]
//File->Frag(fragType=XSLFSlide, fragIndex=SlideIndex)->Sentence

public class XSLFFile extends BiliFile {

	private XMLSlideShow _sliderShow = null;

	public XSLFFile() {
		_fileType = ENTITY_FILE_TYPE.XSLF;
	}

	@Override
	protected Fragmentation NewFrag() {
		return new XSLFFragmentation();
	}

	@Override
	public int Parse() {
		int ret = Const.FAIL;

		if (_sourceFilePath != null) {
			FileInputStream fileHandle = null;

			try {
				fileHandle = new FileInputStream(new File(_sourceFilePath));
				_sliderShow = new XMLSlideShow(fileHandle);

				// XNode fragEntry = new XNode("EntityFrags");
				// _xmlDoc.SetRoot(new XNode("File"));
				// _xmlDoc.GetRoot().AddChild(fragEntry);

				ret = ParseSlideShow(_sliderShow);

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

	private int ParseSlideShow(XMLSlideShow slideShow) {

		int ret = Const.FAIL;

		XNode fragEntry = new XNode("EntityFrags");
		_xmlDoc.SetRoot(new XNode("File"));
		_xmlDoc.GetRoot().AddChild(fragEntry);

		XSLFSlide[] slides = slideShow.getSlides();
		int slideCount = slides.length;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			ret = ParseSlide(slides[fragIndex], fragIndex, fragEntry);
			if (ret != Const.SUCCESS) {
				break;
			}
			_entityFragCount++;
		}

		XSLFNotes notes;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			notes = slideShow.getNotesSlide(slides[fragIndex]);
			ret = ParseNote(notes, fragIndex, fragEntry);
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

	private int ParseSlide(XSLFSlide slide, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.SLIDE;

		XSLFShape[] shapes = slide.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			XSLFShape shape = shapes[sharpIndex];
			if (shape instanceof XSLFTextShape) {
				List<XSLFTextParagraph> paras = ((XSLFTextShape) shape)
						.getTextParagraphs();
				for (XSLFTextParagraph para : paras) {
					ret = ParseTextParagraph(para, sharpIndex, fragIndex, frag,
							fragNode);
					if (ret != Const.SUCCESS) {
						break;
					}
				}
			} else if (shape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) shape;
				List<XSLFTableRow> rows = table.getRows();
				for (XSLFTableRow row : rows) {
					List<XSLFTableCell> cells = row.getCells();
					for (XSLFTableCell cell : cells) {
						List<XSLFTextParagraph> paras = cell
								.getTextParagraphs();
						for (XSLFTextParagraph para : paras) {
							ret = ParseTextParagraph(para, sharpIndex,
									fragIndex, frag, fragNode);
							if (ret != Const.SUCCESS) {
								break;
							}
						}
					}
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
			_filewordcount += frag._wordCount;
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int ParseNote(XSLFNotes note, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.NOTES;

		XSLFShape[] shapes = note.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			XSLFShape shape = shapes[sharpIndex];
			if (shape instanceof XSLFTextShape) {
				List<XSLFTextParagraph> paras = ((XSLFTextShape) shape)
						.getTextParagraphs();
				for (XSLFTextParagraph para : paras) {
					ret = ParseTextParagraph(para, sharpIndex, fragIndex, frag,
							fragNode);
					if (ret != Const.SUCCESS) {
						break;
					}
				}
			} else if (shape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) shape;
				List<XSLFTableRow> rows = table.getRows();
				for (XSLFTableRow row : rows) {
					List<XSLFTableCell> cells = row.getCells();
					for (XSLFTableCell cell : cells) {
						List<XSLFTextParagraph> paras = cell
								.getTextParagraphs();
						for (XSLFTextParagraph para : paras) {
							ret = ParseTextParagraph(para, sharpIndex,
									fragIndex, frag, fragNode);
							if (ret != Const.SUCCESS) {
								break;
							}
						}
					}
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
			_filewordcount += frag._wordCount;
		}

		ret = Const.SUCCESS;

		return ret;
	}

	private int ParseTextParagraph(XSLFTextParagraph para, int paraIndex,
			int fragIndex, Fragmentation frag, XNode fragNode) {
		int ret = Const.FAIL;

		String valWithTag = "";
		List<XSLFTextRun> runs = para.getTextRuns();
		for (XSLFTextRun run : runs) {
			valWithTag = valWithTag + run.getText();
			valWithTag = valWithTag + _tagId;
		}
		// if (valWithTag.length() == 0) {
		// System.err.println("");
		// }
		if (valWithTag.length() > 0) {
			valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
		}
		valWithTag = valWithTag.trim();

		if (!valWithTag.isEmpty()) {
			int sentenceIndex = 0;
			// ArrayList<String> sentences = _sourceLang
			// .AnalyseSentence(valWithTag);

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
				int fragcount = frag._wordCount;
				for (Content sent : sents) {
					XNode sentNode = new XNode("Sentence");
					XSLFSentence hs = new XSLFSentence();
					//hs._state = _state;
					hs._entityFragIndex = fragIndex;
					hs._entitySentenceIndex = sentenceIndex++;
					hs._source = sent._content;
					hs._sourceWordCount = sent._count;
					// hs._sourceTagCount = sent._tagcount;
					hs._hashcode = sent._hashcode;
					hs._entitySentenceIndex = frag._sentences.size();
					fragcount += sent._count;
					hs.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(hs);

				}

				frag._wordCount = fragcount;
				frag._fragIndex = fragIndex;
				frag._sentenceCount = frag._sentences.size();

				// _entityFrags.add(frag);

				// _filesentencecount += frag._sentences.size();

			} else {
				// Log4j.error("Sentence is null or empty.");
			}
		}
		ret = Const.SUCCESS;

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
				XMLSlideShow slideShow = new XMLSlideShow(in);

				ret = CleanupSlideShow(slideShow);

				ostream = new ByteArrayOutputStream();
				out = new FileOutputStream(_targetFilePath, false);
				slideShow.write(ostream);
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

	public int CleanupSlideShow(XMLSlideShow slideShow) {

		int ret = Const.FAIL;

		XSLFSlide[] slides = slideShow.getSlides();
		int slideCount = slides.length;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			ret = CleanupSlide(slides[fragIndex], fragIndex,
					GetFragsWithType(FRAG_TYPE.SLIDE));
			if (ret != Const.SUCCESS) {
				break;
			}
		}
		XSLFNotes notes;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			notes = slideShow.getNotesSlide(slides[fragIndex]);
			ret = CleanupNote(notes, fragIndex,
					GetFragsWithType(FRAG_TYPE.NOTES));
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		return ret;
	}

	public int CleanupSlide(XSLFSlide slide, int fragIndex,
			ArrayList<Fragmentation> frags) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);

		XSLFShape[] shapes = slide.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			XSLFShape shape = shapes[sharpIndex];
			if (shape instanceof XSLFTextShape) {
				List<XSLFTextParagraph> paras = ((XSLFTextShape) shape)
						.getTextParagraphs();
				for (XSLFTextParagraph para : paras) {
					int sentCount = GetSentenceCount(para);
					if (sentCount > 0) {
						ret = CleanupTextParagraph(para, sentOffset, frag);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			} else if (shape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) shape;
				List<XSLFTableRow> rows = table.getRows();
				for (XSLFTableRow row : rows) {
					List<XSLFTableCell> cells = row.getCells();
					for (XSLFTableCell cell : cells) {
						List<XSLFTextParagraph> paras = cell
								.getTextParagraphs();
						for (XSLFTextParagraph para : paras) {
							int sentCount = GetSentenceCount(para);
							if (sentCount > 0) {
								ret = CleanupTextParagraph(para, sentOffset,
										frag);
								if (ret != Const.SUCCESS) {
									break;
								}
								sentOffset = sentOffset + sentCount;
							}
						}
					}
				}
			} else {
				ret = Const.SUCCESS;
			}
		}

		return ret;
	}

	public int CleanupNote(XSLFNotes node, int fragIndex,
			ArrayList<Fragmentation> frags) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);

		XSLFShape[] shapes = node.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			XSLFShape shape = shapes[sharpIndex];
			if (shape instanceof XSLFTextShape) {
				List<XSLFTextParagraph> paras = ((XSLFTextShape) shape)
						.getTextParagraphs();
				for (XSLFTextParagraph para : paras) {
					int sentCount = GetSentenceCount(para);
					if (sentCount > 0) {
						ret = CleanupTextParagraph(para, sentOffset, frag);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			} else if (shape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) shape;
				List<XSLFTableRow> rows = table.getRows();
				for (XSLFTableRow row : rows) {
					List<XSLFTableCell> cells = row.getCells();
					for (XSLFTableCell cell : cells) {
						List<XSLFTextParagraph> paras = cell
								.getTextParagraphs();
						for (XSLFTextParagraph para : paras) {
							int sentCount = GetSentenceCount(para);
							if (sentCount > 0) {
								ret = CleanupTextParagraph(para, sentOffset,
										frag);
								if (ret != Const.SUCCESS) {
									break;
								}
								sentOffset = sentOffset + sentCount;
							}
						}
					}
				}
			} else {
				ret = Const.SUCCESS;
			}
		}

		return ret;
	}

	private int CleanupTextParagraph(XSLFTextParagraph para, int sentOffset,
			Fragmentation frag) {
		int ret = Const.FAIL;

		String valWithTag = "";
		List<XSLFTextRun> runs = para.getTextRuns();
		for (XSLFTextRun run : runs) {
			valWithTag = valWithTag + run.getText();
			valWithTag = valWithTag + _tagId;
		}
		if (valWithTag.length() > 0) {
			valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
		}
		valWithTag = valWithTag.trim();

		if (!valWithTag.isEmpty()) {
			// ArrayList<String> sentences = _sourceLang
			// .AnalyseSentence(valWithTag);

			ArrayList<String> sentences = null;

			ArrayList<String> fragText = getFragContent(frag, sentOffset,
					sentences.size());

			if ((fragText != null) && (2 == fragText.size())) {
				String runsSource = fragText.get(0);
				String runsTarget = fragText.get(1);
				ArrayList<ReplaceMeta> arrMeta = ContentToMeta(runsSource,
						runsTarget);
				if (arrMeta != null) {

					for (XSLFTextRun run : runs) {
						for (ReplaceMeta meta : arrMeta) {
							if (0 == meta._sourceText.trim().compareTo(
									run.getText().trim())) {
								run.setText(meta._targetText);
								// run.setText("111111");
								ret = Const.SUCCESS;
							}
						}
					}
				}
			}
		}

		return ret;
	}

	private int GetSentenceCount(XSLFTextParagraph para) {
		int sentCount = 0;

		String valWithTag = "";
		List<XSLFTextRun> runs = para.getTextRuns();
		for (XSLFTextRun run : runs) {
			valWithTag = valWithTag + run.getText();
			valWithTag = valWithTag + _tagId;
		}
		if (valWithTag.length() > 0) {
			valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
		}
		valWithTag = valWithTag.trim();

		if (!valWithTag.isEmpty()) {
			// ArrayList<String> sentences = _sourceLang
			// .AnalyseSentence(valWithTag);

			ArrayList<String> sentences = null;
			sentCount = sentences.size();
		}

		return sentCount;
	}
}
