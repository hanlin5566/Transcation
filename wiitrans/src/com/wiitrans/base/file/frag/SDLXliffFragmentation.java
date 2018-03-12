package com.wiitrans.base.file.frag;

import com.wiitrans.base.file.sentence.SDLXliffSentence;
import com.wiitrans.base.file.sentence.Sentence;

public class SDLXliffFragmentation extends Fragmentation {
    @Override
    public Sentence NewSentence() {
	return new SDLXliffSentence();
    }
}