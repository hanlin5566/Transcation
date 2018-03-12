/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.HWPFSentence;
import com.wiitrans.base.file.sentence.Sentence;


public class HWPFFragmentation extends Fragmentation {
	
	//public enum FRAG_TYPE {
	//	NONE, TEXTBOX, HEADER_FOOTER, ENDNOTE, FOOTNOTE, BODY
	//}
	
	@Override
	public Sentence NewSentence()
	{
		return new HWPFSentence();
	}
}
