package com.wiitrans.base.file.lang;

import java.io.File;
import java.util.ArrayList;

import org.xml.sax.SAXParseException;

import com.wiitrans.base.db.TMServiceDAO;
import com.wiitrans.base.file.FileUtil;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;
import com.wiitrans.base.tm.LoadDataInfile;
import com.wiitrans.base.tm.TMLanguage;
import com.wiitrans.base.tm.TMTU;

public class TmxFileChunk {
	public String _tmxFilePath = null;
	public String _loadDataFilePath = null;
	public String _prefix = null;
	public String _suffix = null;
	public TMLanguage _sourceLang = null;
	public TMLanguage _targetLang = null;
	public ArrayList<TMTU> _tmtuList = null;
	public int _tmID = 0;
	public int _countPerChunk = 0;
	public LoadDataInfile _infile = null;

	public int Init(String tmxFilePath, String loadDataFilePath, String prefix,
			String suffix, TMLanguage sourceLang, int countPerChunk, int tmID) {

		int ret = Const.FAIL;

		this._tmxFilePath = tmxFilePath;
		this._loadDataFilePath = loadDataFilePath;
		this._prefix = prefix;
		this._suffix = suffix;
		this._sourceLang = sourceLang;
		this._countPerChunk = countPerChunk;
		this._tmID = tmID;
		ret = Const.SUCCESS;

		return ret;
	}

	public int UnInit() {
		int ret = Const.FAIL;

		this._tmxFilePath = null;
		this._sourceLang = null;
		this._targetLang = null;
		if (this._tmtuList != null) {
			this._tmtuList.clear();
		}
		ret = Const.SUCCESS;
		return ret;
	}

	public int AnalyseTMXFile(int indexMax) {
		int ret = Const.FAIL;

		TMServiceDAO dao = null;
		try {
			_infile = new LoadDataInfile();
			String textFile = _loadDataFilePath + _prefix + _tmID + _suffix
					+ "text.txt";
			_infile.CreateFile(textFile);
			String indexFile = _loadDataFilePath + _prefix + _tmID + _suffix
					+ "index.txt";
			_infile.CreateFile(indexFile);
			String wordFile = null;
			if (_sourceLang != null
					&& _sourceLang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
				wordFile = _loadDataFilePath + _prefix + _tmID + _suffix
						+ "word.txt";
				_infile.CreateFile(wordFile);
			}

			try {
				TmxFileChunkParse sax = new TmxFileChunkParse();
				sax.parse(this, indexMax);
			} catch (SAXParseException e) {
				Log4j.warn(e.getMessage());

				// 原始文件改为后缀名加s，替换无效字符后的文件保存为原始文件
				File source = new File(_tmxFilePath);
				String path = source.getPath();
				File target = new File(path + "tmp");
				FileUtil.copyFileForTxt(source, target, "utf-8");
				source.renameTo(new File(path + "s"));
				target.renameTo(new File(path));
				Log4j.warn("Replace invalid XML character");

				// 临时文件重建
				_infile.CreateFile(textFile);
				_infile.CreateFile(indexFile);
				if (_sourceLang != null
						&& _sourceLang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
					wordFile = _loadDataFilePath + _prefix + _tmID + _suffix
							+ "word.txt";
					_infile.CreateFile(wordFile);
				}

				TmxFileChunkParse sax = new TmxFileChunkParse();
				sax.parse(this, indexMax);
			}

			dao = new TMServiceDAO();
			dao.Init();
			dao.ImportText(_tmID, textFile);
			dao.ImportIndex(_tmID, indexFile);
			if (_sourceLang != null
					&& _sourceLang.GetLanguageType() == LANGUAGE_TYPE.LETTER) {
				dao.ImportWord(_tmID, wordFile);
				// dao.ImportTime(_tmID, _loadDataFilePath + _prefix + _tmID
				// + _suffix + "time.txt");
			}
			dao.Commit();

			ret = Const.SUCCESS;
		} catch (Exception e) {
			Log4j.error(e);
		} finally {
			// _sourceLang.Closedb();
			if (dao != null) {
				dao.UnInit();
			}
		}

		return ret;
	}

	public int ChunkWriteAppendIndex(String index) {
		String indexFileName = _loadDataFilePath + _prefix + _tmID + _suffix
				+ "index.txt";
		return _infile.WriteAppend(indexFileName, index);
	}

	public int ChunkWriteAppendText(String text) {
		String indexFileName = _loadDataFilePath + _prefix + _tmID + _suffix
				+ "text.txt";
		return _infile.WriteAppend(indexFileName, text);
	}

	public int ChunkWriteAppendTime(String time) {
		String indexFileName = _loadDataFilePath + _prefix + _tmID + _suffix
				+ "time.txt";
		return _infile.WriteAppend(indexFileName, time);
	}

	public int ChunkWriteAppendWord(String word) {
		String indexFileName = _loadDataFilePath + _prefix + _tmID + _suffix
				+ "word.txt";
		return _infile.WriteAppend(indexFileName, word);
	}

	public int AnalyseChunk(ArrayList<TMTU> tmtuList) {
		int ret = Const.FAIL;
		if (_tmID > 0 && tmtuList != null && tmtuList.size() > 0) {
			_sourceLang.ParseChunk(tmtuList);
			// _sourceLang.WriteTMText(tmtuList);
			ret = Const.SUCCESS;
		}
		return ret;
	}
}
