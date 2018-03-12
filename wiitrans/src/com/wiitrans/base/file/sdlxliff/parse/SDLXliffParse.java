package com.wiitrans.base.file.sdlxliff.parse;

import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.xml.XNode;

public interface SDLXliffParse {
	public int parseBody(XNode node, String source,String target, int lineindex,String mid,boolean lock,int percent,BiliFileNoTag fileNoTag);
}
