package com.wiitrans.base.db;

import java.util.List;

import com.wiitrans.base.db.model.DictRateBean;
import com.wiitrans.base.db.model.DictRateBeanMapper;
import com.wiitrans.base.db.model.DictRateCurrencyBean;
import com.wiitrans.base.db.model.RateCurrencyBean;
import com.wiitrans.base.log.Log4j;
import com.wiitrans.base.misc.Const;

public class DictRateDAO extends CommonDAO {

	private DictRateBeanMapper _mapper = null;

	public int Init(Boolean loadConf) {
		int ret = Const.FAIL;

		try {

			ret = super.Init(loadConf);

			if (ret == Const.SUCCESS) {
				_mapper = _session.getMapper(DictRateBeanMapper.class);

				if (_mapper != null) {
					ret = Const.SUCCESS;
				}
			}

		} catch (Exception ex) {
			Log4j.error(ex);
		}

		return ret;
	}

	public List<DictRateBean> SelectAll() {
		return _mapper.SelectAll();
	}

	public List<DictRateCurrencyBean> SelectCNY() {
		return _mapper.SelectCNY();
	}

	public List<DictRateCurrencyBean> SelectUSD() {
		return _mapper.SelectUSD();
	}

	public List<DictRateCurrencyBean> SelectEUR() {
		return _mapper.SelectEUR();
	}

	public RateCurrencyBean SelectRateCNY(int customer_id, int pair_id) {
		return _mapper.SelectRateCNY(customer_id, pair_id);
	}
}
