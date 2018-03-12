package com.wiitrans.base.db.model;

import java.util.List;

public interface DictTermBeanMapper {

	public List<DictTermBean> SelectAll();

	public List<DictTermBean> SelectForUpdateTime(int pair_id, int industry_id,
			int time);

	public List<DictTermBean> SelectForUpdateTimeOnPair(int pair_id, int time);
}
