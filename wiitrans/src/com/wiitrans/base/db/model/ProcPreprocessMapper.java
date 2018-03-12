package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface ProcPreprocessMapper {
	public List<List<?>> PreprocessSelect(Map<String, Object> map);
}
