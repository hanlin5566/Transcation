package com.wiitrans.base.xml;

import java.util.ArrayList;
import java.util.HashMap;


import com.wiitrans.base.misc.Const;

public class JDomNode {

	private String _tagName = null;
	private String _val = null;
	private String _content = null;
	private ArrayList<JDomNode> _children = null;
	private HashMap<String, String> _attrs = null;

	public JDomNode(String tagName) {
		_children = new ArrayList<JDomNode>();
		_attrs = new HashMap<String, String>();
		_tagName = tagName;
	}

	public String GetTagName() {
		return _tagName;
	}

	public void SetTagName(String tagName) {
		_tagName = tagName;
	}

	public void ClearChildren() {
		_children.clear();
	}

	public void AddChild(String name, String val) {
		JDomNode child = new JDomNode(name);
		child.SetValue(val);

		AddChild(child);
	}

	public void AddChild(JDomNode child) {
		if (child != null) {
			_children.add(child);
		}
	}

	public ArrayList<JDomNode> GetChildren() {
		return _children;
	}

	public int SetAttr(String name, String val) {
		int ret = Const.FAIL;

		if (!_attrs.containsKey(name)) {
			_attrs.put(name, val);
			ret = Const.SUCCESS;
		}

		return ret;
	}

	public int SetAttr(String name, int val) {
		int ret = Const.FAIL;

		if (!_attrs.containsKey(name)) {
			_attrs.put(name, Integer.toString(val));
			ret = Const.SUCCESS;
		}

		return ret;
	}

	public String GetAttr(String name) {
		StringBuilder val = new StringBuilder();

		if (_attrs.containsKey(name)) {
			val.append(_attrs.get(name));
		}

		return val.toString();
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String _content) {
		this._content = _content;
	}

	public void SetValue(String val) {
		_val = val;
	}

	public String GetValue() {
		return _val;
	}

//	public int Parse(Element node) {
//		int ret = Const.FAIL;
//
//		short type = node.();
//		if (Node.TEXT_NODE == type) {
//			_val = node.getNodeValue();
//		} else if (Node.ELEMENT_NODE == type) {
//			_content = node.getTextContent();
//			_content = _content.replaceAll("\n", " ");
//			_content = _content.replaceAll("\r", "");
//			_content = _content.replaceAll("\t", "");
//		}
//		NamedNodeMap attrs = node.getAttributes();
//		if (attrs != null) {
//			for (int index = 0; index < attrs.getLength(); index++) {
//				Node attrNode = attrs.item(index);
//				if (attrNode != null) {
//					_attrs.put(attrNode.getNodeName(), attrNode.getNodeValue());
//				}
//			}
//		}
//
//		NodeList children = node.getChildNodes();
//
//		for (int index = 0; index < children.getLength(); ++index) {
//			Node son = children.item(index);
//			JDomNode xson = new JDomNode(son.getNodeName());
//			xson.Parse(son);
//
//			// AddChild(xson);
//
//			String val = son.getNodeValue();
//			if (val == null) {
//				AddChild(xson);
//			} else if ((!val.isEmpty()) && (!val.trim().isEmpty())) {
//				AddChild(xson);
//			}
//		}
//
//		return ret;
//	}

//	public int Save(Node ancestor, Node parent) {
//		int ret = Const.FAIL;
//
//		if (_children != null) {
//			if (_tagName != "#text") {
//				// 1.Create son node.
//				Element son = ((Document) ancestor).createElement(_tagName);
//
//				// 2.Assign attributes.
//				Iterator<Entry<String, String>> iter = _attrs.entrySet()
//						.iterator();
//				while (iter.hasNext()) {
//					Map.Entry entry = (Map.Entry) iter.next();
//					son.setAttribute((String) entry.getKey(),
//							(String) entry.getValue());
//				}
//
//				// 3.Add value.
//				if (_val != null) {
//					son.appendChild(((Document) ancestor).createTextNode(_val));
//				}
//
//				// 4.Add node
//				if (parent != null) {
//					parent.appendChild(son);
//				}
//				// 5.Add root
//				else {
//					ancestor.appendChild(son);
//				}
//
//				// 6.Children
//				for (JDomNode child : _children) {
//					child.Save(ancestor, son);
//				}
//			} else {
//				if (parent != null) {
//					parent.appendChild(((Document) ancestor)
//							.createTextNode(_val));
//				}
//				// 5.Add root
//				else {
//					ancestor.appendChild(((Document) ancestor)
//							.createTextNode(_val));
//				}
//			}
//
//			ret = Const.SUCCESS;
//		}
//
//		return ret;
//	}
}
