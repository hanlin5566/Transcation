/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.sentence;

import com.wiitrans.base.log.Log4j;

public class SentenceState {
	
	public enum SENTENCE_STATE
	{
		NONE, T, E, TQA, EQA;
		
		public static SENTENCE_STATE String2Enum(String val)
		{
			SENTENCE_STATE state = NONE;
			try {
				
				if(!val.isEmpty())
				{
					state = valueOf(val);
				}
			} catch (Exception e) {
				Log4j.error(e);
			}
			
			return state;
		}
	};
}
