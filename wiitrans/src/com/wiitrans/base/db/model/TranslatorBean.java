package com.wiitrans.base.db.model;

public class TranslatorBean {
	// 用户ID
	public int user_id;

	public String email;
	// 昵称
	public String nickname;
	// 支付宝帐号
	public String account;

	public int translator_id = 0;

	public short level_id = 0;

	public String level;

	public int experience = 0;

	public double money = 0;

	public double aggregate_money = 0;

	public double money_usd = 0;

	public double aggregate_money_usd = 0;

	public double money_eur = 0;

	public double aggregate_money_eur = 0;

	public int word_count = 0;

	public int normal_order_number = 0;

	public int total_order_number = 0;

	public String head;

	public int time_zone_id = 76;

	// 帐号正在翻译或校对的订单
	public String codes;

	// 帐号可抢普通的翻译订单
	public String normalcodes;

	// 帐号可抢高级的翻译订单
	public String viptcodes;

	// 帐号可抢高级的校对订单
	public String vipecodes;

	public int create_time;
}
