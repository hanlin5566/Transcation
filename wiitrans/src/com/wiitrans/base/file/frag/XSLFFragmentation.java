package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.XSLFSentence;

public class XSLFFragmentation extends Fragmentation {

	//public enum FRAG_TYPE {
	//	NONE, TEXTBOX, HEADER_FOOTER, ENDNOTE, FOOTNOTE, BODY
	//}
	
	@Override
	public Sentence NewSentence()
	{
		return new XSLFSentence();
	}
}
