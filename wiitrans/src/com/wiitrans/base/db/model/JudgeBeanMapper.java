package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface JudgeBeanMapper {
	public List<JudgeBean> getKnownJudge(Map<String,Integer> param);
	public void movetoKnownJudge(Map<String,Integer> param);
}
