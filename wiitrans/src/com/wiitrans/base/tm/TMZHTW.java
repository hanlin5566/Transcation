package com.wiitrans.base.tm;

import com.wiitrans.base.file.lang.LangConst;
import com.wiitrans.base.file.lang.TmxFile;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_COUNTRY;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_NAME;
import com.wiitrans.base.file.lang.LangConst.LANGUAGE_TYPE;

public class TMZHTW extends TMZHCN {
	private LANGUAGE_NAME _name = LANGUAGE_NAME.TRADITIONAL;
	private LANGUAGE_TYPE _type = LANGUAGE_TYPE.IDEOGRAPH;
	private LANGUAGE_COUNTRY _langcountry = LANGUAGE_COUNTRY.ZHTW;
	private String _slangcountry = LangConst.LANGUAGE_COUNTRY_ZHTW;

	private TmxFile _tmFile;
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
}
