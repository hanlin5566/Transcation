package com.wiitrans.base.file.sdlxliff;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sdlxliff.parse.SDLXliffParse;
import com.wiitrans.base.file.sdlxliff.parse.SDLXliffParseDefaultImpl;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class SDLXliffSAXParse extends DefaultHandler {

    private int tuType = 0;
    private String qName = null;
    private String mid = null;
    private String mtype = null;
    private String tagId = null;
    private String gId = null;//gId

    private StringBuffer contentBuffer;
    private BiliFileNoTag fileNoTag;
    private XNode fragEntry;
    private SDLXliffParse parse;

    private HashMap<String, String> tagTreeMap = new HashMap<String, String>();
    private List<String> mrkMtypeList = new ArrayList<String>();// 记录父集子集的mtype，当为0时说明全部mrk结束。如果出现了结束标签则上一次标签的mtpye

    public LinkedHashMap<String, String> tagMap = new LinkedHashMap<String, String>();
    public HashMap<String, String> specialTagMap = new HashMap<String, String>();
    public HashMap<String, Boolean> hasEndTagMap = new HashMap<String, Boolean>();
    public HashMap<String, Boolean> isLockMap = new HashMap<String, Boolean>();
    public HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
    public LinkedHashMap<String, String> sentenceMap = new LinkedHashMap<String, String>();

    public final static int TU_TYPE_SOURCE = 1;
    public final static int TU_TYPE_TARGET = 2;

    public void parse(XNode fragEntry, BiliFileNoTag fileNoTag)
	    throws Exception {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser parser = factory.newSAXParser();
	this.fileNoTag = fileNoTag;
	this.fragEntry = fragEntry;
	try {
	    parser.parse(new File(fileNoTag._sourceFilePath), this);
	} catch (Exception e) {
	    Log4j.error(fileNoTag._sourceFilePath);
	    Log4j.error(e);
	}
    }

    @Override
    public void characters(char[] ch, int start, int length)
	    throws SAXException {
	if (qName == null || tuType <= 0) {
	    return;
	}
	String content = new String(ch, start, length);
	switch (this.qName) {
	case "mrk":
	    content = Util.replaceCodeSpecialChat(content);
	    // #3259 替换/n未<ent/>
	    if (content.indexOf("\n") >= 0) {
		content = content.replaceAll("\n", "<ent/> ");
	    }
	    contentBuffer.append(content);
	    break;
	case "g":
	    content = Util.replaceCodeSpecialChat(content);
	    // #3259 替换/n未<ent/>
	    if (content.indexOf("\n") >= 0) {
		content = content.replaceAll("\n", "<ent/> ");
	    }
	    contentBuffer.append(content);
	    break;
	case "x":
	    content = Util.replaceCodeSpecialChat(content);
	    // #3259 替换/n未<ent/>
	    if (content.indexOf("\n") >= 0) {
		content = content.replaceAll("\n", "<ent/> ");
	    }
	    contentBuffer.append(content);
	    break;
	default:
	    break;
	}
    }

    @Override
    public void endDocument() throws SAXException {
	int lineIndex = 0;
	for (String key : sentenceMap.keySet()) {
	    String mid = key.split("_")[0];
	    int tuType = Integer.valueOf(key.split("_")[1]);
	    if (TU_TYPE_SOURCE == tuType) {
		String source = sentenceMap.get(key);
		String target = sentenceMap.get(mid + "_" + TU_TYPE_TARGET);
		boolean lock = isLockMap.get(mid);
		int percent = percentMap.containsKey(mid) ? percentMap.get(mid)
			: 0;
		if (StringUtils.isNotEmpty(source)) {
		    parse.parseBody(fragEntry, source, target, lineIndex++, mid,
			    lock, percent, fileNoTag);
		}
	    }
	}
    }

    @Override
    public void endElement(String uri, String localName, String qName)
	    throws SAXException {
	switch (qName) {
	case "mrk":
	    // mrk节点如x-sdl-location --拆分位置标记 x-sdl-comment 批注
	    if (mid != null) {
		mtype = mrkMtypeList.remove((mrkMtypeList.size()-1));//倒序拿mrktype
		// mtype = x-sdl-location 或者撒批注 x-sdl-comment 并且已经结束则按父级seg处理
		// 或者是父级seq
		if ("seg".equals(mtype)) {
		    sentenceMap.put(mid + "_" + tuType,contentBuffer.toString());
		    mid = null;
		    contentBuffer = new StringBuffer();
		    this.qName = null;
		    this.mtype = null;
		}
	    }
	    break;
	case "seg-source":
	    tagTreeMap = new HashMap<String, String>();//清空tag父子关系map
	    tuType = 0;// 置成默认值
	    break;
	case "target":
	    tagTreeMap = new HashMap<String, String>();//清空tag父子关系map
	    tuType = 0;// 置成默认值
	    break;
	// 标签
	case "g":
	    if (mid != null) {
		this.addEndTag(gId);
		//如果有父tag则id置为父tag
		if(tagTreeMap.containsKey(gId)){
		    gId = tagTreeMap.get(gId);
		}else{
		    //如果为父tag则置为空
		    gId = null;
		}
	    }
	    break;
	case "x":
	    if (mid != null) {
		this.addEndTag(gId);
		//如果有父tag则id置为父tag
		if(tagTreeMap.containsKey(gId)){
		    gId = tagTreeMap.get(gId);
		}else{
		    //如果为父tag则置为空
		    gId = null;
		}
	    }
	    break;
	default:
	    break;
	}
    }

    @Override
    public void startDocument() throws SAXException {
	contentBuffer = new StringBuffer();
	parse = new SDLXliffParseDefaultImpl();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) throws SAXException {
	switch (qName) {
	// tag
	case "tag":
	    tagId = attributes.getValue("id");
	    break;
	// bpt tag
	case "bpt":
	    this.qName = qName;
	    tagMap.put(tagId, Util.cleanSpecialChat(attributes.getValue("name")));
	    hasEndTagMap.put(tagId, true);
	    break;
	// ph tag
	case "ph":
	    this.qName = qName;
	    tagMap.put(tagId, Util.cleanSpecialChat(attributes.getValue("name")));
	    break;
	// st tag
	case "st":
	    this.qName = qName;
	    tagMap.put(tagId, Util.cleanSpecialChat(attributes.getValue("name")));
	    break;
	case "seg-source":
	    tuType = SDLXliffSAXParse.TU_TYPE_SOURCE;
	    break;
	case "target":
	    tuType = SDLXliffSAXParse.TU_TYPE_TARGET;
	    break;
	case "mrk":
	    // source 或 target
	    if (tuType > 0) {
		// x-sdl-comment 批注 x-sdl-location 合并单元后的坐标标记 mid为上级mrk的mid 不处理
		mtype = attributes.getValue("mtype");
		mrkMtypeList.add(mtype);
		this.qName = qName;
		if ("seg".equals(mtype)) {
		    mid = this.changeMid(attributes.getValue("mid"));
		}
	    }
	    break;
	// sdl描述
	case "sdl:seg":
	    String id = attributes.getValue("id");
	    String locked = attributes.getValue("locked");
	    String percent = attributes.getValue("percent");
	    isLockMap.put(
		    id,
		    StringUtils.isEmpty(locked) ? false : Boolean
			    .valueOf(locked));
	    percentMap
		    .put(id,
			    StringUtils.isEmpty(percent) ? 0 : Integer
				    .valueOf(percent));
	    break;
	// 标签
	case "g":
	    if (mid != null) {
		// 放入父及tagId
		if (StringUtils.isNotEmpty(gId)) {
		    tagTreeMap.put(attributes.getValue("id"),gId);
		}
		gId = attributes.getValue("id");
		this.addTag(gId, qName);
	    }
	    break;
	case "x":
	    if (mid != null) {
		// 放入父及tagId
		if (StringUtils.isNotEmpty(gId)) {
		    tagTreeMap.put( attributes.getValue("id"),gId);
		}
		gId = attributes.getValue("id");
		this.addTag(gId, qName);
	    }
	    break;
	default:
	    break;
	}
    }

    private String changeMid(String mid) {
	mid = mid.replaceAll("_x0020_", " ");
	return mid;
    }

    private void addTag(String tagId, String qName) {
	if (tagMap.containsKey(tagId)) {
	    String tag = tagMap.get(tagId);
	    if (specialTagMap.containsKey(tagId)) {
		contentBuffer.append("<" + tag + " name=\"" + qName
			+ "\" tagId=\"" + tagId + "\" value=\""
			+ specialTagMap.get(tagId) + "\">");
	    } else {
		contentBuffer.append("<" + tag + " name=\"" + qName
			+ "\" tagId=\"" + tagId + "\">");
	    }
	} else {
	    // Log4j.info("未发现标签,file:" + fileNoTag._sourceFilePath + " tag:"
	    // + tagId);
	}
    }

    private void addEndTag(String tagId) {
	if (tagMap.containsKey(tagId) && hasEndTagMap.containsKey(tagId)) {
	    String tag = tagMap.get(tagId);
	    // 节点有结束标签
	    contentBuffer.append("</" + tag + " tagId=\"" + tagId + "\">");
	} else {
	    // Log4j.info("未发现标签,file:" + fileNoTag._sourceFilePath + " tag:"
	    // + tagId);
	}
    }
}
