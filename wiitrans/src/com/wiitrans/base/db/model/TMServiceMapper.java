package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface TMServiceMapper {

	public void DropTMIndexIfExists(Map<String, Object> map);

	public void CreateTMIndex(Map<String, Object> map);

	public void DropTMWordIfExists(Map<String, Object> map);

	public void CreateTMWord(Map<String, Object> map);

	public void DropTMTimeIfExists(Map<String, Object> map);

	public void CreateTMTime(Map<String, Object> map);

	public void DropTMTextIfExists(Map<String, Object> map);

	public void CreateTMText(Map<String, Object> map);

	public void ImportIndex(Map<String, Object> map);

	public void ImportText(Map<String, Object> map);

	public void ImportTime(Map<String, Object> map);

	public void ImportWord(Map<String, Object> map);

	public List<TMServiceTimesBean> SelectTimes(Map<String, Object> map);

	public List<TMServiceIndexBean> SelectIndexByWordIDs(Map<String, Object> map);

	public List<TMServiceIndexBean> SelectIndexByTuIDs(Map<String, Object> map);

	public List<TMServiceTextBean> SelectTextByTuIDs(Map<String, Object> map);

	public List<TMServiceTextBean> SelectTextByCHNWordIDs(
			Map<String, Object> map);

	public int SelectTextMaxID(Map<String, Object> map);

	public void TruncateTime(Map<String, Object> map);

	public void CleanUpTM(Map<String, Object> map);
}
