package com.wiitrans.base.db.model;

import java.util.List;

public interface DictRateBeanMapper {
	// 查询全部数据
	public List<DictRateBean> SelectAll();

	public List<DictRateCurrencyBean> SelectCNY();

	public List<DictRateCurrencyBean> SelectUSD();

	public List<DictRateCurrencyBean> SelectEUR();

	public RateCurrencyBean SelectRateCNY(int customer_id, int pair_id);
}
