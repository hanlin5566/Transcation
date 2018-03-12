package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface SubjectiveBeanMapper {
	public List<SubjectiveBean> getSubjective(Map<String,Integer> param);
	public void evalSubjective(Map<String,Integer> param);
	public void deleteUnFinishedSubjectiveAnswer(int paperId);
}
