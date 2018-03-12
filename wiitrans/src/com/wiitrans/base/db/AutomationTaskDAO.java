package com.wiitrans.base.db;

import java.util.List;
import java.util.Map;
import com.wiitrans.base.db.model.AutomationTaskBean;
import com.wiitrans.base.db.model.AutomationTaskBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class AutomationTaskDAO extends CommonDAO {

	private AutomationTaskBeanMapper _automationTaskmapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_automationTaskmapper = _session.getMapper(AutomationTaskBeanMapper.class);
				if (_automationTaskmapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}
	
	public void insert(AutomationTaskBean task) {
		 _automationTaskmapper.insert(task);
	}
	
	public void update(AutomationTaskBean task) {
		 _automationTaskmapper.updateTask(task);
	}
	
	public List<AutomationTaskBean> selectTask(Map<String,Object> param) {
		 return _automationTaskmapper.selectTask(param);
	}
	
	public boolean isActive(AutomationTaskBean task) {
		task = _automationTaskmapper.getAutomationTaskBean(task);
		if(task!=null && Const.TASK_DELETE_N == task.getDeleted() && Const.TASK_STATUS_ACTIVE == task.getStatus()){
			return true;
		}
		 return false;
	}
}