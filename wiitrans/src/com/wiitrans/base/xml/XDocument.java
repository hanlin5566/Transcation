/*
 * @author	: ECI
 * @date	: 2015-4-7
 */

package com.wiitrans.base.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class XDocument {

	private String _version = "1.0";
	private String _encoding = "UTF-8";
	private XNode _root = null;

	public XDocument() {
	}

	public XDocument(String encoding) {
		this._encoding = encoding;
	}

	public XNode Parse(String filePath) {
		try {
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(new File(filePath));
			Node root = doc.getFirstChild();
			_root = new XNode(root.getNodeName());
			_root.Parse(root);

		} catch (Exception e) {
			Log4j.error(e);
		}

		return _root;
	}

	public int Save(String filePath) {
		int ret = Const.FAIL;

		try {
			File file = new File(filePath);
			StreamResult result = new StreamResult(file);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, _encoding);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();

			if (_root != null) {
				doc.setXmlVersion(_version);

				ret = _root.Save(doc, null);
				if (Const.SUCCESS == ret) {
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);
				}
			}
		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public void SetRoot(XNode node) {
		_root = node;
	}

	public XNode GetRoot() {
		return _root;
	}
}
