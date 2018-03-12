package com.wiitrans.base.file.font;

import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TagFont {

	public int _index;
	public String _family;
	public int _size;
	public boolean _bold;
	public boolean _italic;
	public String _underline;
	public String _color;
	public boolean _straike;

	protected XNode _node = null;

	public TagFont() {
		_index = 0;
		_family = null;
		_size = 22;
		_bold = false;
		_italic = false;
		_underline = "NONE";
		_color = "000000";
		_straike = false;
	}

	public void SetNode(XNode node) {
		_node = node;
	}

	public int Parse(FileUtil fileutil) {
		int ret = Const.FAIL;
		_index = Util.String2Int(_node.GetAttr("index"));
		_family = _node.GetAttr("family");
		_size = Util.String2Int(_node.GetAttr("size"));
		_bold = Util.String2Bool(_node.GetAttr("bold"));
		_italic = Util.String2Bool(_node.GetAttr("italic"));
		_underline = _node.GetAttr("underline");
		_color = _node.GetAttr("color");
		_straike = Util.String2Bool(_node.GetAttr("straike"));
		return ret;
	}

	public void Save(FileUtil fileutil) {

		_node.ClearChildren();
		_node.SetAttr("index", _index);
		_node.SetAttr("family", _family);
		_node.SetAttr("size", _size);
		_node.SetAttr("bold", String.valueOf(_bold));
		_node.SetAttr("italic", String.valueOf(_italic));
		_node.SetAttr("underline", _underline);
		_node.SetAttr("color", _color);
		_node.SetAttr("straike", String.valueOf(_straike));
	}

	public void UnInit() {
		_family = null;
		_underline = null;
		_color = null;

	}
}
