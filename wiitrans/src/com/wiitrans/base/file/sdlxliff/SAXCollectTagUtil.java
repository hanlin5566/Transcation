package com.wiitrans.base.file.sdlxliff;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Util;

public class SAXCollectTagUtil extends DefaultHandler {

    private String tagId = null;

    public LinkedHashMap<String, String> tagMap = new LinkedHashMap<String, String>();
    public HashMap<String, String> specialTagMap = new HashMap<String, String>();
    public HashMap<String, Boolean> hasEndTagMap = new HashMap<String, Boolean>();
    public HashMap<String, Boolean> isLockMap = new HashMap<String, Boolean>();
    public HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
    
    public void parse(String filePath) throws Exception {
	SAXParserFactory factory = SAXParserFactory.newInstance();
	SAXParser parser = factory.newSAXParser();
	try {
	    parser.parse(new File(filePath), this);
	} catch (Exception e) {
	    Log4j.error(filePath);
	    Log4j.error(e);
	}
    }

    @Override
    public void characters(char[] ch, int start, int length)
	    throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void endElement(String uri, String localName, String qName)
	    throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
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
	    tagMap.put(tagId,
		    Util.cleanSpecialChat(attributes.getValue("name")));
	    hasEndTagMap.put(tagId, true);
	    break;
	// ph tag
	case "ph":
	    tagMap.put(tagId,
		    Util.cleanSpecialChat(attributes.getValue("name")));
	    hasEndTagMap.put(tagId, false);
	    break;
	// st tag
	case "st":
	    tagMap.put(tagId,
		    Util.cleanSpecialChat(attributes.getValue("name")));
	    hasEndTagMap.put(tagId, false);
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
	default:
	    break;
	}
    }
}