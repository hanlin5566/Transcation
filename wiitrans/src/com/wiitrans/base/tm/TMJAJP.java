package com.wiitrans.base.tm;

import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class TMJAJP extends TMZHCN {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.JAPANESE;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.IDEOGRAPH;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.JAJP;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_JAJP;

	private long _first = 91125000000000L;
	private long _second = 2025000000;
	private long _third = 45000;

	private TmxFile _tmFile;

	private TmxFile getTmxFile() {
		return _tmFile;
	}

	private void setTmxFile(TmxFile tmFile) {
		this._tmFile = tmFile;
	}

	private boolean _isSource;

	@Override
	public LANGUAGE_NAME GetLanguageName() {
		return _name;
	}

	@Override
	public LANGUAGE_TYPE GetLanguageType() {
		return _type;
	}

	@Override
	public String GetLanguageCountryName() {
		return _slangcountry;
	}

	@Override
	public LANGUAGE_COUNTRY GetLanguageCountry() {
		return _langcountry;
	}

	@Override
	public int IsLetter(char c) {
		int result;
		if (c >= 0x3000 && c <= 0x9fff) {
			// if (c >= 0x4e00 && c <= 0x9fbb) {
			result = c;
		} else if (c >= 0xac00 && c <= 0xd7af) {
			result = c - 0xac00 + 1;
		} else {
			result = 0;
		}
		return result;
	}
}
