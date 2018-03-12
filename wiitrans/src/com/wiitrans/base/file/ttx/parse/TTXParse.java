package com.wiitrans.base.file.ttx.parse;

import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.xml.XNode;

public interface TTXParse {
	public int ParseBody(XNode node, String source,String target, int lineindex,int percent,BiliFileNoTag fileNoTag);
}
