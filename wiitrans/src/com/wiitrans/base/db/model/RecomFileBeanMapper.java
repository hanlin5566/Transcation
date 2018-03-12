package com.wiitrans.base.db.model;

import java.util.List;

public interface RecomFileBeanMapper {

	public List<RecomFileBean> SelectFiles(int order_id);
	
	public RecomFileBean Select(int file_id);

}
