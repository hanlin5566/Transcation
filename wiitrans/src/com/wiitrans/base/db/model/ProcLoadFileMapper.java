package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface ProcLoadFileMapper {
	public List<List<?>> LoadFileSelect(Map<String, Object> map);
}
