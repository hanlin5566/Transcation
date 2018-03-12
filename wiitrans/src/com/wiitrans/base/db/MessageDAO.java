package com.wiitrans.base.db;

import org.json.JSONObject;
import com.wiitrans.base.db.model.MessageUserBean;
import com.wiitrans.base.db.model.MessageUserBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class MessageDAO extends CommonDAO {

	private MessageUserBeanMapper _mapper = null;

	public int Init(Boolean loadConf, JSONObject obj) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf, obj);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(MessageUserBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(MessageUserBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public void Insert(MessageUserBean msg) {
		_mapper.Insert(msg);
	}
}
