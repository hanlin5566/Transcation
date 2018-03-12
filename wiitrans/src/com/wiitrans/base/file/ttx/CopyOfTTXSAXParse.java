package com.wiitrans.base.file.ttx;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.wiitrans.base.xml.XNode;

public class CopyOfTTXSAXParse extends DefaultHandler{
	
	private String preTagName = null;
	private int newline;
	private int isSource = 0;
	private XNode fragEntry;
	private StringBuffer sBuffer;
	private StringBuffer tBuffer;
	public void parse(String file,XNode fragEntry) throws Exception{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		this.fragEntry = fragEntry;
		parser.parse(new File(file), new CopyOfTTXSAXParse());
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(preTagName != null){
			String str = new String(ch,start,length);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(newline==isSource){
				System.out.println();
				System.out.print(isSource==1?"source："+str:"target:"+str);
				newline = isSource == 0?1:0;
			}else{
				System.out.print(str);
			}
//			TTXParse parse;
//			System.out.println(sBuffer.toString());
			//通过工厂模式拿出相应的解析类
			
//			parse.ParseBody(fragEntry, lineString, );
		}
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		if("Tu".equalsIgnoreCase(qName)){
			preTagName = null;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		if("Tu".equalsIgnoreCase(qName)){
			isSource = isSource == 0?1:0;
			if(isSource == 0){
				//new line
				sBuffer = new StringBuffer();
			}
			newline = isSource;
			preTagName = qName;
		}
	}
}
