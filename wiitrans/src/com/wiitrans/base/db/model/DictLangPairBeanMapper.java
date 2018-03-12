package com.wiitrans.base.db.model;

import java.util.List;

public interface DictLangPairBeanMapper {
	public DictLangPairBean Select(int pair_id);

	public List<DictLangPairBean> SelectAllPair();
}
