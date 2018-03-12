package com.wiitrans.base.db.model;

import java.io.Serializable;

public class DictRateCurrencyBean implements Serializable{
    	private static final long serialVersionUID = 4494200236972720913L;
	public int rate_id;
	// 级别
	public int price_level_id;
	// 语言对
	public int pair_id;
	// 费率
	public double rate;
	
	public int rate_factor;

	public double rate_t;

	public double rate_e;

}
