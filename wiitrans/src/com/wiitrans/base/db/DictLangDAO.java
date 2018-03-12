package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.DictLangBean;
import com.wiitrans.base.db.model.DictLangBeanMapper;
import com.wiitrans.base.db.model.DictLangCountryBean;
import com.wiitrans.base.db.model.DictLangCountryBeanMapper;
import com.wiitrans.base.db.model.DictLangPairBean;
import com.wiitrans.base.db.model.DictLangPairBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class DictLangDAO extends CommonDAO {

	private DictLangBeanMapper _mapperLang = null;
	private DictLangPairBeanMapper _mapperPair = null;
	private DictLangCountryBeanMapper _mapperLangCountry = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapperLang = _session.getMapper(DictLangBeanMapper.class);
				_mapperPair = _session.getMapper(DictLangPairBeanMapper.class);
				_mapperLangCountry = _session
						.getMapper(DictLangCountryBeanMapper.class);
				if (_mapperLang != null && _mapperPair != null
						&& _mapperLangCountry != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<DictLangBean> SelectAll() {
		return _mapperLang.SelectAll();
	}

	public DictLangBean Select(int language_id) {
		return _mapperLang.Select(language_id);
	}

	public DictLangPairBean SelectPair(int pair_id) {
		return _mapperPair.Select(pair_id);
	}

	public List<DictLangPairBean> SelectAllPair() {
		return _mapperPair.SelectAllPair();
	}

	public DictLangCountryBean SelectLangCountry(String lang_country) {
		return _mapperLangCountry.Select(lang_country);
	}
}
