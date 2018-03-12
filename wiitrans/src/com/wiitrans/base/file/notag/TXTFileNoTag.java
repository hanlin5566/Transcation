package com.wiitrans.base.file.notag;

//import java.io.BufferedInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.TXTFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.lang.LangSentence;
import com.wiitrans.base.file.sentence.TXTSentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TXTFileNoTag extends BiliFileNoTag {

	public TXTFileNoTag() {
		_fileType = ENTITY_FILE_TYPE.TXT;
	}

	private String getCharset(BufferedInputStream bis) {

		String code = null;
		try {
			int p = (bis.read() << 8) + bis.read();
			switch (p) {
			case 0xefbb:
				code = "UTF-8";
				break;
			case 0xfffe:
				code = "UNICODE";
				break;
			case 0xfeff:
				code = "UTF-16BE";
				break;
			default:
				code = "GBK";
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return code;
	}

	@Override
	public int Parse() {
		int ret = Const.FAIL;

		if (_sourceFilePath != null) {
			File fileRead = null;
			BufferedReader reader = null;
			FileInputStream in = null;
			String lineString = null;
			int line = 0;

			try {

				fileRead = new File(_sourceFilePath);
				in = new FileInputStream(fileRead);

				String code = this.getCharset(new BufferedInputStream(
						new FileInputStream(_sourceFilePath)));
				reader = new BufferedReader(new InputStreamReader(in, code));

				XNode fragEntry = new XNode("EntityFrags");
				_xmlDoc.SetRoot(new XNode("File"));
				_xmlDoc.GetRoot().AddChild(fragEntry);
				while ((lineString = reader.readLine()) != null) {
					if (lineString.trim().length() > 0) {
						ret = ParseBody(fragEntry, lineString, line++);
					}
				}

				_xmlDoc.GetRoot().SetAttr("sentencecount", _filesentencecount);
				_xmlDoc.GetRoot().SetAttr("wordcount", _filewordcount);
				reader.close();
			} catch (Exception e) {
				Log4j.error(e);
			} finally {
			}
		}

		return ret;
	}

	@Override
	public int Cleanup(SENTENCE_STATE state) {
		int ret = Const.FAIL;

		if ((_sourceFilePath != null) && (_targetFilePath != null)) {
			File fileRead = null;
			File fileWrite = null;
			BufferedReader reader = null;
			BufferedWriter writer = null;
			FileInputStream in = null;
			FileOutputStream out = null;
			String sourceString = null;
			String targetString = null;
			int line = 0;
			try {
				fileRead = new File(_sourceFilePath);

				in = new FileInputStream(fileRead);
				String code = this.getCharset(new BufferedInputStream(
						new FileInputStream(_sourceFilePath)));
				reader = new BufferedReader(new InputStreamReader(in, code));

				fileWrite = new File(_targetFilePath);

				if (fileWrite.exists()) {
					fileWrite.delete();
				}
				fileWrite.createNewFile();

				out = new FileOutputStream(fileWrite);
				if (code.equals("GBK")) {
					writer = new BufferedWriter(new OutputStreamWriter(out,
							code));
				} else {
					writer = new BufferedWriter(new OutputStreamWriter(out,
							"UNICODE"));
				}
				while ((sourceString = reader.readLine()) != null) {
					if (sourceString.trim().length() > 0) {

						targetString = CleanupBody(sourceString,
								_entityFrags.get(line++), state);
						if (targetString == null) {
							targetString = new String(sourceString);
						}
						writer.write(targetString);
						// writer.write("11111111");
					}
					writer.write("\r\n");
				}
				writer.close();
				reader.close();
				ret = Const.SUCCESS;
			} catch (Exception e) {
				Log4j.error(e);
			} finally {

			}
		} else {
			Log4j.error("Path is not exist.");
		}

		return ret;
	}

	private int ParseBody(XNode node, String lineString, int lineindex) {

		int ret = Const.FAIL;

		int entityFragIndex = lineindex;

		if (lineString != null && lineString.trim().length() > 0) {
			XNode fragNode = new XNode("Frag");
			TXTFragmentation frag = new TXTFragmentation();

			int sentenceIndex = 0;
			ArrayList<LangSentence> sentences = _sourceLang
					.AnalyseSentence(lineString);

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
					TXTSentence ts = new TXTSentence();
					// ts._state = _state;
					ts._entityFragIndex = entityFragIndex;
					ts._entitySentenceIndex = sentenceIndex++;
					ts._source = sent._content;
					ts._sourceWordCount = sent._count;
					// ts._sourceTagCount = sent._tagcount;
					ts._hashcode = sent._hashcode;
					ts._valid = sent._valid;
					fragcount += sent._count;
					ts.SetNode(sentNode);
					fragNode.AddChild(sentNode);

					frag._sentences.add(ts);

				}

				frag._wordCount = fragcount;
				frag._fragIndex = entityFragIndex;
				frag._sentenceCount = frag._sentences.size();
				frag._fragType = FRAG_TYPE.NONE;
				frag.SetNode(fragNode);
				node.AddChild(fragNode);

				_entityFrags.add(frag);

				_filesentencecount += frag._sentences.size();
				_filewordcount += fragcount;

			} else {
				// Log4j.error("Sentence is null or empty.");
			}
		}

		_entityFragCount = _entityFrags.size();

		ret = Const.SUCCESS;

		return ret;
	}

	protected String CleanupBody(String sourceString, Fragmentation frag,
			SENTENCE_STATE state) {
		String targetString = null;

		ArrayList<String> fragText = getFragContent(frag, 0,
				frag._sentenceCount, state);

		if (sourceString != null) {
			if ((fragText != null) && (2 == fragText.size())) {
				String runsSource = fragText.get(0);
				if (0 == runsSource.compareTo(sourceString)) {
					targetString = fragText.get(1);
				}
			}
		}

		return targetString;
	}
}