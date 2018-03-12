package com.wiitrans.base.db.model;

import java.io.Serializable;

public class DictRateBean implements Serializable{
	// 费率ID
	public int rate_id;
	// 级别
	public int price_level_id;
	// 语言对
	public int pair_id;
	// 每小时字数
	public int word_per_hour_t;

	public int word_per_hour_t_s;

	public int word_per_hour_e;

	public int word_per_hour_e_s;

	public int buffer_time_t;

	public int buffer_time_e;

	public int buffer_time_t_factor_s;

	public int buffer_time_e_factor_s;

}
