package com.wiitrans.base.db.model;

import java.util.List;

public interface DictIndustryBeanMapper {
	public List<DictIndustryBean> SelectAll();

	public DictIndustryBean Select(int industry_id);

	public DictIndustryBean SelectOthers();
}
