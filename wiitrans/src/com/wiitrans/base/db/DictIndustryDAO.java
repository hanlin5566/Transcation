package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.DictIndustryBean;
import com.wiitrans.base.db.model.DictIndustryBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class DictIndustryDAO extends CommonDAO {

	private DictIndustryBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(DictIndustryBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public DictIndustryBean Select(int industry_id) {
		return _mapper.Select(industry_id);
	}

	public List<DictIndustryBean> SelectAll() {
		return _mapper.SelectAll();
	}

	public DictIndustryBean SelectOthers() {
		return _mapper.SelectOthers();
	}
}
