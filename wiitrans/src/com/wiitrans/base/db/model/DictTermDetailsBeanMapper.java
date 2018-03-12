package com.wiitrans.base.db.model;

import java.util.List;

public interface DictTermDetailsBeanMapper {
	public List<DictTermDetailsBean> SelectAll();

	public List<DictTermDetailsBean> SelectByTermID(int term_id);

	public List<DictTermDetailsBean> SelectForUpdateTimeOnPair(int pair_id,
			int time);

}
