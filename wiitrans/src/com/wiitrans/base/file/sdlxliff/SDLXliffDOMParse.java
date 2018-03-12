package com.wiitrans.base.file.sdlxliff;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NodeType;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sdlxliff.parse.SDLXliffParse;
import com.wiitrans.base.file.sdlxliff.parse.SDLXliffParseDefaultImpl;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class SDLXliffDOMParse {
    private BiliFileNoTag fileNoTag;
    private SDLXliffParse parse;
    private HashMap<String, String> tagMap = new HashMap<String, String>();
    private HashMap<String, String> specialTagMap = new HashMap<String, String>();
    private HashMap<String, Boolean> hasEndTagMap = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> isLockMap = new HashMap<String, Boolean>();
    private HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
    private LinkedHashMap<String, String> sentenceMap = new LinkedHashMap<String, String>();
    public final static String TU_TYPE_SOURCE = "source";
    public final static String TU_TYPE_TARGET = "target";

    public void parse(XNode fragEntry, BiliFileNoTag fileNoTag)
	    throws Exception {
	try {
	    this.fileNoTag = fileNoTag;
	    Document doc = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder()
		    .parse(new File(fileNoTag._sourceFilePath));
	    // collect segDef
	    NodeList segDef = doc.getElementsByTagName("sdl:seg");
	    this.collectsegsDef(segDef);
	    NodeList tags = doc.getElementsByTagName("tag");
	    this.collectTags(tags);
	    NodeList mrks = doc.getElementsByTagName("mrk");
	    this.collectSentence(mrks);
	    parse = new SDLXliffParseDefaultImpl();
	    int lineIndex = 1;
	    for (String key : sentenceMap.keySet()) {
		String mid = key.split("_")[0];
		String tuType = key.split("_")[1];
		if (TU_TYPE_SOURCE.equals(tuType)) {
		    String source = sentenceMap.get(key);
		    String target = sentenceMap.get(mid + "_" + TU_TYPE_TARGET);
		    boolean lock = isLockMap.get(mid);
		    int percent = percentMap.containsKey(mid) ? percentMap
			    .get(mid) : 0;
		    if (StringUtils.isNotEmpty(source)) {
			parse.parseBody(fragEntry, source, target, lineIndex,
				mid, lock, percent, fileNoTag);
			lineIndex++;
		    }
		}
		// System.out.println(key+"     "+ sentenceMap.get(key));
	    }
	} catch (Exception e) {
	    Log4j.error("parse sdl exception: flie=" + fileNoTag._biliFilePath,
		    e);
	    Log4j.error(e);
	}
    }
    
    private String changeMid(String mid){
	mid = mid.replaceAll("_x0020_", " ");
	return mid;
    }
    
    private void collectSentence(NodeList mrks) throws Exception {
	for (int i = 0; i < mrks.getLength(); i++) {
	    Node mrk = mrks.item(i);
	    String mtype = mrk.getAttributes().getNamedItem("mtype")
		    .getNodeValue();
	    String mid = "";
	    if ("seg".equals(mtype)) {
		mid = mrk.getAttributes().getNamedItem("mid")
			.getNodeValue();
	    }else if("x-sdl-comment".equals(mtype) && isTransUnit(mrk)){
		//seg批注
		if(StringUtils.isNotEmpty(mrk.getTextContent())){//过滤没有内容的批注
		    mid = SDLXliffDOMParse.getMid(mrk);
		}
	    }
	    if(StringUtils.isNotEmpty(mid)){
		mid = changeMid(mid);
		String tuType = getTuType(mrk);
		String sentenceKey = mid + "_" + tuType;
		String sentence = getContext(mrk);
		//#3259 替换/n未<ent/>
		if (sentence.indexOf("\n") >= 0) {
		    sentence = sentence.replaceAll("\n", "<ent/> ");
		}
		// System.out.println(sentence);
		if (sentenceMap.containsKey(sentenceKey)) {
		    sentenceMap.put(sentenceKey, sentenceMap.get(sentenceKey)
			    + sentence);
		} else {
		    sentenceMap.put(sentenceKey, sentence);
		}
	    }
	}
    }

    private void collectsegsDef(NodeList segsDef) throws Exception {
	for (int i = 0; i < segsDef.getLength(); i++) {
	    Node segDef = segsDef.item(i);
	    if (segDef.getNodeType() == Node.ELEMENT_NODE) {
		if (segDef.hasAttributes()) {
		    Node segIdNode = segDef.getAttributes().getNamedItem("id");
		    Node percentNode = segDef.getAttributes().getNamedItem(
			    "percent");
		    Node lockedNode = segDef.getAttributes().getNamedItem(
			    "locked");
		    if (segIdNode != null) {
			String segId = segIdNode.getNodeValue();
			int percent = percentNode != null ? Integer
				.valueOf(percentNode.getNodeValue()) : 0;
			boolean locked = lockedNode != null ? Boolean
				.valueOf(lockedNode.getNodeValue()) : false;
			isLockMap.put(segId, locked);
			percentMap.put(segId, percent);
		    }
		}
	    }
	}
    }
    
    public static String getMid(Node mrk){
	String mid = "";
	if(mrk.getParentNode().getAttributes() == null){
	    SDLXliffDOMParse.getMid(mrk.getParentNode());
	}else{
	    Node midNode = mrk.getParentNode().getAttributes().getNamedItem("mid");
	    if(midNode == null){
		mid = SDLXliffDOMParse.getMid(mrk.getParentNode());
	    }else{
		mid = midNode.getNodeValue();
	    }
	}
	return mid;
    }
    //过滤不是seqsoucre，全source
    public static boolean isTransUnit(Node mrk){
	boolean isseg = false;
	String name = mrk.getParentNode().getNodeName();
	if("source".equals(name)){
	    isseg = false;
	}else if("seg-source".equals(name)){
	    isseg = true;
	}else if("target".equals(name)){
	    isseg = true;
	}else{
	    isseg = isTransUnit(mrk.getParentNode());
	}
	return isseg;
    }
    
    private void collectTags(NodeList tags) throws Exception {
	for (int i = 0; i < tags.getLength(); i++) {
	    Node tag = tags.item(i);
	    String tagId = null;
	    String tagName = null;
	    boolean hasEndTag = false;
	    if (tag.getNodeType() == Node.ELEMENT_NODE) {
		if (tag.hasAttributes()) {
		    tagId = tag.getAttributes().getNamedItem("id").getNodeValue();
		    String qName = tag.getFirstChild().getNodeName();
		    if("bpt".equals(qName)){
			//有结束标签
			hasEndTag = true;
		    }
		    if (tag.hasChildNodes()) {
			Node tagNameNode = tag.getFirstChild().getAttributes().getNamedItem("name");
			tagName = tagNameNode != null ? tagNameNode.getNodeValue() : "";
//			NodeList cNodes = tag.getChildNodes();
			// 结束标签通常在尾部，所以倒循环
//			for (int j = cNodes.getLength() - 1; j >= 0; j--) {
//			    Node cNode = cNodes.item(j);
//			    String text = cNode.getTextContent();
//			    if (Pattern.matches("\\</.*?\\>", text)) {
//				hasEndTag = true;
//			    }
//			    // 判断是否未特殊标签,如果有则放入特殊标签map
//			    if (cNode.hasAttributes()
//				    && cNode.getAttributes().getNamedItem(
//					    "equiv-text") != null) {
//				specialTagMap.put(tagId, cNode.getAttributes()
//					.getNamedItem("equiv-text")
//					.getNodeValue());
//				tagName = "special";
//			    }
//			}
		    }
		}
		try {
		    tagMap.put(tagId, Util.cleanSpecialChat(tagName.toString()));
		    hasEndTagMap.put(tagId, hasEndTag);
		} catch (Exception e) {
		    Log4j.info("put tag exception: tagId=" + tagId
			    + "   tagName=" + tagName);
		    Log4j.error(e);
		}
	    }
	}
    }

    public static String getTuType(Node node) throws Exception {
	String type = "";
	String parentName = node.getParentNode().getNodeName();
	Node parentNode = node.getParentNode();
	if (parentName.indexOf("source") >= 0) {
	    type = TU_TYPE_SOURCE;
	} else if (parentName.indexOf("target") >= 0) {
	    type = TU_TYPE_TARGET;
	} else {
	    type = getTuType(parentNode);
	}
	return type;
    }

    private void setXTag(Node gNode, StringBuffer context) throws Exception {
	if (gNode.hasChildNodes()) {
	    NodeList xNodes = gNode.getChildNodes();
	    for (int i = 0; i < xNodes.getLength(); i++) {
		Node xNode = xNodes.item(i);
		if (xNode.getNodeType() == Node.ELEMENT_NODE) {
		    // 过滤mrk的子mrk节点
		    String cNodeName = xNode.getNodeName();
		    if (cNodeName.equals("mrk")
			    || xNode.getAttributes().getNamedItem("id") == null) {
			continue;
		    }
		    String tagId = xNode.getAttributes().getNamedItem("id")
			    .getNodeValue();
		    String nodeName = xNode.getNodeName();
		    String tag = tagMap.get(tagId);
		    // 子节点有特殊标签
		    if (specialTagMap.containsKey(tagId)) {
			context.append("<" + tag + " name=\"" + nodeName
				+ "\" tagId=\"" + tagId + "\" value=\""
				+ specialTagMap.get(tagId) + "\">");
		    } else {
			context.append("<" + tag + " name=\"" + nodeName
				+ "\" tagId=\"" + tagId + "\">");
		    }
		    setXTag(xNode, context);
		    if (hasEndTagMap.get(tagId)) {
			// 子节点有结束标签
			context.append("</" + tag + " tagId=\"" + tagId
				+ "\">");
		    }
		} else if (xNode.getNodeType() == Node.TEXT_NODE) {
		    // 内容
		    context.append(Util.replaceCodeSpecialChat(xNode
			    .getTextContent()));
		}
	    }
	}
    }

    private String getContext(Node node) throws Exception {
	StringBuffer context = new StringBuffer();
	if (node.hasChildNodes()) {
	    NodeList cNodes = node.getChildNodes();
	    for (int i = 0; i < cNodes.getLength(); i++) {
		Node cNode = cNodes.item(i);
		short nodeType = cNode.getNodeType();
		switch (nodeType) {
		case Node.ELEMENT_NODE:
		    // 过滤mrk的子mrk节点
		    String cNodeName = cNode.getNodeName();
		    if (cNodeName.equals("mrk")) {
			continue;
		    }
		    // 标签
		    String tagId = cNode.getAttributes().getNamedItem("id")
			    .getNodeValue();
		    String nodeName = cNode.getNodeName();
		    if (tagMap.containsKey(tagId)) {
			String tag = tagMap.get(tagId);
			if (specialTagMap.containsKey(tagId)) {
			    context.append("<" + tag + " name=\"" + nodeName
				    + "\" tagId=\"" + tagId + "\" value=\""
				    + specialTagMap.get(tagId) + "\">");
			} else {
			    context.append("<" + tag + " name=\"" + nodeName
				    + "\" tagId=\"" + tagId + "\">");
			}
			// 添加子节点内容
			this.setXTag(cNode, context);
			// 节点有结束标签
			if (hasEndTagMap.get(tagId)) {
			    context.append("</" + tag + " tagId=\"" + tagId
				    + "\">");
			}
		    } else {
			Log4j.info("未发现标签,file:" + fileNoTag._sourceFilePath
				+ " tag:" + tagId);
		    }
		    break;
		case Node.TEXT_NODE:
		    // 内容
		    context.append(Util.replaceCodeSpecialChat(cNode
			    .getTextContent()));
		    break;
		default:
		    Log4j.error("unsupport nodeType:" + cNode.getNodeType());
		    break;
		}
	    }
	} else {
	    // 未找到子节点直接返回内容
	    context.append(Util.replaceCodeSpecialChat(node.getTextContent()));
	}
	return context.toString();
    }
}
