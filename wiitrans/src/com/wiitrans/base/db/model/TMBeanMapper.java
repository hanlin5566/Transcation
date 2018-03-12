package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface TMBeanMapper {

	public List<TMBean> SelectAll();

	public TMBean SelectForTMID(int tmid);

	public TMBean SelectForNodeTMID(int tmid);

	public void UpdateAnalyse(TMBean tm);

	public void UpdateAnalyseForNode(TMBean tm);

	public void Delete(int tmid);

	public void DeleteForNode(int tmid);

	public List<Integer> Deletable(Map<String, Object> map);
}
