package com.wiitrans.base.db.model;

import java.io.Serializable;
import java.util.Date;

public class DictFactorBean implements Serializable{
    	private static final long serialVersionUID = -9041821448304937348L;
	// ID
	public int factor_id;
	// 加急费率
	public double expedited_fee;
	// 客户服务费率
	public double customer_service_fee;
	// 翻译服务费率
	public double translator_service_fee;
	// 税率
	public double taxation;

	public Date start_time;

	public Date end_time;

}
