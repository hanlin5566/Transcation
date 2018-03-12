package com.wiitrans.base.file.lang;

import java.util.ArrayList;
import com.wiitrans.base.file.frag.Fragmentation;
import com.wiitrans.base.file.notag.BiliFileNoTag;
import com.wiitrans.base.file.sentence.Sentence;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.tm.DetectTMLanguage;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.tm.TMTU;
import com.wiitrans.base.xml.XDocument;
import com.wiitrans.base.xml.XNode;

public class TmxFile {

	private BiliFileNoTag _bilifile = null;
	public String _tmxFilePath = null;
	public TMLanguage _sourceLang = null;
	public TMLanguage _targetLang = null;
	public ArrayList<TMTU> _tmtuList = null;

	public int Init(BiliFileNoTag bilifile, String tmxFilePath,
			TMLanguage sourceLang, TMLanguage targetLang) {

		int ret = Const.FAIL;

		this._bilifile = bilifile;
		this._tmxFilePath = tmxFilePath;
		this._sourceLang = sourceLang;
		this._targetLang = targetLang;

		ret = Const.SUCCESS;

		return ret;
	}

	public int Init(BiliFileNoTag bilifile, String tmxFilePath,
			String sourceLang, String targetLang) {

		int ret = Const.FAIL;

		this._bilifile = bilifile;
		this._tmxFilePath = tmxFilePath;
		this._sourceLang = DetectTMLanguage.Detect(sourceLang);
		this._targetLang = DetectTMLanguage.Detect(targetLang);

		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		this._bilifile = null;
		this._tmxFilePath = null;
		this._sourceLang = null;
		this._targetLang = null;
		this._tmtuList.clear();

		return ret;
	}

	public int Parse() {
		int ret = Const.FAIL;
		_tmtuList = new ArrayList<TMTU>();
		int tuid = 0;
		try {
			if (_bilifile != null) {
				if (_bilifile._virtualFrags != null
						&& _bilifile._virtualFrags.size() > 0) {
					for (Fragmentation frag : _bilifile._virtualFrags) {
						for (Sentence sentence : frag._sentences) {
							TMTU tu = new TMTU();
							tu._tuid = ++tuid;
							tu._tuv1 = sentence._source;
							if (sentence._edit != null) {
								tu._tuv2 = sentence._edit;
							} else if (sentence._translate != null) {
								tu._tuv2 = sentence._translate;
							} else {
								tu._tuv2 = sentence._source;
							}
							_tmtuList.add(tu);
						}
					}
				}

				ret = Const.SUCCESS;
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	public int WriteTMXFile() {
		int ret = Const.FAIL;
		try {
			if (_tmtuList != null && _tmxFilePath != null) {

				XDocument xmlDoc = new XDocument("UNICODELITTLE");
				//XDocument xmlDoc = new XDocument();
				XNode tmx = new XNode("tmx");
				tmx.SetAttr("version", "1.4");
				xmlDoc.SetRoot(tmx);

				XNode tmxFile = xmlDoc.GetRoot();

				XNode header = new XNode("header");
				header.SetAttr("datatype", "xml");
				header.SetAttr("segtype", "sentence");
				header.SetAttr("adminlang",
						_sourceLang.GetLanguageCountryName());
				header.SetAttr("srclang", _sourceLang.GetLanguageCountryName());
				tmxFile.AddChild(header);

				XNode body = new XNode("body");

				if (_tmtuList.size() > 0) {
					for (TMTU tmtu : _tmtuList) {

						XNode tu = new XNode("tu");

						XNode tuv1 = new XNode("tuv");
						tuv1.SetAttr("xml:lang",
								_sourceLang.GetLanguageCountryName());
						XNode seg1 = new XNode("seg");
						seg1.SetValue(tmtu._tuv1);
						tuv1.AddChild(seg1);
						tu.AddChild(tuv1);

						XNode tuv2 = new XNode("tuv");
						tuv2.SetAttr("xml:lang",
								_targetLang.GetLanguageCountryName());
						XNode seg2 = new XNode("seg");
						seg2.SetValue(tmtu._tuv2);
						tuv2.AddChild(seg2);
						tu.AddChild(tuv2);

						body.AddChild(tu);

					}

					tmxFile.AddChild(body);
				}

				ret = xmlDoc.Save(_tmxFilePath);
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	public int ReadTMXFile() {
		int ret = Const.FAIL;
		_tmtuList = new ArrayList<TMTU>();
		try {
			TmxFileParse sax = new TmxFileParse();
			sax.parse(this);
			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}
}
