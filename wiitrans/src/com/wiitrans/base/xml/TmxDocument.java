package com.wiitrans.base.xml;

import java.io.File;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TmxDocument {

	private String _version = "1.0";
	private String _encoding = "UTF-8";
	private XNode _root = null;

	public XNode Parse(String filePath) {
		try {
			DOMImplementation di = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("XML 1.0");
			DocumentType docType = di.createDocumentType("tmx", "tmx14.dtd",
					"SYSTEM");
			Document doc = di.createDocument(filePath, "aa", docType);

			Node root = doc.getFirstChild();
			_root = new XNode(root.getNodeName());

			_root.Parse(root);
			DocumentType a = doc.getDoctype();
			System.out.println(a.toString());
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
			
			
			DOMImplementation di = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("XML 1.0");
			DocumentType docType = di.createDocumentType("tmx", "tmx14.dtd",
					"SYSTEM");
			Document doc = di.createDocument(filePath, "aa", docType);


//			Document doc = DocumentBuilderFactory.newInstance()
//					.newDocumentBuilder().newDocument();

			if (_root != null) {
				doc.setXmlVersion(_version);
				// if (!_xmlStandalone) {
				//
				// DOMImplementation di =
				// DOMImplementationRegistry.newInstance().getDOMImplementation("asdfasdf");
				// di.createDocumentType("111", "222", "333");
				// // doc.create
				// // doc.setXmlStandalone(true);
				// // DocumentType doctype = doc.getDoctype();
				// // doctype.setTextContent("tmx14.dtd");
				// // doctype.setNodeValue("tmx14.dtd");
				// }
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
