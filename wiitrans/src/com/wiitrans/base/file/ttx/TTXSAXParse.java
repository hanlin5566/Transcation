package com.wiitrans.base.file.ttx;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.mail.handlers.text_html;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.ttx.parse.TTXParse;
import com.wiitrans.base.file.ttx.parse.TTXParseDocImpl;
import com.wiitrans.base.file.ttx.parse.TTXParseDocxImpl;
import com.wiitrans.base.file.ttx.parse.TTXParseHtmlImpl;
import com.wiitrans.base.file.ttx.parse.TTXParsePptImpl;
import com.wiitrans.base.file.ttx.parse.TTXParsePptxImpl;
import com.wiitrans.base.file.ttx.parse.TTXParseXlsImpl;
import com.wiitrans.base.file.ttx.parse.TTXParseXlsxImpl;
import com.wiitrans.base.file.ttx.parse.TTXParseXmlImpl;
import com.wiitrans.base.misc.Util;
import com.wiitrans.base.xml.XNode;

public class TTXSAXParse extends DefaultHandler{
	
	private String preTagName = null;
	private TTXParse parse;
	private int isSource = 0;//1:source 0:target
	private int lineIndex = 0;
	private int percent = 0;
	private String source = null;
	private String target = null;
	private XNode fragEntry;
	private BiliFileNoTag fileNoTag;
	private StringBuffer sBuffer;
	private boolean isTag = false;
	private String tagAtt_key;
	private JSONObject tagattJson;
	private StringBuffer ttxTagsBuffer;
//	private StringBuffer ttxContentBuffer;
	private final List<String> convertedType = Arrays.asList("PlugInConverted","RTF");
	public void parse(XNode fragEntry,BiliFileNoTag fileNoTag) throws Exception{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		this.fragEntry = fragEntry;
		this.fileNoTag = fileNoTag;
		try {
			parser.parse(new File(fileNoTag._sourceFilePath),this);
		} catch (Exception e) {
			//TODO:暂时注册
			//System.err.println(fileNoTag._sourceFilePath+"parse error case"+e.fillInStackTrace());
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
	    String str = new String(ch,start,length);
		if("Tuv".equalsIgnoreCase(preTagName)){
//			System.out.println(str);
			sBuffer.append(str);
		}
		
		if(isTag){
			ttxTagsBuffer.append(str);
		}
//		else{
//		    ttxContentBuffer.append(str);
//		}
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(isTag){
//			ttxTagsBuffer.append("  [FILE]:"+fileNoTag._sourceFilePath);
//			System.out.println(ttxTagsBuffer.toString());
//			ttxTagsBuffer = new StringBuffer();
			
			tagAtt_key = ttxTagsBuffer.toString();
			Util.addTagAtt(tagAtt_key, tagattJson);
			
			ttxTagsBuffer = new StringBuffer();
			isTag = false;
			tagAtt_key = "";
			tagattJson = new JSONObject();
		}
		//Tu end
		switch (qName) {
		case "Tu":
			if (source !=null && target !=null) {
//				System.out.println("source:"+source+" target:"+target);
				if(parse != null){
					parse.ParseBody(fragEntry, source, target, lineIndex++,percent,this.fileNoTag);
					percent = 0;
				}
			}
			break;
		case "Tuv":
			isSource = isSource == 0?1:0;
			if(sBuffer != null && sBuffer.length() > 0){
			    	String str = Util.enCodeSpecialChart(sBuffer.toString());
//			    	String ss = ttxContentBuffer.toString();
//			    	System.err.println(ss);
				if(isSource == 1){
					source= str;
				}else{
					target = str;
				}
				sBuffer = new StringBuffer();
//				ttxContentBuffer = new StringBuffer();
				str = null;
			}
			preTagName = null;
			break;
		default:
			break;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		sBuffer = new StringBuffer();
		tagAtt_key = "";
		tagattJson = new JSONObject();
		ttxTagsBuffer = new StringBuffer();
//		ttxContentBuffer = new StringBuffer();
		//改为通过文件读取
//		String originalFileName = fileNoTag._originalFileName.substring(0,fileNoTag._originalFileName.lastIndexOf('.'));
//		String ext = originalFileName.substring(originalFileName.lastIndexOf('.')+1, originalFileName.length());
//		//通过工厂模式拿出相应的解析类
//		switch (ext.toLowerCase()) {
//		case "doc": {
//			parse = new TTXParseDocImpl();
//			break;
//		}
//		case "docx": {
//			parse = new TTXParseDocxImpl();
//			break;
//		}
//		case "ppt": {
//			parse = new TTXParsePptImpl();
//			break;
//		}
//		case "pptx": {
//			parse = new TTXParsePptxImpl();
//			break;
//		}
//		case "xls": {
//			parse = new TTXParseXlsImpl();
//			break;
//		}
//		case "xlsx": {
//			parse = new TTXParseXlsxImpl();
//			break;
//		}
//		case "xml": {
//			parse = new TTXParseXmlImpl();
//			break;
//		}
//		case "html": {
//			parse = new TTXParseHtmlImpl();
//			break;
//		}
//		default:
//			System.err.println("unsupported type unparse"+ ext + " path"+fileNoTag._sourceFilePath);
//			break;
//		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if("Tuv".equalsIgnoreCase(preTagName) && "ut".equalsIgnoreCase(qName)){
//			String str = "";
			String displayText = "";
			for (int i = 0; i < attributes.getLength(); i++) {
				String attName = attributes.getQName(i);
				String attValue = attributes.getValue(i);
				if("DisplayText".equalsIgnoreCase(attName)){
					displayText = attValue;
				}
//				str+=attName+"="+attValue+" ";
				tagattJson.put(attName, attValue);
			}
//				System.out.println(ss+str+">");
			if(StringUtils.isNotEmpty(displayText)){
//				ttxTagsBuffer.append("[TAG]:<"+qName+" ");
				// && !Util.ttxTagList.contains(displayText)
//				ttxTagsBuffer.append(str+">   [TEXT]:");
				isTag = true;
			}
		}
		switch (qName) {
		case "Tuv":
			preTagName = qName;
			break;
		case "Tu":
		    percent = StringUtils.isNotEmpty(attributes.getValue("MatchPercent"))?Integer.valueOf(attributes.getValue("MatchPercent")):0;
		    break;
		case "UserSettings":
			//使用文件内的扩展名
			String ext = attributes.getValue("DataType");
			if(convertedType.contains(ext)){
				//如果是插件文件需要转换则取文件内后缀名
				String sourceDocumentPath = attributes.getValue("SourceDocumentPath");
				ext = sourceDocumentPath.substring(sourceDocumentPath.lastIndexOf('.')+1, sourceDocumentPath.length());
			}
			//通过工厂模式拿出相应的解析类
			switch (ext.toLowerCase()) {
			case "doc": {
				parse = new TTXParseDocImpl();
				break;
			}
			case "docx": {
				parse = new TTXParseDocxImpl();
				break;
			}
			case "ppt": {
				parse = new TTXParsePptImpl();
				break;
			}
			case "pptx": {
				parse = new TTXParsePptxImpl();
				break;
			}
			case "xls": {
				parse = new TTXParseXlsImpl();
				break;
			}
			case "xlsx": {
				parse = new TTXParseXlsxImpl();
				break;
			}
			case "xml": {
				parse = new TTXParseXmlImpl();
				break;
			}
			case "html": {
				parse = new TTXParseHtmlImpl();
				break;
			}
			default:
				//TODO:暂时注释
//				System.err.println("unsupported type, can't parse "+ ext + " name "+fileNoTag._originalFileName);
				break;
			}
		default:
			break;
		}
	}
}
