package com.wiitrans.base.file.lang;

public class TextTerm {
	public int term_id;

	public String term;

	public int index;

	public int count = 1;

	public String meaning;

	public boolean equals(Object obj) {
		if (obj instanceof TextTerm) {
			TextTerm term = (TextTerm) obj;
			return this.term_id == term.term_id && this.term.equals(term.term);
		}
		return super.equals(obj);
	}
}
