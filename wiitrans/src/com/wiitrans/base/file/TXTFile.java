package com.wiitrans.base.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xml.sax.InputSource;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FRAG_TYPE;
import com.wiitrans.base.file.frag.TXTFragmentation;
import com.wiitrans.base.file.sentence.TXTSentence;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TXTFile extends BiliFile {

	public TXTFile() {
		_fileType = ENTITY_FILE_TYPE.HWPF;
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
			Log4j.error(e);
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
	public int Cleanup() {
		int ret = Const.FAIL;

		if ((_sourceFilePath != null) && (_targetFilePath != null)) {
			File fileRead = null;
			File fileWrite = null;
			BufferedReader reader = null;
			FileInputStream in = null;
			FileWriter writer = null;
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

				writer = new FileWriter(fileWrite);
				while ((sourceString = reader.readLine()) != null) {
					if (sourceString.trim().length() > 0) {

						targetString = CleanupBody(sourceString,
								_entityFrags.get(line++));
						writer.write(targetString);
						// writer.write("11111111");
					}
					writer.write("\n");
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
			// ArrayList<String> sentences = _sourceLang
			// .AnalyseSentence(_xmlcharutil.Encode(lineString).toString());

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
					TXTSentence ts = new TXTSentence();
					//ts._state = _state;
					ts._entityFragIndex = entityFragIndex;
					ts._entitySentenceIndex = sentenceIndex++;
					ts._source = sent._content;
					ts._sourceWordCount = sent._count;
					// ts._sourceTagCount = sent._tagcount;
					ts._hashcode = sent._hashcode;
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

	protected String CleanupBody(String sourceString, Fragmentation frag) {
		String targetString = null;

		ArrayList<String> fragText = getFragContent(frag, 0,
				frag._sentenceCount);

		if (sourceString != null) {
			if ((fragText != null) && (2 == fragText.size())) {
				String runsSource = fragText.get(0);
				if (0 == runsSource
						.compareTo(_xmlcharutil.Encode(sourceString))) {
					targetString = fragText.get(1);
				}
			}
		}

		return _xmlcharutil.Decode(targetString);
	}
}
