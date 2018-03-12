package com.wiitrans.base.file.lang;

import java.util.HashMap;

public class WordNode {

	public String key;
	public HashMap<String, WordNode> subNodes;
	public boolean alsoLeaf;
	public int term_id;
	public String meaning;

	private WordNode() {
	}

	public WordNode(String key) {
		this.key = key;
		this.subNodes = null;
		this.alsoLeaf = false;
		this.term_id = 0;
	}
}