package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface ProcTranslatorMsgMapper {
	public List<List<?>> Select(Map<String,Object> map);
}
