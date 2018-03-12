package com.wiitrans.base.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TableCell;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HSLFFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.sentence.HSLFSentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

//.PPT

//[POI]
//SlideShow->Slide->TextRun->RichTextRun->getText/setText

//[UFile]
//File->Frag(fragType=Slide, fragIndex=SlideIndex)->Sentence

public class HSLFFile extends BiliFile {

	private SlideShow _sliderShow = null;

	public HSLFFile() {
		_fileType = ENTITY_FILE_TYPE.HSLF;
	}

	@Override
	protected Fragmentation NewFrag() {
		return new HSLFFragmentation();
	}

	@Override
	public int Parse() {
		int ret = Const.FAIL;

		if (_sourceFilePath != null) {
			FileInputStream fileHandle = null;

			try {
				fileHandle = new FileInputStream(new File(_sourceFilePath));
				_sliderShow = new SlideShow(fileHandle);

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

	private int ParseSlideShow(SlideShow slideShow) {

		int ret = Const.FAIL;

		XNode fragEntry = new XNode("EntityFrags");
		_xmlDoc.SetRoot(new XNode("File"));
		_xmlDoc.GetRoot().AddChild(fragEntry);

		Slide[] slides = slideShow.getSlides();
		int slideCount = slides.length;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			ret = ParseSlide(slides[fragIndex], fragIndex, fragEntry);
			if (ret != Const.SUCCESS) {
				break;
			}
			_entityFragCount++;

		}

		Notes[] notes = slideShow.getNotes();
		int notesCount = notes.length;
		for (int fragIndex = 0; fragIndex < notesCount; ++fragIndex) {
			ret = ParseNote(notes[fragIndex], fragIndex, fragEntry);
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

	private int ParseSlide(Slide slide, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.SLIDE;

		Shape[] shapes = slide.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			Shape shape = shapes[sharpIndex];
			if (shape instanceof TextShape) {
				TextRun run = ((TextShape) shape).getTextRun();
				ret = ParseTextRun(run, sharpIndex, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}

			} else if (shape instanceof Table) {
				Table table = ((Table) shape);
				int rowNum = ((Table) shape).getNumberOfRows();
				int colNum = ((Table) shape).getNumberOfColumns();
				for (int rowIndex = 0; rowIndex < rowNum; ++rowIndex) {
					for (int colIndex = 0; colIndex < colNum; ++colIndex) {
						TableCell cell = table.getCell(rowIndex, colIndex);
						if (cell != null) {
							ret = ParseTextRun(cell.getTextRun(), sharpIndex,
									fragIndex, frag, fragNode);
						} else {
							ret = ParseTextRun(null, sharpIndex, fragIndex,
									frag, fragNode);
						}

						if (ret != Const.SUCCESS) {
							break;
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

		return ret;
	}

	private int ParseNote(Notes node, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.NOTES;

		Shape[] shapes = node.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			Shape shape = shapes[sharpIndex];
			if (shape instanceof TextShape) {
				TextRun run = ((TextShape) shape).getTextRun();
				ret = ParseTextRun(run, sharpIndex, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}

			} else if (shape instanceof Table) {
				Table table = ((Table) shape);
				int rowNum = ((Table) shape).getNumberOfRows();
				int colNum = ((Table) shape).getNumberOfColumns();
				for (int rowIndex = 0; rowIndex < rowNum; ++rowIndex) {
					for (int colIndex = 0; colIndex < colNum; ++colIndex) {
						TableCell cell = table.getCell(rowIndex, colIndex);
						if (cell != null) {
							ret = ParseTextRun(cell.getTextRun(), sharpIndex,
									fragIndex, frag, fragNode);
						} else {
							ret = ParseTextRun(null, sharpIndex, fragIndex,
									frag, fragNode);
						}

						if (ret != Const.SUCCESS) {
							break;
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

		return ret;
	}

	private int ParseTextRun(TextRun run, int runIndex, int fragIndex,
			Fragmentation frag, XNode fragNode) {
		int ret = Const.FAIL;
		if (run != null) {

			String valWithTag = "";
			RichTextRun[] runs = run.getRichTextRuns();
			int runCount = runs.length;
			for (int index = 0; index < runCount; ++index) {
				valWithTag = valWithTag + runs[index].getText();
				valWithTag = valWithTag + _tagId;
			}
			valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
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
								.AnalyseWord(_xmlcharutil.Decode(sentence))
								.size());

						content._hashcode = Util.GetHashCode(sentence);
					}

					sents.add(content);
				}

				if ((sents != null) && (!sents.isEmpty())) {
					int fragcount = frag._wordCount;
					for (Content sent : sents) {
						XNode sentNode = new XNode("Sentence");
						HSLFSentence hs = new HSLFSentence();
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

				}
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
				SlideShow slideShow = new SlideShow(in);

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

	public int CleanupSlideShow(SlideShow slideShow) {

		int ret = Const.FAIL;

		Slide[] slides = slideShow.getSlides();
		int slideCount = slides.length;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			ret = CleanupSlide(slides[fragIndex], fragIndex,
					GetFragsWithType(FRAG_TYPE.SLIDE));
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		Notes[] notes = slideShow.getNotes();
		int notesCount = notes.length;
		for (int fragIndex = 0; fragIndex < notesCount; ++fragIndex) {
			ret = CleanupNote(notes[fragIndex], fragIndex,
					GetFragsWithType(FRAG_TYPE.NOTES));
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		return ret;
	}

	public int CleanupSlide(Slide slide, int fragIndex,
			ArrayList<Fragmentation> frags) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);

		Shape[] shapes = slide.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			Shape shape = shapes[sharpIndex];
			if (shape instanceof TextShape) {
				TextRun run = ((TextShape) shape).getTextRun();
				if (run != null) {
					int sentCount = GetSentenceCount(run);
					if (sentCount > 0) {
						ret = CleanupTextRun(run, sentOffset, frag);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			} else if (shape instanceof Table) {
				Table table = ((Table) shape);
				int rowNum = ((Table) shape).getNumberOfRows();
				int colNum = ((Table) shape).getNumberOfColumns();
				for (int rowIndex = 0; rowIndex < rowNum; ++rowIndex) {
					for (int colIndex = 0; colIndex < colNum; ++colIndex) {
						TableCell cell = table.getCell(rowIndex, colIndex);
						if (cell != null) {
							TextRun run = cell.getTextRun();

							int sentCount = GetSentenceCount(run);
							if (sentCount > 0) {
								ret = CleanupTextRun(run, sentOffset, frag);
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

	public int CleanupNote(Notes note, int fragIndex,
			ArrayList<Fragmentation> frags) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);

		Shape[] shapes = note.getShapes();
		for (int sharpIndex = 0; sharpIndex < shapes.length; ++sharpIndex) {
			Shape shape = shapes[sharpIndex];
			if (shape instanceof TextShape) {
				TextRun run = ((TextShape) shape).getTextRun();
				if (run != null) {
					int sentCount = GetSentenceCount(run);
					if (sentCount > 0) {
						ret = CleanupTextRun(run, sentOffset, frag);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			} else if (shape instanceof Table) {
				Table table = ((Table) shape);
				int rowNum = ((Table) shape).getNumberOfRows();
				int colNum = ((Table) shape).getNumberOfColumns();
				for (int rowIndex = 0; rowIndex < rowNum; ++rowIndex) {
					for (int colIndex = 0; colIndex < colNum; ++colIndex) {
						TableCell cell = table.getCell(rowIndex, colIndex);
						if (cell != null) {
							TextRun run = cell.getTextRun();

							int sentCount = GetSentenceCount(run);
							if (sentCount > 0) {
								ret = CleanupTextRun(run, sentOffset, frag);
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

	private int CleanupTextRun(TextRun run, int sentOffset, Fragmentation frag) {
		int ret = Const.FAIL;

		String valWithTag = "";
		RichTextRun[] runs = run.getRichTextRuns();
		int runCount = runs.length;
		for (int index = 0; index < runCount; ++index) {
			valWithTag = valWithTag + runs[index].getText();
			valWithTag = valWithTag + _tagId;
		}
		valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
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
					for (int index = runCount - 1; index >= 0; --index) {
						RichTextRun richRun = runs[index];
						// for (int metaIndex = arrMeta.size() - 1; metaIndex >=
						// 0; --metaIndex) {
						// ReplaceMeta meta = arrMeta.get(metaIndex);
						for (ReplaceMeta meta : arrMeta) {
							// String text = meta._sourceText;
							// String gettext = richRun.getText();

							// System.out.println(text);
							// System.out.println(gettext);

							if (0 == meta._sourceText.compareTo(richRun
									.getText())) {
								richRun.setText(meta._targetText);
								// richRun.setText("abcdef");
								ret = Const.SUCCESS;
								break;
							}
						}
					}
				}
			}
		}

		return ret;
	}

	private int GetSentenceCount(TextRun run) {
		int sentCount = 0;
		if (run != null) {
			String valWithTag = "";
			RichTextRun[] runs = run.getRichTextRuns();
			int runCount = runs.length;
			for (int index = 0; index < runCount; ++index) {
				valWithTag = valWithTag + runs[index].getText();
				valWithTag = valWithTag + _tagId;
			}
			valWithTag = valWithTag.substring(0, valWithTag.length() - 1);
			valWithTag = valWithTag.trim();

			if (!valWithTag.isEmpty()) {
				// ArrayList<String> sentences = _sourceLang
				// .AnalyseSentence(valWithTag);
				ArrayList<String> sentences = null;
				sentCount = sentences.size();
			}
		}
		return sentCount;
	}
}
