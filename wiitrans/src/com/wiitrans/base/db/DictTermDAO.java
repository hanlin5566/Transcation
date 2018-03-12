package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.DictTermBean;
import com.wiitrans.base.db.model.DictTermBeanMapper;
import com.wiitrans.base.db.model.DictTermDetailsBean;
import com.wiitrans.base.db.model.DictTermDetailsBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class DictTermDAO extends CommonDAO {
	private DictTermBeanMapper _termmapper = null;
	private DictTermDetailsBeanMapper _termdetailsmapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_termmapper = _session.getMapper(DictTermBeanMapper.class);
				_termdetailsmapper = _session
						.getMapper(DictTermDetailsBeanMapper.class);

				if (_termmapper != null && _termdetailsmapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<DictTermBean> SelectAllTerm() {
		return _termmapper.SelectAll();
	}

	public List<DictTermBean> SelectForUpdateTime(int pair_id, int industry_id,
			int time) {
		return _termmapper.SelectForUpdateTime(pair_id, industry_id, time);
	}

	public List<DictTermBean> SelectForUpdateTimeOnPair(int pair_id, int time) {
		return _termmapper.SelectForUpdateTimeOnPair(pair_id, time);
	}

	public List<DictTermDetailsBean> SelectAllTermDetails() {
		return _termdetailsmapper.SelectAll();
	}

	public List<DictTermDetailsBean> SelectTermDetailsByTermID(int term_id) {
		return _termdetailsmapper.SelectByTermID(term_id);
	}

	public List<DictTermDetailsBean> SelectTermDetailsForUpdateTimeOnPair(
			int pair_id, int time) {
		return _termdetailsmapper.SelectForUpdateTimeOnPair(pair_id, time);
	}
}
