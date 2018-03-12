package com.wiitrans.base.db;

import java.util.List;
import java.util.Map;

import com.wiitrans.base.db.model.ProcTranslatorMsgMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ProcTranslatorMsgDAO extends CommonDAO {

	private ProcTranslatorMsgMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(ProcTranslatorMsgMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<List<?>> Select(Map<String, Object> map) {
		return _mapper.Select(map);
	}
}
