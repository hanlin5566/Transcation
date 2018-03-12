package com.wiitrans.base.file.notag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HSLFFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.LangSentence;
import com.wiitrans.base.file.sentence.HSLFSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class HSLFFileNoTag extends BiliFileNoTag {

	private SlideShow _sliderShow = null;

	@Override
	public int UnInit() {
		_sliderShow = null;
		return super.UnInit();
	}

	public HSLFFileNoTag() {
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
			ret = ParseNotes(notes[fragIndex], fragIndex, fragEntry);
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

	private int ParseNotes(Notes notes, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.NOTES;

		TextRun[] runs = notes.getTextRuns();
		if (runs == null || runs.length == 0) {
			// 添加原因：如果没有数据，直接放回成功，可以进行下一个shape的解析
			ret = Const.SUCCESS;
		} else {
			for (int runIndex = 0; runIndex < runs.length; ++runIndex) {
				TextRun run = runs[runIndex];
				ret = ParseTextRun(run, runIndex, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}
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

	private int ParseSlide(Slide slide, int fragIndex, XNode fragEntry) {
		int ret = Const.FAIL;

		XNode fragNode = new XNode("Frag");
		Fragmentation frag = NewFrag();
		frag._wordCount = 0;
		frag._fragIndex = fragIndex;
		frag._sentenceCount = 0;
		frag._fragType = FRAG_TYPE.SLIDE;
		TextRun[] runs = slide.getTextRuns();
		// Shape[] shapes = slide.getShapes();
		if (runs == null || runs.length == 0) {
			// 添加原因：如果没有数据，直接放回成功，可以进行下一个shape的解析
			ret = Const.SUCCESS;
		} else {
			for (int runIndex = 0; runIndex < runs.length; ++runIndex) {
				TextRun run = runs[runIndex];
				ret = ParseTextRun(run, runIndex, fragIndex, frag, fragNode);
				if (ret != Const.SUCCESS) {
					break;
				}
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

			int sentenceIndex = 0;

			ArrayList<LangSentence> sentences = _sourceLang.AnalyseSentence(run
					.getText());

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
				int fragcount = frag._wordCount;
				for (Content sent : sents) {
					XNode sentNode = new XNode("Sentence");
					HSLFSentence hs = new HSLFSentence();
					// hs._state = _state;
					hs._entityFragIndex = fragIndex;
					hs._entitySentenceIndex = sentenceIndex++;
					hs._source = sent._content;
					hs._sourceWordCount = sent._count;
					// hs._sourceTagCount = sent._tagcount;
					hs._hashcode = sent._hashcode;
					hs._valid = sent._valid;
					hs._entitySentenceIndex = frag._sentences.size();
					fragcount += sent._count;
					hs.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(hs);

				}

				frag._wordCount = fragcount;
				frag._fragIndex = fragIndex;
				frag._sentenceCount = frag._sentences.size();
			}

		}
		ret = Const.SUCCESS;

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
				SlideShow slideShow = new SlideShow(in);

				ret = CleanupSlideShow(slideShow, state);

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

	public int CleanupSlideShow(SlideShow slideShow, SENTENCE_STATE state) {

		int ret = Const.FAIL;

		Slide[] slides = slideShow.getSlides();
		int slideCount = slides.length;
		for (int fragIndex = 0; fragIndex < slideCount; ++fragIndex) {
			ret = CleanupSlide(slides[fragIndex], fragIndex,
					GetFragsWithType(FRAG_TYPE.SLIDE), state);
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		Notes[] notes = slideShow.getNotes();
		int notesCount = notes.length;
		for (int fragIndex = 0; fragIndex < notesCount; ++fragIndex) {
			ret = CleanupNotes(notes[fragIndex], fragIndex,
					GetFragsWithType(FRAG_TYPE.NOTES), state);
			if (ret != Const.SUCCESS) {
				break;
			}
		}

		return ret;
	}

	public int CleanupNotes(Notes notes, int fragIndex,
			ArrayList<Fragmentation> frags, SENTENCE_STATE state) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);

		TextRun[] runs = notes.getTextRuns();
		if (runs == null || runs.length == 0) {
			// 添加原因：如果没有数据，直接放回成功，可以进行下一个shape的解析
			ret = Const.SUCCESS;
		} else {
			for (int runIndex = 0; runIndex < runs.length; ++runIndex) {
				TextRun run = runs[runIndex];
				if (run != null) {
					int sentCount = GetSentenceCount(run);
					if (sentCount > 0) {
						ret = CleanupTextRun(run, sentOffset, frag, state);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			}
		}

		return ret;
	}

	public int CleanupSlide(Slide slide, int fragIndex,
			ArrayList<Fragmentation> frags, SENTENCE_STATE state) {

		int ret = Const.FAIL;

		int sentOffset = 0;
		Fragmentation frag = frags.get(fragIndex);
		TextRun[] runs = slide.getTextRuns();
		if (runs == null || runs.length == 0) {
			// 添加原因：如果没有数据，直接放回成功，可以进行下一个shape的解析
			ret = Const.SUCCESS;
		} else {
			for (int runIndex = 0; runIndex < runs.length; ++runIndex) {
				TextRun run = runs[runIndex];
				if (run != null) {
					int sentCount = GetSentenceCount(run);
					if (sentCount > 0) {
						ret = CleanupTextRun(run, sentOffset, frag, state);
						if (ret != Const.SUCCESS) {
							break;
						}
						sentOffset = sentOffset + sentCount;
					}
				}
			}
		}

		return ret;
	}

	private int CleanupTextRun(TextRun run, int sentOffset, Fragmentation frag,
			SENTENCE_STATE state) {
		int ret = Const.FAIL;

		String paraSource = run.getText();

		ArrayList<LangSentence> sentences = _sourceLang
				.AnalyseSentence(paraSource);

		ArrayList<String> fragText = getFragContent(frag, sentOffset,
				sentences.size(), state);

		if ((fragText != null) && (2 == fragText.size())) {
			String runsSource = fragText.get(0);
			String runsTarget = fragText.get(1);
			if (!runsSource.equals(runsTarget)) {
				if (runsSource.trim().length() > 0
						&& paraSource.trim().length() > 0
						&& 0 == runsSource.trim().compareTo(paraSource.trim())) {
					run.setText(runsTarget);
				}
			}
		}

		// run.setText("11111");

		ret = Const.SUCCESS;

		return ret;
	}

	private int GetSentenceCount(TextRun run) {
		int sentCount = 0;
		if (run != null) {

			ArrayList<LangSentence> sentences = _sourceLang.AnalyseSentence(run
					.getText());
			sentCount = sentences.size();

		}
		return sentCount;
	}
}