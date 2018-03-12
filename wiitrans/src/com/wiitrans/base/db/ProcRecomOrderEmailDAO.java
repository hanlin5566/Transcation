package com.wiitrans.base.db;

import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.model.ProcRecomOrderBean;
import com.wiitrans.base.db.model.ProcRecomOrderEmailMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ProcRecomOrderEmailDAO extends CommonDAO {
	private ProcRecomOrderEmailMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(ProcRecomOrderEmailMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<ProcRecomOrderBean> RecomOrderEmail(Map<String, Object> map) {
		return _mapper.RecomOrderEmail(map);
	}
}