package com.wiitrans.base.db;

import com.wiitrans.base.db.model.DictFactorBean;
import com.wiitrans.base.db.model.DictFactorBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class DictFactorDAO extends CommonDAO {
	
	private DictFactorBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {
			ret = super.Init(loadConf);
			
			if(ret == Const.SUCCESS)
			{
				_mapper = _session.getMapper(DictFactorBeanMapper.class);
	
				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public DictFactorBean Select() {
		return _mapper.Select();
	}
}
