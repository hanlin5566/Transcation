package com.wiitrans.base.db;

import java.util.List;
import com.wiitrans.base.db.model.TermCustoBean;
import com.wiitrans.base.db.model.TermCustoBeanMapper;
import com.wiitrans.base.db.model.TermCustoGroupBean;
import com.wiitrans.base.db.model.TermCustoGroupBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TermGroupDAO extends CommonDAO {

	private TermCustoGroupBeanMapper _termCustoGroupmapper = null;
	private TermCustoBeanMapper _termCustomapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_termCustoGroupmapper = _session
						.getMapper(TermCustoGroupBeanMapper.class);
				_termCustomapper = _session
						.getMapper(TermCustoBeanMapper.class);
				if (_termCustoGroupmapper != null && _termCustomapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<TermCustoGroupBean> SelectAllTermCustoGroup() {
		return _termCustoGroupmapper.SelectAllTermCustoGroup();
	}

	public List<TermCustoBean> SelectForUpdateTime(int term_group_id, int time) {
		return _termCustomapper.SelectForUpdateTime(term_group_id, time);
	}
}
