package com.wiitrans.base.file.ttx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.HSLFFragmentation;
import com.wiitrans.base.file.frag.TTXFragmentation;
import com.wiitrans.base.file.frag.Fragmentation.FILE_TYPE;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TTXFileNoTag extends BiliFileNoTag {
    public TTXFileNoTag() {
	_fileType = ENTITY_FILE_TYPE.TTX;
    }

    @Override
    protected Fragmentation NewFrag() {
	return new TTXFragmentation();
    }

    public int _totalfilewordcount;
    // private List<String> supportedFileType =
    // Arrays.asList("doc","xls","ppt","docx","xlsx","pptx","html","xml");
    private List<String> supportedFileType = Arrays.asList("doc", "xls", "ppt",
	    "docx", "xlsx", "pptx", "html", "HTML","xml", "XML");
    private List<String> convertedType = Arrays
	    .asList("PlugInConverted", "RTF");

    @Override
    public int Parse() {
	int ret = Const.FAIL;

	if (_sourceFilePath != null) {
	    try {
		XNode fragEntry = new XNode("EntityFrags");
		_xmlDoc.SetRoot(new XNode("File"));
		_xmlDoc.GetRoot().AddChild(fragEntry);
		TTXSAXParse sax = new TTXSAXParse();
		sax.parse(fragEntry, this);
		_xmlDoc.GetRoot().SetAttr("sentencecount", _filesentencecount);
		_xmlDoc.GetRoot().SetAttr("totalwordcount", ""+_totalfilewordcount);
		// TODO:对有效字数向上取整
		_xmlDoc.GetRoot().SetAttr("wordcount",_filewordcount);
	    } catch (Exception e) {
		Log4j.error(e);
	    } finally {
	    }
	}
	return ret;
    }

    private void printNode(Node node) {
	if (node.getNodeType() == Node.ELEMENT_NODE) {
	    System.out.println("节点名:" + node.getNodeName() + "节点值:"
		    + node.getNodeValue());
	    if (node.hasAttributes()) {
		NamedNodeMap att = node.getAttributes();
		for (int i = 0; i < att.getLength(); i++) {
		    Node attNode = att.item(i);
		    System.out.print("属性:" + attNode.getNodeName() + " = "
			    + attNode.getNodeValue() + "  ");
		}
		System.out.println("");
	    }
	    System.out.println("节点内容:" + node.getTextContent());
	}
	// 遍历子节点
	if (node.hasChildNodes()) {
	    NodeList childList = node.getChildNodes();
	    for (int i = 0; i < childList.getLength(); i++) {
		// if(childList.item(i).getNodeType() == Node.TEXT_NODE){
		// System.out.println("节点值:"+node.getNodeValue());
		// }
		printNode(childList.item(i));
	    }
	}
    }

    @Override
    public int Cleanup(SENTENCE_STATE state) {
	int ret = Const.FAIL;

	if ((_sourceFilePath != null) && (_targetFilePath != null)) {
	    String sourceString = null;
	    String targetString = null;
	    int line = 0;
	    try {
		Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder().parse(new File(_sourceFilePath));
		// 由于xml格式的源文件地址会不以xml结尾，所以改用文件名获取扩展名
		// String sourceDocumentPath =
		// doc.getElementsByTagName("UserSettings").item(0).getAttributes().getNamedItem("SourceDocumentPath").getNodeValue();
		// String ext =
		// sourceDocumentPath.substring(sourceDocumentPath.lastIndexOf('.')+1,
		// sourceDocumentPath.length());
		String ext = doc.getElementsByTagName("UserSettings").item(0)
			.getAttributes().getNamedItem("DataType")
			.getNodeValue();
		if (convertedType.contains(ext)) {
		    // 如果是插件文件需要转换则取文件内后缀名
		    String sourceDocumentPath = doc
			    .getElementsByTagName("UserSettings").item(0)
			    .getAttributes().getNamedItem("SourceDocumentPath")
			    .getNodeValue();
		    ext = sourceDocumentPath.substring(
			    sourceDocumentPath.lastIndexOf('.') + 1,
			    sourceDocumentPath.length());
		}
		// String originalFileName =
		// _originalFileName.substring(0,_originalFileName.lastIndexOf('.'));
		// String ext =
		// originalFileName.substring(originalFileName.lastIndexOf('.')+1,
		// originalFileName.length());
		// 过滤不支持的格式
		if (supportedFileType.contains(ext)) {
		    NodeList nodeList = doc.getElementsByTagName("Tu");
		    for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Node sourceNode = node.getFirstChild();
			Node targetNode = node.getLastChild();
			if (sourceNode != null && targetNode != null) {
			    sourceString = sourceNode.getTextContent();
			    // this.printNode(sourceNode);
			    // this.printNode(targetNode);
			    if (sourceString.trim().length() > 0) {

				targetString = CleanupBody(sourceString,
					_entityFrags.get(line++), state);
				targetNode.setTextContent("");
				Util.setTagAttBySource(targetString,
					sourceNode, targetNode);
			    }
			}
		    }

		    // save document
		    File file = new File(_targetFilePath);
		    StreamResult result = new StreamResult(file);
		    Transformer transformer = TransformerFactory.newInstance()
			    .newTransformer();
		    DOMSource source = new DOMSource(doc);
		    transformer.transform(source, result);
		    ret = Const.SUCCESS;
		} else {
		    System.err.println("unsupported type can't clean up " + ext
			    + " path " + _sourceFilePath);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {

	    }
	} else {
	    Log4j.error("Path is not exist.");
	}

	return ret;
    }

    public int ParseBili() {
	int ret = Const.FAIL;

	_entityFrags.clear();
	_virtualFrags.clear();

	XNode root = _xmlDoc.Parse(_biliFilePath);
	_fileId = root.GetAttr("id");
	_sentenceCount = Util.String2Int(root.GetAttr("sentencecount"));
	_wordCount = Util.String2Int(root.GetAttr("wordcount"));
	_totalfilewordcount = Util.String2Int(root.GetAttr("totalwordcount"));
	_fileType = ENTITY_FILE_TYPE.valueOf(root.GetAttr("type"));
	_sourceLang = _detect.Detect(root.GetAttr("sourcelang"));

	_targetLang = _detect.Detect(root.GetAttr("targetlang"));

	for (XNode node : root.GetChildren()) {
	    if (0 == node.GetTagName().compareTo("EntityFrags")) {
		ParseEntityFrags(node);
	    }
	}

	EFragsToVFrags();
	EFragsToNotDoneAndDetails();

	return ret;
    }

    /**
     * 替换源文件的译文
     * 
     * @param sourceString
     * @param frag
     * @return
     */
    protected String CleanupBody(String sourceString, Fragmentation frag,
	    SENTENCE_STATE state) {
	String targetString = null;

	ArrayList<String> fragText = getFragContent(frag, 0,
		frag._sentenceCount, state);

	if (sourceString != null) {
	    if ((fragText != null) && (2 == fragText.size())) {
		String runsSource = fragText.get(0);
		//将biliFile对象的原文decode
		runsSource = Util.deCodeSpecialChart(runsSource);
		if (0 == runsSource.compareTo(sourceString)) {
		    targetString = fragText.get(1);
		}
	    }
	}

	return targetString;
    }
}
