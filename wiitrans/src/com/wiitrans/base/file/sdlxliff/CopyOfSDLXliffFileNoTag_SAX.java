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

import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.frag.SDLXliffFragmentation;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sentence.SentenceState.SENTENCE_STATE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class CopyOfSDLXliffFileNoTag_SAX extends BiliFileNoTag {
    // private HashMap<String, String> tagMap = new HashMap<String, String>();
    private HashMap<String, Boolean> hasEndTagMap = new HashMap<String, Boolean>();
    // private HashMap<String, String> specialTagMap = new HashMap<String,
    // String>();
    public int _totalwordcount;

    public CopyOfSDLXliffFileNoTag_SAX() {
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

    private void setContext(Node mrk, String str) {
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
		Document doc = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder().parse(new File(_sourceFilePath));
		// collect mrks
		NodeList mrks = doc.getElementsByTagName("mrk");
		Map<String, Node> mrkMap = new LinkedHashMap<String, Node>();
		for (int i = 0; i < mrks.getLength(); i++) {
		    Node mrk = mrks.item(i);
		    String mtype = mrk.getAttributes().getNamedItem("mtype").getNodeValue();
		    String mid = "";
		    if ("seg".equals(mtype)) {
			mid = mrk.getAttributes().getNamedItem("mid")
				.getNodeValue();
		    }else if("x-sdl-comment".equals(mtype)&& SDLXliffDOMParse.isTransUnit(mrk)){
			//批注
			mid = this.getMid(mrk);
		    }
		    if(StringUtils.isNotEmpty(mid)){
			mid = changeMid(mid);
			String tuType = SDLXliffDOMParse.getTuType(mrk);
			mrkMap.put(mid + "_" + tuType, mrk);
		    }
		}
		// clean up body
		int lineIndex = 1;
		for (String key : mrkMap.keySet()) {
		    String tuType = key.split("_")[1];
		    if (tuType.equals(SDLXliffDOMParse.TU_TYPE_SOURCE)) {
			Node sourceMrk = mrkMap.get(key);
			if(StringUtils.isNotEmpty(sourceMrk.getTextContent())){
			    String mid = key.split("_")[0];
			    Node targetMrk = mrkMap.get(mid + "_"
				    + SDLXliffDOMParse.TU_TYPE_TARGET);
			    String targetString = CleanupBody(
				    _entityFrags.get(lineIndex - 1), state);
			    // #3259 clean时 <ent/>替换成 /n
			    if (targetString.indexOf("<ent/> ") >= 0) {
				targetString = targetString.replaceAll("<ent/> ",
					"\n");
			    }
			    this.setContext(targetMrk, targetString);
			    lineIndex++;
			}
		    }
		}
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
    
    private String getMid(Node mrk){
	String mid = "";
	Node midNode = mrk.getParentNode().getAttributes().getNamedItem("mid");
	if(midNode == null){
	    mid = this.getMid(mrk.getParentNode());
	}else{
	    mid = midNode.getNodeValue();
	}
	return mid;
    }
    
    private String changeMid(String mid) {
	mid = mid.replaceAll("_x0020_", " ");
	return mid;
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
    protected String CleanupBody(Fragmentation frag, SENTENCE_STATE state) {
	String targetString = null;

	ArrayList<String> fragText = getFragContent(frag, 0,
		frag._sentenceCount, state);

	if ((fragText != null) && (2 == fragText.size())) {
	    targetString = fragText.get(1);
	}
	return targetString;
    }
}
