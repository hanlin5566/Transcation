package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface MatchServiceMapper {
	public void ImportOrderKeyword(Map<String, Object> map);

	public void DeleteNewOrderKeyword(int order_id);

	public void ImportNewOrderKeyword(Map<String, Object> map);

	public void DeleteNoOrderKeyword(String java_id);

	public void ImportNoOrderKeyword(Map<String, Object> map);

	public void AddKeywordScore(Map<String, Object> map);

	public List<TransScoreBean> SelectTopTrans(Map<String, Object> map);

	public List<TransScoreBean> SelectTopEdits(Map<String, Object> map);

	public List<TransScoreBean> SelectTrans(Map<String, Object> map);

	public List<TransScoreBean> SelectTopTransByJavaID(Map<String, Object> map);

	public List<TransScoreBean> SelectTopEditsByJavaID(Map<String, Object> map);

	public List<TransScoreBean> SelectTransByJavaID(Map<String, Object> map);
}
