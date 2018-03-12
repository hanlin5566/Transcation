package com.wiitrans.base.db.model;

import java.util.List;

public interface DictLangBeanMapper {

	public List<DictLangBean> SelectAll();

	public DictLangBean Select(int language_id);
}
