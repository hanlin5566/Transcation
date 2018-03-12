package com.wiitrans.base.file.sdlxliff;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wiitrans.base.bundle.BundleConf;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.SDLXliffFragmentation;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class SDLXliffFileNoTag extends BiliFileNoTag {
    public HashMap<String, String> tagMap = null;
    public HashMap<String, Boolean> hasEndTagMap = null;
    public HashMap<String, String> specialTagMap = null;
    public int _totalwordcount;

    public SDLXliffFileNoTag() {
	_fileType = ENTITY_FILE_TYPE.SDLXLIFF;
    }

    @Override
    protected Fragmentation NewFrag() {
	return new SDLXliffFragmentation();
    }

    @Override
    public int Parse() {
	int ret = Const.FAIL;

	if (_sourceFilePath != null) {
	    try {
		XNode fragEntry = new XNode("EntityFrags");
		_xmlDoc.SetRoot(new XNode("File"));
		_xmlDoc.GetRoot().AddChild(fragEntry);
		// SDLXliffDOMParse domParse = new SDLXliffDOMParse();
		// domParse.parse(fragEntry, this);
		SDLXliffSAXParse saxParse = new SDLXliffSAXParse();
		saxParse.parse(fragEntry, this);
		_xmlDoc.GetRoot().SetAttr("sentencecount", _filesentencecount);
		_xmlDoc.GetRoot().SetAttr("totalwordcount",
			"" + _totalwordcount);
		// TODO:对有效字数向上取整
		_xmlDoc.GetRoot().SetAttr("wordcount", _filewordcount);
	    } catch (Exception e) {
		Log4j.error(e);
	    } finally {
	    }
	}
	return ret;
    }

    private void getNodeLevel(Node pNode, Map<String, String> nodeLevel) {
	// clean context
	if (pNode.hasChildNodes()) {
	    for (int i = 0; i < pNode.getChildNodes().getLength(); i++) {
		Node cNode = pNode.getChildNodes().item(i);
		short nodeType = cNode.getNodeType();
		switch (nodeType) {
		case Node.ELEMENT_NODE:
		    // 过滤mrk的子mrk节点
		    String cNodeName = cNode.getNodeName();
		    if (cNodeName.equals("mrk")
			    || cNode.getAttributes().getNamedItem("id") == null) {
			continue;
		    }
		    if (cNode.hasAttributes()) {
			String tagId = cNode.getAttributes().getNamedItem("id")
				.getNodeValue();
			nodeLevel.put(
				tagId,
				pNode.getNodeName().equals("mrk") ? "top"
					: pNode.getAttributes()
						.getNamedItem("id")
						.getNodeValue());
			if (cNode.hasChildNodes()) {
			    getNodeLevel(cNode, nodeLevel);
			}
		    }
		    break;
		case Node.TEXT_NODE:
		    // 内容
		    cNode.setTextContent("");
		    break;
		}
		// pNode.removeChild(cNode);
	    }
	} else {
	    pNode.setTextContent("");
	}
    }

    public void setContext(Node mrk, String str) {
	Map<String, String> nodeLevel = new LinkedHashMap<String, String>();
	Map<String, Node> tagIdMap = new LinkedHashMap<String, Node>();
	getNodeLevel(mrk, nodeLevel);
	// 清空所有
	mrk.setTextContent("");
	Pattern tagPattern = Pattern.compile("\\<[^\\>]*\\>");
	Matcher tagMatch = null;
	try {
	    tagMatch = tagPattern.matcher(str);
	} catch (Exception e) {
	    Log4j.error("content:" + str);
	    Log4j.error(e);
	    throw e;
	}
	if (tagMatch != null && Pattern.matches("(?s).*?\\<[^\\>]*\\>.*?", str)) {
	    // 匹配到标签
	    int pre_end = 0;
	    String start_tagId = "";
	    Element node = null;
	    while (tagMatch.find()) {
		int s = tagMatch.start();
		int e = tagMatch.end();
		if (s != pre_end) {
		    // 中间内容
		    if (node != null) {
			node.appendChild(node.getOwnerDocument()
				.createTextNode(
					Util.deCodeSpecialChart(str.substring(
						pre_end, s))));
		    } else {
			mrk.appendChild(mrk.getOwnerDocument().createTextNode(
				Util.deCodeSpecialChart(str.substring(pre_end,
					s))));
		    }
		}
		String tag = str.substring(s, e);
		// 非结束标签
		if (!Pattern.matches("</[^>]+>", tag)) {

		    if (tag.indexOf("name=\"") > 0
			    && tag.indexOf("tagId=\"") > 0) {
			String tagId = tag.split("tagId=\"")[1].replaceAll(
				"\".*", "");
			String tagName = tag.split("name=\"")[1].replaceAll(
				"\".*", "");
			if (nodeLevel.containsKey(tagId)
				&& !"top".equals(nodeLevel.get(tagId))
				&& tagIdMap.containsKey(nodeLevel.get(tagId))) {
			    // 不是顶级节点
			    Node pNode = tagIdMap.get(nodeLevel.get(tagId));
			    node = pNode.getOwnerDocument().createElement(
				    tagName);// 新建节点
			    node.setAttribute("id", tagId);
			    pNode.appendChild(node);
			    tagIdMap.put(tagId, node);
			} else {
			    // 顶级节点则添加到mrk
			    node = mrk.getOwnerDocument()
				    .createElement(tagName);// 新建节点
			    node.setAttribute("id", tagId);
			    mrk.appendChild(node);
			    tagIdMap.put(tagId, node);
			}
			start_tagId = tagId;
			// 如果没有结束标签则退回到上一标签，将内容添加至上一标签
			if (!hasEndTagMap.get(tagId)) {
			    // 结束标签则将node退到上一及
			    if (nodeLevel.containsKey(start_tagId)
				    && !"top"
					    .equals(nodeLevel.get(start_tagId))
				    && tagIdMap.containsKey(nodeLevel
					    .get(tagId))) {
				node = (Element) tagIdMap.get(nodeLevel
					.get(start_tagId));
				start_tagId = node.getAttributes()
					.getNamedItem("id").getNodeValue();
			    } else {
				node = null;
			    }
			}
		    } else {
			// 不是自己拼装的标签，按带尖叫号的内容处理
			if (node != null) {
			    node.appendChild(node.getOwnerDocument()
				    .createTextNode(
					    Util.deCodeSpecialChart(str
						    .substring(pre_end, s))));
			} else {
			    mrk.appendChild(mrk.getOwnerDocument()
				    .createTextNode(
					    Util.deCodeSpecialChart(str
						    .substring(pre_end, s))));
			}
		    }
		} else {
		    // 结束标签则将node退到上一及
		    if (nodeLevel.containsKey(start_tagId)
			    && !"top".equals(nodeLevel.get(start_tagId))) {
			node = (Element) tagIdMap.get(nodeLevel
				.get(start_tagId));
		    } else {
			node = null;
		    }
		}
		// 只有结束标签，未处理
		// else{
		// Element endTag =
		// mrk.getOwnerDocument().createElement("g");//结尾标签
		// endTag.setAttribute("id", ""+tagId);
		// mrk.appendChild(endTag);
		// }
		pre_end = e;
	    }
	    // 补尾
	    if (pre_end < str.length()) {
		String endContent = str.substring(pre_end, str.length());
		mrk.appendChild(mrk.getOwnerDocument().createTextNode(
			Util.deCodeSpecialChart(endContent)));
	    }
	} else {
	    mrk.appendChild(mrk.getOwnerDocument().createTextNode(
		    Util.deCodeSpecialChart(str)));
	}
    }

    @Override
    public int Cleanup(SENTENCE_STATE state) {
	int ret = Const.FAIL;

	if ((_sourceFilePath != null) && (_targetFilePath != null)) {
	    try {
		long s = System.currentTimeMillis();

		Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder().parse(new File(_sourceFilePath));
		Log4j.debug("parse doc " + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		SAXCollectTagUtil tagUtil = new SAXCollectTagUtil();
		tagUtil.parse(_sourceFilePath);
		this.hasEndTagMap = tagUtil.hasEndTagMap;
		this.tagMap = tagUtil.tagMap;
		this.specialTagMap = tagUtil.specialTagMap;
		Log4j.debug("collect tag " + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// collect mrks
		NodeList mrks = doc.getElementsByTagName("mrk");
		Map<String, Node> mrkMap = new LinkedHashMap<String, Node>();
		for (int i = 0; i < mrks.getLength(); i++) {
		    Node mrk = mrks.item(i);
		    String mtype = mrk.getAttributes().getNamedItem("mtype")
			    .getNodeValue();
		    String mid = "";
		    // System.out.println(i+" content:"+mrk.getTextContent());
		    if ("seg".equals(mtype)) {
			mid = mrk.getAttributes().getNamedItem("mid")
				.getNodeValue();
		    } else if ("x-sdl-comment".equals(mtype)
			    && SDLXliffDOMParse.isTransUnit(mrk)) {
			// 批注
			if(StringUtils.isNotEmpty(mrk.getTextContent())){//过滤没有内容的批注
			    mid = SDLXliffDOMParse.getMid(mrk);
			}
		    }
		    if (StringUtils.isNotEmpty(mid)) {
			mid = changeMid(mid);
			String tuType = SDLXliffDOMParse.getTuType(mrk);
			mrkMap.put(mid + "_" + tuType, mrk);
		    }
		}
		Log4j.debug("parse mrk " + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// clean up body
		int lineIndex = 1;
		for (String key : mrkMap.keySet()) {
		    String tuType = key.split("_")[1];
		    if (tuType.equals(SDLXliffDOMParse.TU_TYPE_SOURCE)) {
			String mid = key.split("_")[0];
			Node sourceMrk = mrkMap.get(key);
			Node targetMrk = mrkMap.get(mid + "_"
				+ SDLXliffDOMParse.TU_TYPE_TARGET);
			String sourceString = getContext(sourceMrk, tagMap,
				hasEndTagMap);
			// #3259 替换/n未<ent/>
			if (sourceString.indexOf("\n") >= 0) {
			    sourceString = sourceString.replaceAll("\n",
				    "<ent/> ");
			}
			if (StringUtils.isNotEmpty(sourceString)) {
			    String targetString = CleanupBody(sourceString,
				    _entityFrags.get(lineIndex - 1), state);
			    // 添加为空验证，本身为空应为异常
			    if (targetString == null) {
				targetString = sourceString;
			    }
			    // #3259 clean时 <ent/>替换成 /n
			    if (targetString.indexOf("<ent/> ") >= 0) {
				targetString = targetString.replaceAll(
					"<ent/> ", "\n");
			    }
			    this.setContext(targetMrk, targetString);
			    lineIndex++;
			}
		    }
		}
		Log4j.debug("clean " + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		// change sentence state to translated
		// collect segDef
		if(BundleConf.BUNDLE_WRITE_TRANSLATED){
		    NodeList segsDef = doc.getElementsByTagName("sdl:seg");
		    Log4j.debug("sdl:seg length"+ segsDef.getLength());
		    for (int i = 0; i < segsDef.getLength(); i++) {
			Node segDef = segsDef.item(i);
			Node confAtt = segDef.getOwnerDocument().createAttribute("conf");
			Node originAtt = segDef.getOwnerDocument().createAttribute("origin");
			confAtt.setNodeValue("Translated");
			originAtt.setNodeValue("interactive");
			segDef.getAttributes().setNamedItem(confAtt);
			segDef.getAttributes().setNamedItem(originAtt);
//		    ((Element)segDef).setAttribute("conf", "Translated");
//		    ((Element)segDef).setAttribute("origin", "interactive");
		    }
		    Log4j.debug("change sentence state to translated :" + (System.currentTimeMillis() - s));
		    s = System.currentTimeMillis();
		}
		// targetNode.setTextContent("");
		// Util.setTagAttBySource(targetString, sourceNode, targetNode);

		// save document
		// //复制原文
		// int buff = 0;
		// InputStream in = new FileInputStream(new
		// File(_sourceFilePath));
		// OutputStream out = new FileOutputStream(new
		// File(_targetFilePath));
		// byte [] bytes = new byte[1024];
		// while((buff = in.read(bytes)) != -1){
		// out.write(bytes, 0, buff);
		// }
		// in.close();
		// out.close();
		Log4j.debug("write doc" + (System.currentTimeMillis() - s));
		s = System.currentTimeMillis();
		File file = new File(_targetFilePath);
		StreamResult result = new StreamResult(file);
		Transformer transformer = TransformerFactory.newInstance()
			.newTransformer();
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		ret = Const.SUCCESS;
	    } catch (Exception e) {
		Log4j.error(e);
	    } finally {

	    }
	} else {
	    Log4j.error("Path is not exist.");
	}

	return ret;
    }

    public String changeMid(String mid) {
	mid = mid.replaceAll("_x0020_", " ");
	return mid;
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
			context.append("</" + tag + " tagId=\"" + tagId + "\">");
		    }
		} else if (xNode.getNodeType() == Node.TEXT_NODE) {
		    // 内容
		    context.append(Util.replaceCodeSpecialChat(xNode
			    .getTextContent()));
		}
	    }
	}
    }

    public String getContext(Node node, HashMap<String, String> tagMap,
	    HashMap<String, Boolean> hasEndTagMap) throws Exception {
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
			Log4j.info("未发现标签,file:" + GetBiliFilePath() + " tag:"
				+ tagId);
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

    public int ParseBili() {
	int ret = Const.FAIL;

	_entityFrags.clear();
	_virtualFrags.clear();

	XNode root = _xmlDoc.Parse(_biliFilePath);
	_fileId = root.GetAttr("id");
	_sentenceCount = Util.String2Int(root.GetAttr("sentencecount"));
	_wordCount = Util.String2Int(root.GetAttr("wordcount"));
	_totalwordcount = Util.String2Int(root.GetAttr("totalwordcount"));
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
		if (0 == runsSource.compareTo(sourceString)) {
		    targetString = fragText.get(1);
		}
	    }
	}
	return targetString;
    }
}
