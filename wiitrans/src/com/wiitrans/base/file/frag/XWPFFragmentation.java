/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.XWPFSentence;

public class XWPFFragmentation extends Fragmentation {
	
	// TODO : Text box is cannot parse, now.
	//public enum FRAG_TYPE {
	//	NONE, TEXTBOX, HEADER, FOOTER, ENDNOTE, FOOTNOTE, BODY, TABLE
	//}
	
	@Override
	public Sentence NewSentence()
	{
		return new XWPFSentence();
	}
}
