package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface StrategyBeanMapper {
	public List<StrategyBean> getUnknownJudge(Map<String,Integer> param);
	public void deleteUnFinishedJudgeVote(int paperId);
	public void deleteUnknowJudge(int strategy_id);
	public void evalJudge(Map<String, Integer> param);
	public void updateAutoJudge(int strategy_id);
}
