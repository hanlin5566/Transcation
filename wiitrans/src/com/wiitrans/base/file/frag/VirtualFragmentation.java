/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;

public class VirtualFragmentation extends Fragmentation {

	public class TaskRange {
		public long _startTime;
		public long _endTime;
	}

	public SENTENCE_STATE _currState = SENTENCE_STATE.NONE;

	public String _tUserId = null;
	public String _eUserId = null;
	public String _qaUserId = null;

	public TaskRange _tTaskRange = null;
	public TaskRange _eTaskRange = null;
	public TaskRange _qaTaskRange = null;

	public String _id = null;
	public String _name = null;

	public void UnInit() {
		_tUserId = null;
		_eUserId = null;
		_qaUserId = null;

		_tTaskRange = null;
		_eTaskRange = null;
		_qaTaskRange = null;

		_id = null;
		_name = null;
		super.UnInit();
	}
}
