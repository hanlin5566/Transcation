package com.wiitrans.base.db;

import com.wiitrans.base.db.model.TranslatorFinanceBean;
import com.wiitrans.base.db.model.TranslatorFinanceBeanMapper;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class TranslatorFinanceDAO extends CommonDAO {

	private TranslatorFinanceBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(TranslatorFinanceBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	// TODO:此处需要增加调用成功失败返回值
	public void Insert(TranslatorFinanceBean tran) {
		_mapper.Insert(tran);
	}
}
