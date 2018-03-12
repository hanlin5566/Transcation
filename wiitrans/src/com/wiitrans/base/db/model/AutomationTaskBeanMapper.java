package com.wiitrans.base.db.model;

import java.util.List;
import java.util.Map;

public interface AutomationTaskBeanMapper {
	public void insert(AutomationTaskBean task);
	public void updateTask(AutomationTaskBean task);
	public List<AutomationTaskBean> selectTask(Map<String,Object> param);
	public AutomationTaskBean getAutomationTaskBean(AutomationTaskBean task);
}
