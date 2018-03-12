/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.frag;

import java.util.ArrayList;
import java.util.HashMap;

import com.wiitrans.base.file.FileConst;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.sentence.SDLXliffSentence;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.file.sentence.TTXSentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

// In word this is a paragraph.
// In excel this is a sheet.
// In PPT this is a slider.
public class Fragmentation {

	public enum FRAG_TYPE {
		NONE, TEXTBOX, HEADER, FOOTER, ENDNOTE, FOOTNOTE, BODY, COMMENT, TABLE, HEADER_FOOTER, SHEET, SLIDE, NOTES;

		public static FRAG_TYPE String2Enum(String val) {
			FRAG_TYPE type = NONE;
			try {

				if (!val.isEmpty()) {
					type = valueOf(val);
				}
			} catch (Exception e) {
				Log4j.error(e);
			}

			return type;
		}
	}
	
	public enum FILE_TYPE {
	    DEFAULT,TTX,SDLXLIFF
	}

	public SENTENCE_STATE _state;

	public FRAG_TYPE _fragType;

	public String _fileId;

	public int _sentenceCount;
	public int _wordCount;
	public int _totalwordCount;

	public int _fragIndex;
	
	private FILE_TYPE fileType = FILE_TYPE.DEFAULT;;

	public ArrayList<Sentence> _sentences;
	public HashMap<String, Sentence> _sentencesMap;

	protected XNode _node = null;
	
	
	
	public Fragmentation() {
	    	fileType = FILE_TYPE.DEFAULT;;
		_fragIndex = FileConst.INVALID_INDEX;
		// _wordCount = FileConst.INVALID_INT32_COUNT;
		// _sentenceCount = FileConst.INVALID_INT32_COUNT;
		_wordCount = 0;
		_sentenceCount = 0;
		_sentences = new ArrayList<Sentence>();
		_sentencesMap = new HashMap<String, Sentence>();
		_fragType = FRAG_TYPE.NONE;
		_state = SENTENCE_STATE.NONE;
	}
	public Fragmentation(FILE_TYPE fileType) {
	    	this.fileType = fileType;
		_fragIndex = FileConst.INVALID_INDEX;
		// _wordCount = FileConst.INVALID_INT32_COUNT;
		// _sentenceCount = FileConst.INVALID_INT32_COUNT;
		_wordCount = 0;
		_sentenceCount = 0;
		_sentences = new ArrayList<Sentence>();
		_sentencesMap = new HashMap<String, Sentence>();
		_fragType = FRAG_TYPE.NONE;
		_state = SENTENCE_STATE.NONE;
	}

	public void SetNode(XNode node) {
		_node = node;
	}

	public Sentence NewSentence() {
	    return new Sentence();
	}

	public int Parse(FileUtil fileutil) {
		int ret = Const.FAIL;

		// <Frag type="" index="" sentencecount="" wordcount="">
		_fragIndex = Util.String2Int(_node.GetAttr("index"));
		_wordCount = Util.String2Int(_node.GetAttr("wordcount"));
		_totalwordCount = Util.String2Int(_node.GetAttr("totalwordcount"));
		_fragType = FRAG_TYPE.String2Enum(_node.GetAttr("type"));

		int sentenceCount = Util.String2Int(_node.GetAttr("sentencecount"));
		int realSentenceCount = 0;

		for (XNode sent : _node.GetChildren()) {
			//TODO:判断是TTX类型则 new TTXSentence
			Sentence obj = NewSentence();
			obj.SetNode(sent);
			if (obj.Parse(fileutil) == Const.SUCCESS) {
				realSentenceCount++;
				obj._entityFragIndex = _fragIndex;// 20150706添加实体碎片的句子中的碎片ID
				_sentences.add(obj);
			}
		}

		if (realSentenceCount > 0) {
			if (sentenceCount != realSentenceCount) {
				Log4j.error(String.format(
						"The sentenceCount is %d, but only %d.", sentenceCount,
						realSentenceCount));
			}
		}

		ret = Const.SUCCESS;
		_sentenceCount = realSentenceCount;

		return ret;
	}

	public void Save(FileUtil fileutil) {
		// <Frag type="" index="" sentencecount="" wordcount="">
		_node.SetAttr("type", _fragType.toString());
		_node.SetAttr("index", _fragIndex);
		_node.SetAttr("sentencecount", _sentenceCount);
		_node.SetAttr("wordcount", _wordCount);
		_node.SetAttr("totalwordcount", ""+_totalwordCount);
		
		for (Sentence sent : _sentences) {
			sent.Save(fileutil);
		}

	}

	public void UnInit() {
		if (_sentencesMap != null) {
			_sentencesMap.clear();
		}

		if (_sentences != null) {
			for (Sentence sentence : _sentences) {
				sentence.UnInit();
			}
			_sentences.clear();
			_sentences = null;
		}

		_fileId = null;
		_fragType = null;
		_state = null;
		_node = null;
	}
}
