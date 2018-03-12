package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.ProcLoginMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class ProcLoginDAO extends CommonDAO {

	private ProcLoginMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(ProcLoginMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<Integer> Login(int user_id) {
		return _mapper.Login(user_id);
	}
	public int getSysMsgCount(int user_id) {
	    return _mapper.getSysMsgCount(user_id);
	}
}
