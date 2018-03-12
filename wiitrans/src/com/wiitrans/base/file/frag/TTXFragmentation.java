package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.file.sentence.TTXSentence;

public class TTXFragmentation extends Fragmentation {
    @Override
    public Sentence NewSentence() {
	return new TTXSentence();
    }
}