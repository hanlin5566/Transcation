package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.HSSFSentence;
import com.wiitrans.base.file.sentence.Sentence;

public class HSSFFragmentation extends Fragmentation {

	//public enum FRAG_TYPE {
	//	NONE, TEXTBOX, HEADER_FOOTER, ENDNOTE, FOOTNOTE, BODY
	//}
	
	@Override
	public Sentence NewSentence()
	{
		return new HSSFSentence();
	}
}
