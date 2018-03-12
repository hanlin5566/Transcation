package com.wiitrans.base.file.lang;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.TMTU;

public class TmxFileParse extends DefaultHandler {

	private String _preTagName = null;
	private int _lineIndex = 0;
	private boolean isSource = false;
	private StringBuffer _sb;

	private TmxFile _tmxfile;
	private TMTU _tmtu;

	public void parse(TmxFile tmxfile) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		try {
			this._tmxfile = tmxfile;
			parser.parse(new File(tmxfile._tmxFilePath), this);
		} catch (Exception e) {
			// System.err.println(tmxfile._tmxFilePath + "parse error case"
			// + e.fillInStackTrace());
			Log4j.error(e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if ("seg".equalsIgnoreCase(_preTagName)) {
			_sb.append(new String(ch, start, length));
		}
	}

	@Override
	public void endDocument() throws SAXException {
		Log4j.log("tmx parse element num " + _lineIndex);
		Log4j.log("tmx parse end document");
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Tu end
		switch (qName.toLowerCase()) {
		case "tu":
			_tmtu._tuid = ++_lineIndex;
			if ((_lineIndex & (_lineIndex - 1)) == 0) {
				// 输出2的幂次的记录，可以防止需要打印几万条的情况
				Log4j.log("tmx parse element num " + _lineIndex);
			}
			_tmxfile._tmtuList.add(_tmtu);
			break;
		case "seg":
			if (isSource) {
				_tmtu._tuv1 = _sb.toString();
			} else {
				_tmtu._tuv2 = _sb.toString();

			}
			_sb = null;
			_preTagName = null;
			break;
		case "body":
			Log4j.log("tmx parse the end element num " + _lineIndex);
			break;
		default:
			break;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		Log4j.log("tmx parse start document");

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		switch (qName.toLowerCase()) {
		case "tu":
			_tmtu = new TMTU();
			break;
		case "seg": {
			_sb = new StringBuffer();
			_preTagName = qName;
			break;
		}
		case "tuv":
			String lang = attributes.getValue("xml:lang");
			if (lang.equalsIgnoreCase("ja")) {
				lang = "ja-JP";
			}
			if (!lang.equalsIgnoreCase(_tmxfile._sourceLang
					.GetLanguageCountryName())) {
				if (_tmxfile._targetLang == null) {
					_tmxfile._targetLang = DetectTMLanguage.Detect(lang);
				}
				isSource = false;
			} else {
				isSource = true;
			}

			break;
		case "header": {
			_tmxfile._sourceLang = DetectTMLanguage.Detect(attributes
					.getValue("srclang"));
			break;
		}
		default:
			break;
		}
	}
}