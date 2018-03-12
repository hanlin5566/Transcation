package com.wiitrans.base.db.model;

import java.util.List;

public interface TermCustoBeanMapper {
	public List<TermCustoBean> SelectForUpdateTime(int term_group_id,
			int time);
}
